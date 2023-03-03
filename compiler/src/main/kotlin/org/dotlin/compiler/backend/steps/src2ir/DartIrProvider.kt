package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.DartPackage
import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.descriptors.DartPackageFragmentDescriptor
import org.dotlin.compiler.backend.descriptors.DartSyntheticDescriptor
import org.dotlin.compiler.backend.isCurrent
import org.dotlin.compiler.dart.element.DartLibraryElement
import org.jetbrains.kotlin.backend.common.serialization.DescriptorByIdSignatureFinderImpl
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.linkage.IrProvider
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.psi2ir.generators.DeclarationStubGeneratorImpl
import org.jetbrains.kotlin.resolve.descriptorUtil.module

/**
 * Providers IR elements from Dart descriptors.
 */
class DartIrProvider(
    private val module: ModuleDescriptor,
    symbolTable: SymbolTable,
    irBuiltIns: IrBuiltIns,
) : IrProvider {
    private val descriptorFinder = DescriptorByIdSignatureFinderImpl(
        module,
        DotlinDescriptorBasedMangler,
    )

    private val stubGenerator = DeclarationStubGeneratorImpl(
        module,
        symbolTable,
        irBuiltIns,
        descriptorFinder,
    )

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun getDeclaration(symbol: IrSymbol): IrDeclaration? {
        val descriptor = symbol.descriptor

        if (descriptor.module != module) return null

        if (descriptor !is DartDescriptor && descriptor !is DartSyntheticDescriptor) return null

        val declaration = stubGenerator.run {
            when (symbol) {
                is IrClassSymbol -> generateClassStub(descriptor as ClassDescriptor)
                is IrPropertySymbol -> generatePropertyStub(descriptor as PropertyDescriptor)
                is IrConstructorSymbol -> generateConstructorStub(descriptor as ClassConstructorDescriptor)
                is IrFunctionSymbol -> generateFunctionStub(descriptor as FunctionDescriptor)
                is IrEnumEntrySymbol -> generateEnumEntryStub(descriptor as ClassDescriptor)
                is IrTypeAliasSymbol -> generateTypeAliasStub(descriptor as TypeAliasDescriptor)
                else -> error("Unexpected symbol type: ${symbol::class.simpleName} (sig: ${symbol.signature})")
            }
        }

        return declaration
    }
}

// TODO: Remove when #81 is completed.
@Suppress("FunctionName")
fun DartIrFile(packageFragment: DartPackageFragmentDescriptor) =
    IrFileImpl(
        DartIrFileEntry(packageFragment.library, packageFragment.context.pkg),
        packageFragment,
    )

private class DartIrFileEntry(library: DartLibraryElement, dartPackage: DartPackage) : IrFileEntry {
    override val maxOffset: Int = UNDEFINED_OFFSET
    override val name: String = library.path.let {
        when {
            // If it's the current project, we need to add the packagePath as a root, because it's
            // serialized without.
            dartPackage.isCurrent() -> dartPackage.packagePath.resolve(it)
            else -> it
        }.toString()
    }

    override fun getColumnNumber(offset: Int): Int =
        throw UnsupportedOperationException("Column number not available")

    override fun getLineNumber(offset: Int): Int =
        throw UnsupportedOperationException("Line number not available")

    override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int): SourceRangeInfo =
        throw UnsupportedOperationException("Source range info not available")
}

val IrFile.isDartFile: Boolean
    get() = fileEntry is DartIrFileEntry