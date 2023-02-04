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

import org.dotlin.compiler.backend.DartProject
import org.jetbrains.kotlin.backend.common.serialization.metadata.impl.KlibMetadataDeserializedPackageFragmentsFactoryImpl
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ModuleCapability
import org.jetbrains.kotlin.descriptors.deserialization.PlatformDependentTypeTransformer
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.descriptors.konan.KlibModuleDescriptorFactory
import org.jetbrains.kotlin.descriptors.konan.KlibModuleOrigin
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.konan.impl.KlibMetadataModuleDescriptorFactoryImpl
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.util.DummyLogger
import kotlin.io.path.Path
import kotlin.io.path.exists

object DartPackageModuleResolver {
    fun resolve(
        project: DartProject,
        builtIns: DotlinBuiltIns,
        elementLocator: DartElementLocator,
    ): List<DotlinModule> {
        val packages = project.dependencies

        val klibs = resolveKlibs(
            paths = packages
                .map { it.klibPath }
                .filter { it.exists() },
            DummyLogger,
        ).getFullList()

        val factory = KlibMetadataModuleDescriptorFactoryImpl(
            // We'll never use this.
            descriptorFactory = object : KlibModuleDescriptorFactory {
                override fun createDescriptor(
                    name: Name,
                    storageManager: StorageManager,
                    builtIns: KotlinBuiltIns,
                    origin: KlibModuleOrigin,
                    customCapabilities: Map<ModuleCapability<*>, Any?>
                ): ModuleDescriptorImpl = throw UnsupportedOperationException()

                override fun createDescriptorAndNewBuiltIns(
                    name: Name,
                    storageManager: StorageManager,
                    origin: KlibModuleOrigin,
                    customCapabilities: Map<ModuleCapability<*>, Any?>
                ): ModuleDescriptorImpl = throw UnsupportedOperationException()
            },
            packageFragmentsFactory = KlibMetadataDeserializedPackageFragmentsFactoryImpl(),
            flexibleTypeDeserializer = DynamicTypeDeserializer,
            platformDependentTypeTransformer = PlatformDependentTypeTransformer.None,
        )

        return packages.map { pkg ->
            DotlinModule(
                project,
                pkg,
                elementLocator,
                builtIns,
                klib = klibs.firstOrNull { Path(it.libraryFile.path) == pkg.klibPath },
                factory,
                LockBasedStorageManager("DartPackageModuleResolver"),
            )
        }
    }
}