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

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.protobuf.ProtoBuf
import org.dotlin.compiler.backend.DartPackage
import org.dotlin.compiler.backend.DartProject
import org.dotlin.compiler.backend.bin.DotlinGenerator
import org.dotlin.compiler.dart.element.*
import kotlin.io.path.exists
import kotlin.io.path.readBytes

object DartPackageDeserializer {
    fun deserialize(project: DartProject, pkg: DartPackage, locator: DartElementLocator) =
        deserialize(project, listOf(pkg), locator)

    @OptIn(ExperimentalSerializationApi::class)
    fun deserialize(
        project: DartProject,
        packages: Iterable<DartPackage>,
        locator: DartElementLocator,
    ): List<DartPackageElement?> {
        generateElements(project, packages)

        val protobuf = ProtoBuf {
            serializersModule = SerializersModule {
                DartLibraryElement.serializer().registerOnSerialize(locator)
                DartCompilationUnitElement.serializer().registerOnSerialize(locator)
                DartClassElement.serializer().registerOnSerialize(locator)
                DartFieldElement.serializer().registerOnSerialize(locator)
                DartFunctionElement.serializer().registerOnSerialize(locator)
                DartConstructorElement.serializer().registerOnSerialize(locator)
            }
        }

        return packages.map {
            val bytes = it.dlibPath.readBytes()
            // TODO: Use MessageCollector
            println("Deserializing Dart package: ${it.name} (${it.path})")
            when {
                bytes.isEmpty() -> null
                else -> protobuf.decodeFromByteArray<DartPackageElement>(bytes)
            }
        }
    }

    private fun generateElements(project: DartProject, packages: Iterable<DartPackage>) {
        val paths = packages.map {
            // TODO: Only skip if Dotlin compiler version matches
            when (it.dlibPath.exists()) {
                //true -> null // TODO: Uncomment
                else -> it.path to it.packagePath
            }
        }

        if (paths.isNotEmpty()) {
            runBlocking {
                DotlinGenerator.generate(paths, workingDirectory = project.path)
            }
        }
    }
}