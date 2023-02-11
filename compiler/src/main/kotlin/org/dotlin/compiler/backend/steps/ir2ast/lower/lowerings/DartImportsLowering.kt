/*
 * Copyright 2021-2022 Wilko Manger
 *
 * This file is part of Dotlin.
 *
 * Dotlin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dotlin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Dotlin.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:OptIn(ObsoleteDescriptorBasedAPI::class)

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.annotatedDartLibrary
import org.dotlin.compiler.backend.attributes.DartImport
import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrTypeContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrTypeContext.SuperTypes
import org.dotlin.compiler.backend.steps.ir2ast.ir.correspondingProperty
import org.dotlin.compiler.backend.steps.ir2ast.ir.isNonExtensionMethod
import org.dotlin.compiler.backend.steps.ir2ast.ir.visitTypes
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrFileLowering
import org.dotlin.compiler.backend.steps.src2ir.dotlinModule
import org.dotlin.compiler.backend.util.importAliasIn
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module

/**
 * Dart import directives are added, based on types used in files.
 */
class DartImportsLowering(override val context: DotlinLoweringContext) : IrFileLowering {
    /**
     * FQNs of declarations that should never be imported.
     */
    private val ignore = listOf(
        dotlin.typeOf,
        dotlin.intrinsics.Dynamic,
        dotlin.dart,
    )

    override fun DotlinLoweringContext.transform(file: IrFile) {
        // Add imports through declaration references and types.
        file.acceptChildrenVoid(
            object : IrElementVisitorVoid {
                override fun visitDeclarationReference(expression: IrDeclarationReference) {
                    super.visitDeclarationReference(expression)

                    val declaration = expression.symbol.owner as? IrDeclaration ?: return

                    // TODO: typeContext = null must be explicitely passed because of a bug in contextual receivers
                    file.maybeAddDartImportsFor(declaration, typeContext = null)
                }

                override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) =
                    visitDeclarationReference(expression)

                override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
            }
        )

        file.visitTypes { type, context ->
            if (type is IrSimpleType) {
                // Add import for type alias or class.
                type.abbreviation?.typeAlias?.owner?.let { file.maybeAddDartImportsFor(it, context) }
                    ?: type.classOrNull?.owner?.let { file.maybeAddDartImportsFor(it, context) }
            }
        }
    }

    context(DotlinLoweringContext)
    private fun IrFile.maybeAddDartImportsFor(
        declaration: IrDeclaration,
        typeContext: IrTypeContext? = null
    ) {
        if (declaration !is IrDeclarationWithName) return
        if (declaration.fqNameWhenAvailable in ignore) return
        if (declaration is IrValueParameter) return
        // TODO: Handle Unit
        if (declaration is IrClass && declaration.defaultType.isUnit()) return

        val isDartConstructor by lazy { declaration.hasAnnotation(dotlin.DartConstructor) }

        // Don't import methods (except if it's annotated with @DartConstructor).
        if (declaration is IrSimpleFunction && !isDartConstructor && declaration.isNonExtensionMethod) return

        // Import the extension container class if it's an extension.
        val relevantDeclaration = declaration.extensionContainer ?: when (declaration) {
            is IrConstructor -> declaration.parentAsClass
            is IrSimpleFunction -> when {
                // For `@DartConstructor`s, we want to import the parent class of the companion object it's in.
                isDartConstructor -> declaration.parentClassOrNull?.parentClassOrNull
                else -> null
            } ?: declaration.correspondingProperty ?: declaration

            else -> declaration
        }

        val isFunctionType = relevantDeclaration is IrClass && relevantDeclaration.defaultType.isFunction()

        if (isFunctionType && typeContext != null && typeContext !is SuperTypes) return

        // If the file is null, it's most likely Kotlin intrinsics (ANDAND, OROR, EQEQ, etc.)
        val fileOfDeclaration = when {
            // Function1, Function2, etc. are in an "external package fragment", but are in reality located
            // in the same file as the Function interface.
            isFunctionType -> {
                irBuiltIns.functionClass.owner.fileOrNull
            }

            else -> relevantDeclaration.fileOrNull
        } ?: return

        if (relevantDeclaration.name.isSpecial) return

        val annotatedLibraryUri = relevantDeclaration.annotatedDartLibrary

        // We don't need to import declarations defined in the same file, unless there's an `@DartLibrary` annotation.
        if (this == fileOfDeclaration && annotatedLibraryUri == null) return

        val descriptor = relevantDeclaration.descriptor

        val libraryUri = annotatedLibraryUri ?: run {
            var module = descriptor.module

            // If the imported declaration is from our module, or if we know it's written in Kotlin
            // we cannot build the file path based on the fq name, because the `package`
            // and path might not match.
            if (module == currentFile.module.descriptor) {
                return@run fileOfDeclaration.relativeDartPath.toString()
            }

            // At this point, the declaration is from a dependency and thus the module must be a
            // DotlinModule.
            module = module.dotlinModule ?: return
            val pkg = module.dartPackage

            val filePath = when (descriptor) {
                // Relativize to the package root, example:
                // Package root: ${pkg.path}/lib/
                // File Dart path: ${pkg.path}/lib/somewhere/else.dart
                // Result: somewhere/else.dart
                !is DartDescriptor -> pkg.packagePath.relativize(pkg.path.resolve(fileOfDeclaration.dartPath))
                else -> run {
                    val fqNameWithoutPackageAndDeclaration = descriptor.fqNameSafe
                        .toString()
                        .replace("${module.fqName}.", "")
                        .replace(".${relevantDeclaration.name.identifier}", "")

                    val extension = descriptor.element.location.library.name.split(".").lastOrNull()

                    fqNameWithoutPackageAndDeclaration
                        .split(".")
                        .joinToString("/", postfix = ".$extension")
                }
            }

            val packageName = module.dartPackage.name

            "package:$packageName/$filePath"
        }

        val showName = relevantDeclaration.simpleDartNameWithoutKotlinImportAlias.value
        val alias = relevantDeclaration.importAliasIn(currentFile)

        // We don't need to import "dart:core" if there's no alias, it's a default import in Dart.
        if (libraryUri == "dart:core" && alias == null) return

        dartImports.apply {
            add(DartImport(libraryUri, alias, hide = null, showName))

            if (alias != null) {
                add(DartImport(libraryUri, hide = showName))
            }
        }
    }
}