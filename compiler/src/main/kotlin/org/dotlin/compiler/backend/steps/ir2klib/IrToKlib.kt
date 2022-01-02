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

import org.dotlin.compiler.backend.steps.src2ir.IrResult
import org.jetbrains.kotlin.backend.common.serialization.KlibIrVersion
import org.jetbrains.kotlin.backend.common.serialization.metadata.KlibMetadataMonolithicSerializer
import org.jetbrains.kotlin.backend.common.serialization.metadata.KlibMetadataVersion
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.KotlinLibraryVersioning
import org.jetbrains.kotlin.library.impl.BuiltInsPlatform
import org.jetbrains.kotlin.library.impl.buildKotlinLibrary
import java.io.File

fun writeToKlib(env: KotlinCoreEnvironment, config: CompilerConfiguration, irResult: IrResult, outputFile: File) {
    val serializedIr = DartIrModuleSerializer(
        messageLogger = config.get(IrMessageLogger.IR_MESSAGE_LOGGER) ?: IrMessageLogger.None,
        builtIns = irResult.module.irBuiltins,
    ).serializedIrModule(irResult.module)

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
        exportKDoc = false,
        skipExpects = true,
        allowErrorTypes = false
    ).serializeModule(irResult.module.descriptor)

    buildKotlinLibrary(
        linkDependencies = irResult.resolvedLibs.getFullList(),
        metadata = serializedMetadata,
        ir = serializedIr,
        versions = versions,
        output = outputFile.absolutePath,
        moduleName = irResult.module.name.asStringStripSpecialMarkers(),
        nopack = false,
        perFile = false, // TODO
        manifestProperties = null,
        dataFlowGraph = null,
        builtInsPlatform = BuiltInsPlatform.COMMON,
    )
}
