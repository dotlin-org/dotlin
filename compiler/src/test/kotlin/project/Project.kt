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

package project

import BaseTest
import assertCanCompile
import org.dotlin.compiler.backend.bin.DotlinGenerator
import org.dotlin.compiler.backend.util.toPosixString
import org.junit.jupiter.api.Test
import stdlibPath
import kotlin.io.path.relativeTo


class Project : BaseTest {
    @Test
    fun `relative path pubspec dependencies`() = assertCanCompile {
        // language=yaml
        pubspec =
            """
            name: $name
            version: 1.0.0
            
            environment:
              sdk: '>=2.18.0 <3.0.0'
    
            publish_to: none
    
            dependencies:
              dotlin:
                path: ${stdlibPath.toRealPath().relativeTo(path).toPosixString()}
                
            dev_dependencies:
              dotlin_generator:
                path: ${DotlinGenerator.projectPath.toRealPath().relativeTo(path).toPosixString()}
            """.trimIndent()

        // pubspec.lock should be clear to trigger a `dart pub get`.
        pubspecLock = ""

        kotlin(
            """
            fun main() {}
            """
        )
    }
}