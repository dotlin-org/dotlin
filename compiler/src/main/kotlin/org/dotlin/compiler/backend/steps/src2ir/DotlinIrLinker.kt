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

package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.DotlinIrMangler
import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.descriptors.DartPackageFragmentDescriptor
import org.dotlin.compiler.dart.element.DartLibraryElement
import org.jetbrains.kotlin.backend.common.overrides.FakeOverrideBuilder
import org.jetbrains.kotlin.backend.common.serialization.*
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData.SymbolKind.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.SourceRangeInfo
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsManglerDesc
import org.jetbrains.kotlin.ir.builders.TranslationPluginContext
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.library.IrLibrary
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.containsErrorCode
import org.jetbrains.kotlin.psi2ir.generators.DeclarationStubGeneratorImpl
import org.jetbrains.kotlin.utils.addToStdlib.cast

class DotlinIrLinker(
    currentModule: ModuleDescriptor?,
    logger: IrMessageLogger,
    builtIns: IrBuiltIns,
    symbolTable: SymbolTable,
    override val translationPluginContext: TranslationPluginContext?,
) : KotlinIrLinker(currentModule, logger, builtIns, symbolTable, emptyList()) {
    override val fakeOverrideBuilder = FakeOverrideBuilder(
        this,
        symbolTable,
        DotlinIrMangler,
        IrTypeSystemContextImpl(builtIns),
        friendModules = emptyMap()
    )

    override fun createModuleDeserializer(
        moduleDescriptor: ModuleDescriptor,
        klib: KotlinLibrary?,
        strategyResolver: (String) -> DeserializationStrategy
    ): IrModuleDeserializer = DotlinModuleDeserializer(
        moduleDescriptor,
        klib ?: EmptyIrLibrary,
        strategyResolver,
        libraryAbiVersion = klib?.versions?.abiVersion ?: KotlinAbiVersion.CURRENT,
        allowErrorCode = klib?.containsErrorCode ?: false,
    )

    override fun isBuiltInModule(moduleDescriptor: ModuleDescriptor) =
        moduleDescriptor === moduleDescriptor.builtIns.builtInsModule

    private inner class DotlinModuleDeserializer(
        module: ModuleDescriptor,
        klib: IrLibrary,
        strategyResolver: (String) -> DeserializationStrategy,
        libraryAbiVersion: KotlinAbiVersion,
        allowErrorCode: Boolean,
    ) : BasicIrModuleDeserializer(
        this,
        module,
        klib,
        strategyResolver,
        libraryAbiVersion,
        allowErrorCode
    ) {
        private val filesByLibrary = mutableMapOf<DartLibraryElement, IrFile>()

        private val descriptorFinder = DescriptorByIdSignatureFinderImpl(
            module,
            JsManglerDesc, // TODO: JS reference
        )

        private val stubGenerator = DeclarationStubGeneratorImpl(
            module,
            symbolTable,
            moduleFragment.irBuiltins,
            descriptorFinder,
        )

        override fun contains(idSig: IdSignature): Boolean = super.contains(idSig) // TODO

        override fun tryDeserializeIrSymbol(idSig: IdSignature, symbolKind: BinarySymbolData.SymbolKind): IrSymbol? {
            val deserialized = super.tryDeserializeIrSymbol(idSig, symbolKind)
            if (deserialized != null) return deserialized

            val descriptor = descriptorFinder.findDescriptorBySignature(idSig)

            if (descriptor !is DartDescriptor) return null

            val declaration = stubGenerator.run {
                when (symbolKind) {
                    CLASS_SYMBOL -> generateClassStub(descriptor as ClassDescriptor)
                    PROPERTY_SYMBOL -> generatePropertyStub(descriptor as PropertyDescriptor)
                    FUNCTION_SYMBOL -> generateFunctionStub(descriptor as FunctionDescriptor)
                    CONSTRUCTOR_SYMBOL -> generateConstructorStub(descriptor as ClassConstructorDescriptor)
                    ENUM_ENTRY_SYMBOL -> generateEnumEntryStub(descriptor as ClassDescriptor)
                    TYPEALIAS_SYMBOL -> generateTypeAliasStub(descriptor as TypeAliasDescriptor)
                    else -> error("Unexpected kind: $symbolKind (sig: $idSig)")
                }
            }

            declaration.parent = descriptor.containingDeclaration.cast<DartPackageFragmentDescriptor>().let {
                filesByLibrary.computeIfAbsent(it.library) { library ->
                    IrFileImpl(
                        DartIrFileEntry(library),
                        packageFragmentDescriptor = it
                    )
                }
            }

            return declaration.symbol
        }
    }
}

private object EmptyIrLibrary : IrLibrary {
    private val emptyByteArray = ByteArray(0)

    override val dataFlowGraph: ByteArray? = null
    override fun bodies(fileIndex: Int): ByteArray = emptyByteArray
    override fun body(index: Int, fileIndex: Int): ByteArray = emptyByteArray
    override fun debugInfo(index: Int, fileIndex: Int): ByteArray? = null
    override fun declarations(fileIndex: Int): ByteArray = emptyByteArray
    override fun file(index: Int): ByteArray = emptyByteArray
    override fun fileCount(): Int = 0
    override fun irDeclaration(index: Int, fileIndex: Int): ByteArray = emptyByteArray
    override fun signature(index: Int, fileIndex: Int): ByteArray = emptyByteArray
    override fun signatures(fileIndex: Int): ByteArray = emptyByteArray
    override fun string(index: Int, fileIndex: Int): ByteArray = emptyByteArray
    override fun strings(fileIndex: Int): ByteArray = emptyByteArray
    override fun type(index: Int, fileIndex: Int): ByteArray = emptyByteArray
    override fun types(fileIndex: Int): ByteArray = emptyByteArray
}

private class DartIrFileEntry(private val library: DartLibraryElement) : IrFileEntry {
    override val maxOffset: Int = UNDEFINED_OFFSET
    override val name: String = library.path.toString()

    override fun getColumnNumber(offset: Int): Int =
        throw UnsupportedOperationException("Column number not available")

    override fun getLineNumber(offset: Int): Int =
        throw UnsupportedOperationException("Line number not available")

    override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int): SourceRangeInfo =
        throw UnsupportedOperationException("Source range info not available")
}