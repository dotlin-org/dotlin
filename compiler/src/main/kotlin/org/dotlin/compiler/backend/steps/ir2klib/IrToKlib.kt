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

package org.dotlin.compiler.backend.steps.ir2klib

import org.dotlin.compiler.backend.DartProject
import org.dotlin.compiler.backend.steps.src2ir.IrResult
import org.jetbrains.kotlin.backend.common.serialization.KlibIrVersion
import org.jetbrains.kotlin.backend.common.serialization.metadata.KlibMetadataMonolithicSerializer
import org.jetbrains.kotlin.backend.common.serialization.metadata.KlibMetadataVersion
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFileImpl
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.KotlinLibraryVersioning
import org.jetbrains.kotlin.library.impl.BuiltInsPlatform
import org.jetbrains.kotlin.library.impl.buildKotlinLibrary
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

fun writeToKlib(
    env: KotlinCoreEnvironment,
    config: CompilerConfiguration,
    irResult: IrResult,
    dartProject: DartProject
) {
    irResult.module.withFilePathsRelativeTo(dartProject.path) { module ->
        val serializedIr = DartIrModuleSerializer(
            messageLogger = config.get(IrMessageLogger.IR_MESSAGE_LOGGER) ?: IrMessageLogger.None,
            builtIns = module.irBuiltins,
        ).serializedIrModule(module)

        val metadataVersion = KlibMetadataVersion.INSTANCE

        val versions = KotlinLibraryVersioning(
            libraryVersion = null,
            compilerVersion = null, // TODO: get
            abiVersion = KotlinAbiVersion.CURRENT,
            metadataVersion = metadataVersion.toString(),
            irVersion = KlibIrVersion.INSTANCE.toString()
        )

        val serializedMetadata = KlibMetadataMonolithicSerializer(
            languageVersionSettings = config.languageVersionSettings,
            metadataVersion = metadataVersion,
            project = env.project,
            exportKDoc = true,
            skipExpects = true,
            allowErrorTypes = false
        ).serializeModule(irResult.module.descriptor)

        buildKotlinLibrary(
            linkDependencies = irResult.resolvedLibs.getFullList(),
            metadata = serializedMetadata,
            ir = serializedIr,
            versions = versions,
            output = dartProject.klibPath.absolutePathString(),
            moduleName = module.name.asStringStripSpecialMarkers(),
            nopack = true,
            perFile = false, // TODO
            manifestProperties = null,
            dataFlowGraph = null,
            builtInsPlatform = BuiltInsPlatform.COMMON,
        )
    }
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
private fun IrModuleFragment.withFilePathsRelativeTo(sourceRoot: Path, block: (IrModuleFragment) -> Unit) {
    // We want to serialize the file paths as relative to the source root.
    val originalFiles = files.toList()
    files.clear()
    originalFiles.mapTo(files) {
        IrFileImpl(
            fileEntry = it.fileEntry.let { entry ->
                object : IrFileEntry {
                    override val name = sourceRoot.relativize(Path(entry.name)).toString()
                    override val maxOffset = entry.maxOffset
                    override fun getColumnNumber(offset: Int) = entry.getColumnNumber(offset)
                    override fun getLineNumber(offset: Int) = entry.getLineNumber(offset)

                    override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int) =
                        entry.getSourceRangeInfo(beginOffset, endOffset)
                }
            },
            it.packageFragmentDescriptor,
            it.module
        ).apply {
            declarations.addAll(it.declarations)
            patchDeclarationParents(this)
            annotations = it.annotations
        }
    }

    block(this)

    // We put back the original files, so that there are no side effects to calling this function.
    files.apply {
        clear()
        addAll(originalFiles)
        forEach { patchDeclarationParents(it) }
    }
}