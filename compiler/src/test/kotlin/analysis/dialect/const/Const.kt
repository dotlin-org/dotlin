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

package analysis.dialect.const

import BaseTest
import assertCompilesWithError
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.*
import org.jetbrains.kotlin.diagnostics.Errors.WRONG_MODIFIER_TARGET
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Dialect: Const")
class Const : BaseTest {
    @Test
    fun `error if non-const constructor called for global const val`() =
        assertCompilesWithError(CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE) {
            kotlin(
                """
                class Test constructor(val message: String)
    
                const val t = Test("Test")
                """
            )
        }

    @Test
    fun `error if non-const constructor called for local const val`() =
        assertCompilesWithError(CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE) {
            kotlin(
                """
                class Test constructor(val message: String)
    
                fun test() {
                    const val t = Test("Test")
                }
                """
            )
        }

    @Test
    fun `error if @const used on non-constructor call expression`() =
        assertCompilesWithError(ONLY_FUNCTION_AND_CONSTRUCTOR_CALLS_CAN_BE_CONST) {
            kotlin(
                """
                fun test() {
                    const val t = @const 0
                }
                """
            )
        }

    @Test
    fun `error if @const used on non-const constructor for local const val`() =
        assertCompilesWithError(CONST_WITH_NON_CONST) {
            kotlin(
                """
                class Test constructor(val message: String)

                fun test() {
                    const val t = @const Test("asd")
                }
                """
            )
        }

    @Test
    fun `error if @const used on non-const constructor`() =
        assertCompilesWithError(CONST_WITH_NON_CONST) {
            kotlin(
                """
                class Test constructor(val message: String)

                fun test() {
                    val t = @const Test("asd")
                }
                """
            )
        }

    @Test
    fun `error if const constructor parameter with default value is not const`() =
        assertCompilesWithError(NON_CONSTANT_DEFAULT_VALUE_IN_CONST_FUNCTION) {
            kotlin(
                """
                class Test

                class Testable const constructor(val test: Test = Test())
                """
            )
    }

    @Test
    fun `error if const lambda literal accessing non-global closure variable`() =
        assertCompilesWithError(CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE) {
            kotlin(
                """
                class Zen const constructor(private val maintainMotorcycle: () -> Int)

                fun main() {
                    val someValue = 1024

                    const val zen = Zen { someValue }
                }
                """
            )
        }

    @Test
    fun `error if const lambda literal accessing non-global closure anonymous object`() =
        assertCompilesWithError(CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE) {
            kotlin(
                """
                class Zen const constructor(private val maintainMotorcycle: () -> String)

                fun main() {
                    val someObject = object {
                        fun maintain() {}
                    }

                    const val zen = Zen {
                        someObject
                        "Quality"
                    }
                }
                """
            )
        }

    @Test
    fun `error if const constructor called with non-const values`() =
        assertCompilesWithError(CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE) {
            kotlin(
                """
                class Test const constructor(val message: String)

                fun main() {
                    val nonConst = "asd"
                    const val z = Test(nonConst)
                }
                """
            )
        }

    @Test
    fun `error if using non-const argument in @const function call`() =
        assertCompilesWithError(CONST_WITH_NON_CONST) {
            kotlin(
                """
                fun main() {
                    val x = 30
                    val array = @const arrayOf(0, 1, x)
                }
                """
            )
    }

    @Test
    fun `error if using const keyword on non-@DartConstructor member function`() =
        assertCompilesWithError(WRONG_MODIFIER_TARGET) {
            kotlin(
                """
                external class Test {
                    const fun create(): Test
                }
                """
            )
        }

    @Test
    fun `error if using const keyword without inline on top level function`() =
        assertCompilesWithError(INAPPLICABLE_CONST_FUNCTION_MODIFIER) {
            kotlin(
                """
                const fun create(): Int = 0
                """
            )
        }
}