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
import org.dotlin.compiler.backend.steps.ir2ast.ir.remapTypes
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

                    val referenced = when (val owner = expression.symbol.owner) {
                        is IrConstructor -> owner.parentClassOrNull
                        else -> owner as? IrDeclarationWithName
                    } ?: return

                    file.maybeAddDartImportsFor(referenced)
                }

                override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) =
                    visitDeclarationReference(expression)

                override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
            }
        )

        file.remapTypes { type ->
            fun maybeAddImport(type: IrType) {
                if (type is IrSimpleType) {
                    // Add import for type alias or class.
                    type.abbreviation?.typeAlias?.owner?.let { file.maybeAddDartImportsFor(it) }
                        ?: type.classOrNull?.owner?.let { file.maybeAddDartImportsFor(it) }

                    // Add imports for type arguments.
                    type.arguments.forEach {
                        it.typeOrNull?.let { t -> maybeAddImport(t) }
                    }
                }
            }

            maybeAddImport(type)

            type
        }
    }

    context(DotlinLoweringContext)
    private fun IrFile.maybeAddDartImportsFor(
        declaration: IrDeclarationWithName
    ) {
        if (declaration.name.isSpecial) return
        if (declaration.fqNameWhenAvailable in ignore) return
        if (declaration is IrValueParameter) return
        // TODO: Handle Unit
        if (declaration is IrClass && declaration.defaultType.isUnit()) return
        // Don't import methods.
        if (declaration is IrSimpleFunction && declaration.dispatchReceiverParameter != null) return

        // If the file is null, it's most likely Kotlin intrinsics (ANDAND, OROR, EQEQ, etc.)
        val fileOfDeclaration = declaration.fileOrNull ?: return

        // We don't need to import declarations defined in the same file.
        if (this == fileOfDeclaration) return

        // Import the extension container class if it's an extension.
        val relevantDeclaration = declaration.extensionContainer ?: declaration

        val descriptor = relevantDeclaration.descriptor

        val libraryUri = relevantDeclaration.annotatedDartLibrary ?: run {
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

            val fileName = when (descriptor) {
                !is DartDescriptor -> fileOfDeclaration.dartPath
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

            "package:$packageName/$fileName"
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