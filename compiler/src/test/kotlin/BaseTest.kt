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

import org.dotlin.compiler.KotlinToDartCompiler
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayNameGeneration

@DisplayNameGeneration(FunctionDisplayNameGenerator::class)
interface BaseTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun compileStdlib() {
            KotlinToDartCompiler.compile(
                sourceRoot = stdlibSrc,
                output = stdlibKlib,
                dependencies = emptySet(),
                isKlib = true,
                isPublicPackage = true
            )
        }
    }
}