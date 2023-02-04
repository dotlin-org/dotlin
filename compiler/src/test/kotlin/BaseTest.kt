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

import kotlinx.coroutines.runBlocking
import org.dotlin.compiler.KotlinToDartCompiler
import org.dotlin.compiler.backend.bin.dart
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayNameGeneration
import java.nio.file.Path

@DisplayNameGeneration(FunctionDisplayNameGenerator::class)
interface BaseTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun compileStdlib() {
            println("Compiling stdlib..")
            KotlinToDartCompiler.compile(stdlibPath)
        }

        @BeforeAll
        @JvmStatic
        fun runDartPubGet() {
            // We run "dart pub get" once, to create the .pub-cache with the default dependencies for test projects.
            // In [DartTestProject], we create the pubspec.lock and package_config.json ourselves, to prevent
            // a "pub get" for every test.

            val project = object : DartTestProject() {
                override fun kotlin(kotlin: String, path: Path?) { /* Nothing. */ }
                override fun dart(dart: String, path: Path?) { /* Nothing. */ }
            }.apply {
                writeConfigFiles()
            }

            println("Running 'dart pub get' in ${project.path}")

            runBlocking { dart.pub.get(project.path) }
        }
    }
}