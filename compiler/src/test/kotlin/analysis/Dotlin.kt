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

package analysis

import BaseTest
import assertCompilesWithError
import assertCompilesWithWarning
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Analysis: Dotlin")
class Dotlin : BaseTest {
    @Test
    fun `warning if declaring extension without @DartExtensionName in public package`() =
        assertCompilesWithWarning(ErrorsDart.EXTENSION_WITHOUT_EXPLICIT_DART_EXTENSION_NAME_IN_PUBLIC_PACKAGE) {
            isLibrary = true

            kotlin(
                """
                fun Int.negate() = -this
                """
            )
        }

    @Test
    fun `error if using @DartName on overridden method`() =
        assertCompilesWithError(ErrorsDart.DART_NAME_ON_OVERRIDE) {
            kotlin(
                """
                interface Test {
                    fun test() {}
                }

                class Test2 : Test {
                    @DartName("myTest")
                    override fun test() {}
                }
                """
            )
        }

    @Test
    fun `error if using @DartName on overridden property`() =
        assertCompilesWithError(ErrorsDart.DART_NAME_ON_OVERRIDE) {
            kotlin(
                """
                interface Test {
                    val test: Int
                }

                class Test2 : Test {
                    @DartName("myTest")
                    override val test: Int = 3
                }
                """
            )
        }

    @Test
    fun `error if using @DartIndex below zero`() =
        assertCompilesWithError(ErrorsDart.DART_INDEX_OUT_OF_BOUNDS) {
            kotlin(
                """
                fun process(@DartIndex(-1) first: Int, @DartIndex(1) second: Int) {}
                """
            )
        }

    @Test
    fun `error if using @DartIndex above parameter max index`() =
        assertCompilesWithError(ErrorsDart.DART_INDEX_OUT_OF_BOUNDS) {
            kotlin(
                """
                fun process(@DartIndex(2) first: Int, @DartIndex(1) second: Int) {}
                """
            )
        }

    @Test
    fun `error if using duplicate @DartIndex`() =
        assertCompilesWithError(ErrorsDart.DART_INDEX_CONFLICT) {
            kotlin(
                """
                fun process(@DartIndex(1) first: Int, @DartIndex(1) second: Int) {}
                """
            )
        }

    @Test
    fun `error if using @DartDifferentDefaultValue on parameter without default value`() =
        assertCompilesWithError(ErrorsDart.DART_DIFFERENT_DEFAULT_VALUE_ON_PARAMETER_WITHOUT_DEFAULT_VALUE) {
            kotlin(
                """
                fun process(@DartDifferentDefaultValue first: Int) {}
                """
            )
        }

    @Test
    fun `error if using @DartDifferentDefaultValue on parameter in non-external function`() =
        assertCompilesWithError(ErrorsDart.DART_DIFFERENT_DEFAULT_VALUE_ON_NON_EXTERNAL) {
            kotlin(
                """
                fun process(@DartDifferentDefaultValue first: Int = 3) {}
                """
            )
        }
}