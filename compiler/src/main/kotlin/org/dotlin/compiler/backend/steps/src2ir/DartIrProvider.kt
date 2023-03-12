package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.descriptors.DartInteropDescriptor
import org.jetbrains.kotlin.backend.common.serialization.DescriptorByIdSignatureFinderImpl
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.linkage.IrProvider
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.referenceClassifier
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

        if (descriptor !is DartDescriptor && descriptor !is DartInteropDescriptor) return null

        val declaration = stubGenerator.run {
            when (symbol) {
                is IrClassSymbol -> generateClassStub(descriptor as ClassDescriptor).apply {
                    // We need to reference classifiers used in super types ourselves, otherwise we still have
                    // unbound symbols that can be referenced.
                    // TODO: This might happen for all IrTypes, if so do this everywhere with a generic solution.
                    superTypes
                        .mapNotNull { it.originalKotlinType?.constructor?.declarationDescriptor }
                        .forEach { symbolTable.referenceClassifier(it) }
                }
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