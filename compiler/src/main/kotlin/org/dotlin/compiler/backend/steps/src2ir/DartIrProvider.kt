package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.DartPackage
import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.descriptors.DartPackageFragmentDescriptor
import org.dotlin.compiler.backend.isCurrent
import org.dotlin.compiler.dart.element.DartLibraryElement
import org.jetbrains.kotlin.backend.common.serialization.DescriptorByIdSignatureFinderImpl
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.SourceRangeInfo
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerDesc
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.linkage.IrProvider
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.psi2ir.generators.DeclarationStubGeneratorImpl
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

/**
 * Providers IR elements from Dart descriptors.
 */
class DartIrProvider(
    module: ModuleDescriptor,
    symbolTable: SymbolTable,
    irBuiltIns: IrBuiltIns,
    private val irModuleFragment: IrModuleFragment?,
) : IrProvider {
    private val filesByLibrary = mutableMapOf<DartLibraryElement, IrFileImpl>()

    private val descriptorFinder = DescriptorByIdSignatureFinderImpl(
        module,
        JsManglerDesc, // TODO: JS reference
    )

    private val stubGenerator = DeclarationStubGeneratorImpl(
        module,
        symbolTable,
        irBuiltIns,
        descriptorFinder,
    )

    override fun getDeclaration(symbol: IrSymbol): IrDeclaration? {
        val descriptor = descriptorFinder.findDescriptorBySignature(symbol.signature!!)

        if (descriptor !is DartDescriptor) return null

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

        // Generated top-level IR elements by default have an IrExternalPackageFragment parent,
        // we want this to be an IrFile.
        descriptor.containingDeclaration.safeAs<DartPackageFragmentDescriptor>()?.let {
            filesByLibrary.computeIfAbsent(it.library) { library ->
                IrFileImpl(
                    DartIrFileEntry(library, it.pkg),
                    packageFragmentDescriptor = it
                )
            }.apply {
                addChild(declaration)
                irModuleFragment?.addFile(this)
            }
        }

        return declaration
    }

    fun setModuleFiles(irModuleFragment: IrModuleFragment) {
        require(this.irModuleFragment == null) { "Modules are already set" }
        filesByLibrary.values.forEach { irModuleFragment.addFile(it) }
    }

    private fun IrModuleFragment.addFile(irFile: IrFileImpl) {
        irFile.module = this
        files.add(irFile)
    }
}

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