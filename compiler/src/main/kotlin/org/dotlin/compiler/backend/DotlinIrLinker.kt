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

package org.dotlin.compiler.backend

import org.jetbrains.kotlin.backend.common.overrides.FakeOverrideBuilder
import org.jetbrains.kotlin.backend.common.serialization.BasicIrModuleDeserializer
import org.jetbrains.kotlin.backend.common.serialization.DeserializationStrategy
import org.jetbrains.kotlin.backend.common.serialization.IrModuleDeserializer
import org.jetbrains.kotlin.backend.common.serialization.KotlinIrLinker
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.TranslationPluginContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.library.IrLibrary
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.containsErrorCode

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
    ): IrModuleDeserializer =
        DartModuleDeserializer(
            moduleDescriptor,
            klib ?: error("klib cannot be null"),
            strategyResolver,
            libraryAbiVersion = klib.versions.abiVersion ?: KotlinAbiVersion.CURRENT,
            allowErrorCode = klib.containsErrorCode,
        )

    override fun isBuiltInModule(moduleDescriptor: ModuleDescriptor) =
        moduleDescriptor === moduleDescriptor.builtIns.builtInsModule

    private inner class DartModuleDeserializer(
        moduleDescriptor: ModuleDescriptor,
        klib: IrLibrary,
        strategyResolver: (String) -> DeserializationStrategy,
        libraryAbiVersion: KotlinAbiVersion,
        allowErrorCode: Boolean,
    ) : BasicIrModuleDeserializer(
        this,
        moduleDescriptor,
        klib,
        strategyResolver,
        libraryAbiVersion,
        allowErrorCode
    )
}