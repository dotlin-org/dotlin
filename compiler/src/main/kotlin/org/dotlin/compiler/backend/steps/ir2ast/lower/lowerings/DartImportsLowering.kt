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

import org.dotlin.compiler.backend.DartPathGenerator.dartPath
import org.dotlin.compiler.backend.DartPathGenerator.relativeDartPath
import org.dotlin.compiler.backend.annotatedDartLibrary
import org.dotlin.compiler.backend.attributes.DartImport
import org.dotlin.compiler.backend.descriptors.*
import org.dotlin.compiler.backend.descriptors.export.DartExportPackageFragmentDescriptor
import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrTypeContext.SuperTypes
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrFileLowering
import org.dotlin.compiler.backend.steps.src2ir.dotlinModule
import org.dotlin.compiler.backend.util.annotationsWithRuntimeRetention
import org.dotlin.compiler.backend.util.descriptor
import org.dotlin.compiler.backend.util.importAliasIn
import org.jetbrains.kotlin.backend.jvm.ir.getKtFile
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.descriptors.IrBuiltinsPackageFragmentDescriptorImpl
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.util.isStatic
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.nio.file.Path
import kotlin.io.path.relativeTo

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
                private val visitor = this

                override fun visitDeclarationReference(expression: IrDeclarationReference) {
                    super.visitDeclarationReference(expression)

                    val declaration = expression.symbol.owner as? IrDeclaration ?: return

                    // TODO: typeContext = null must be explicitly passed because of a bug in contextual receivers
                    file.maybeAddDartImportsFor(declaration, typeContext = null)
                }

                override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) =
                    visitDeclarationReference(expression)

                private fun IrAnnotationContainer.visitAnnotations() {
                    annotationsWithRuntimeRetention.forEach {
                        it.acceptChildrenVoid(visitor)
                        file.maybeAddDartImportsFor(it.symbol.owner, typeContext = null)
                    }
                }

                override fun visitAnonymousInitializer(declaration: IrAnonymousInitializer) {
                    super.visitAnonymousInitializer(declaration)
                    declaration.visitAnnotations()
                }

                override fun visitClass(declaration: IrClass) {
                    super.visitClass(declaration)
                    declaration.visitAnnotations()
                }

                override fun visitEnumEntry(declaration: IrEnumEntry) {
                    super.visitEnumEntry(declaration)
                    declaration.visitAnnotations()
                }

                override fun visitField(declaration: IrField) {
                    super.visitField(declaration)
                    declaration.visitAnnotations()
                }

                override fun visitFunction(declaration: IrFunction) {
                    super.visitFunction(declaration)
                    declaration.visitAnnotations()
                }

                override fun visitLocalDelegatedProperty(declaration: IrLocalDelegatedProperty) {
                    super.visitLocalDelegatedProperty(declaration)
                    declaration.visitAnnotations()
                }

                override fun visitProperty(declaration: IrProperty) {
                    super.visitProperty(declaration)
                    declaration.visitAnnotations()
                }

                override fun visitTypeAlias(declaration: IrTypeAlias) {
                    super.visitTypeAlias(declaration)
                    declaration.visitAnnotations()
                }

                override fun visitTypeParameter(declaration: IrTypeParameter) {
                    super.visitTypeParameter(declaration)
                    declaration.visitAnnotations()
                }

                override fun visitValueParameter(declaration: IrValueParameter) {
                    super.visitValueParameter(declaration)
                    declaration.visitAnnotations()
                }

                override fun visitVariable(declaration: IrVariable) {
                    super.visitVariable(declaration)
                    declaration.visitAnnotations()
                }

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

        // Don't import methods/property accessors, except if they're static or annotated with @DartConstructor, then
        // we will import the relevant class. This won't happen naturally because there's no IrDeclarationReference
        // whose type would be imported.
        if (declaration is IrSimpleFunction &&
            !declaration.isStatic &&
            !isDartConstructor &&
            declaration.isNonExtensionMethod
        ) {
            return
        }

        // Import the extension container class if it's an extension.
        val relevantDeclaration = declaration.extensionContainer ?: when (declaration) {
            is IrConstructor, is IrEnumEntry -> declaration.parentAsClass
            is IrSimpleFunction -> when {
                // For `@DartConstructor`s, we want to import the parent class of the companion object it's in.
                isDartConstructor -> declaration.parentAsClass.parentAsClass
                declaration.isStatic -> declaration.parentAsClass
                else -> {
                    val prop = declaration.correspondingProperty

                    when (prop?.isStatic) {
                        true -> declaration.parentAsClass
                        else -> prop ?: declaration
                    }
                }
            }

            else -> declaration
        }

        val isFunctionType = relevantDeclaration is IrClass && relevantDeclaration.defaultType.isFunction()

        if (isFunctionType && typeContext != null && typeContext !is SuperTypes) return

        val irFragmentOfDeclaration = when {
            // Function1, Function2, etc. are in an "external package fragment", but are in reality located
            // in the same file as the Function interface.
            isFunctionType -> irBuiltIns.functionClass.owner.getPackageFragment()
            else -> relevantDeclaration.getPackageFragment()
        }

        // We don't need to import Kotlin intrinsics such as ANDAND, OROR, EQEQ, etc.
        if (irFragmentOfDeclaration.packageFragmentDescriptor is IrBuiltinsPackageFragmentDescriptorImpl) return

        if (relevantDeclaration.name.isSpecial) return

        val annotatedLibraryUri = relevantDeclaration.annotatedDartLibrary

        // We don't need to import declarations defined in the same file, unless there's an `@DartLibrary` annotation.
        if (this == irFragmentOfDeclaration && annotatedLibraryUri == null) return

        val descriptor = relevantDeclaration.descriptor

        val libraryUri = annotatedLibraryUri ?: run {
            var module = descriptor.module

            // If the imported declaration is from our module, the library URI is just a relative path.
            if (module == currentFile.module.descriptor) {
                exportedPackageFragmentOf(descriptor)?.let { return@run it.relativeDartPath.toUriString() }

                // TODO: Make relativeDartPath work without IrFiles (#81)
                return@run irFragmentOfDeclaration.relativeDartPath.toUriString()
            }

            // At this point, the declaration is from a dependency and thus the module must be a
            // DotlinModule.
            module = module.dotlinModule ?: return
            val pkg = module.dartPackage

            val filePath = when (descriptor) {
                is DartDescriptor, is DartInteropDescriptor -> {
                    val fragment = exportedPackageFragmentOf(descriptor) ?: descriptor.dartPackageFragment
                    fragment.library.path.toUriString()
                }
                // Make filePath relative to packageRoot, example:
                // Package root: ${pkg.path}/lib/
                // File Dart path: ${pkg.path}/lib/somewhere/else.dart
                // Result: somewhere/else.dart
                else -> pkg.path.resolve(irFragmentOfDeclaration.dartPath)
                    .relativeTo(pkg.packagePath)
                    .toUriString()
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

context(DotlinLoweringContext)
private fun IrFile.exportedPackageFragmentOf(descriptor: DeclarationDescriptor): DartPackageFragmentDescriptor? {
    val ktFile = getKtFile() ?: return null
    val import = ktFile.importDirectives
        .associateWith { it.descriptor(bindingContext) }
        .entries
        .firstOrNull { (_, d) -> d == descriptor }
        ?.key
        ?: return null

    val importedFqName = import.importedFqName ?: return null

    // Not an export if the imported fqName is the same as that of the descriptor.
    if (importedFqName == descriptor.fqNameOrNull()) return null

    val packageFragment = module.descriptor.getPackage(importedFqName.parent())
        .fragments
        .firstIsInstanceOrNull<DartExportPackageFragmentDescriptor>()
        ?: return null

    return packageFragment.fragment
}

private fun Path.toUriString() = joinToString("/") { it.toString() }