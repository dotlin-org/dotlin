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
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Dialect: Const Inline")
class ConstInline : BaseTest {
    @Test
    fun `error if const inline function has multiple returns`() =
        assertCompilesWithError(ErrorsDart.CONST_INLINE_FUNCTION_WITH_MULTIPLE_RETURNS) {
            kotlin(
                """
                class Test const constructor(val message: String)
    
                const inline fun test(): Test {
                    if (true) {
                        return Test("asd")
                    } else {
                        return Test("wasd")
                    }
                }
                """
            )
        }

    @Test
    fun `error if const inline function returns non-const value`() =
        assertCompilesWithError(ErrorsDart.CONST_INLINE_FUNCTION_RETURNS_NON_CONST) {
            kotlin(
                """
                class Test constructor(val message: String)
    
                const inline fun test(): Test = Test("asd")
                """
            )
        }

    @Test
    fun `error if const inline function has invalid statement`() =
        assertCompilesWithError(ErrorsDart.CONST_INLINE_FUNCTION_HAS_INVALID_STATEMENT) {
            kotlin(
                """
                class Test const constructor(val message: String)
    
                const inline fun test(): Test {
                    try {
                        return Test("test1")
                    } catch (something: String) {
                        return Test("test")
                    }
                }
                """
            )
        }

    @Test
    fun `error if const inline function has const variable with non-const value`() =
        assertCompilesWithError(ErrorsDart.CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE) {
            kotlin(
                """
                class Test const constructor(val message: String)
    
                val y = "Y"

                const inline fun test(): Test {
                    const val x = "X + ${'$'}y"
                    return Test("something")
                }
                """
            )
        }

    @Test
    fun `error if @const used on non-const function for local const val`() =
        assertCompilesWithError(ErrorsDart.CONST_WITH_NON_CONST) {
            kotlin(
                """
                fun create() = 0

                fun test() {
                    const val t = @const create()
                }
                """
            )
        }

    @Test
    fun `error if const inline function called with non-const values`() =
        assertCompilesWithError(ErrorsDart.CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE) {
            kotlin(
                """
                class Test const constructor(val message: String)
    
                const inline fun test(x: String, y: String): Test {
                    return Test("asd${'$'}x${'$'}y")
                }

                fun main() {
                    val nonConst = "asd"
                    const val z = test("abc", nonConst)
                }
                """
            )
        }

    @Test
    fun `error if @const used on non-const function`() =
        assertCompilesWithError(ErrorsDart.CONST_WITH_NON_CONST) {
            kotlin(
                """
                fun create() = 0

                fun test() {
                    val t = @const create()
                }
                """
            )
        }
}