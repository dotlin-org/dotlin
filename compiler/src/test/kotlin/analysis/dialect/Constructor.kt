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

package analysis.dialect

import BaseTest
import assertCompilesWithError
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.DART_CONSTRUCTOR_WRONG_RETURN_TYPE
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.DART_CONSTRUCTOR_WRONG_TARGET
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Analysis: Dialect: Constructor")
class Constructor : BaseTest {
    @Test
    fun `error if using @DartConstructor on non-external`() =
        assertCompilesWithError(DART_CONSTRUCTOR_WRONG_TARGET) {
            kotlin(
                """
                class Test {
                    companion object {
                        @DartConstructor
                        fun test(): Test = Test()
                    }
                }
                """
            )
        }

    @Test
    fun `error if using @DartConstructor not in companion object`() =
        assertCompilesWithError(DART_CONSTRUCTOR_WRONG_TARGET) {
            kotlin(
                """
                class Test {
                    @DartConstructor
                    fun test(): Int = 3
                }
                """
            )
        }

    @Test
    fun `error if using @DartConstructor with wrong return type`() =
        assertCompilesWithError(DART_CONSTRUCTOR_WRONG_RETURN_TYPE) {
            kotlin(
                """
                external class Test {
                    companion object {
                        @DartConstructor
                        external fun create(): Int
                    }
                }
                """
            )
        }

    @Test
    fun `error if using @DartConstructor with no type arguments when it was expected`() =
        assertCompilesWithError(DART_CONSTRUCTOR_WRONG_RETURN_TYPE) {
            kotlin(
                """
                interface Marker
    
                external class Test<A, B : Marker> {
                    companion object {
                        @DartConstructor
                        external fun create(a: Int, b: Int): Test<Int, Marker>
                    }
                }
                """
            )
    }

    @Test
    fun `error if using @DartConstructor with different amount of type arguments`() =
        assertCompilesWithError(DART_CONSTRUCTOR_WRONG_RETURN_TYPE) {
            kotlin(
                """
                interface Marker
    
                external class Test<A, B : Marker> {
                    companion object {
                        @DartConstructor
                        external fun <A> create(a: A, b: Int): Test<A, Marker>
                    }
                }
                """
            )
        }

    @Test
    fun `error if using @DartConstructor with type argument bound that is valid but not exactly equal`() =
        assertCompilesWithError(DART_CONSTRUCTOR_WRONG_RETURN_TYPE) {
            kotlin(
                """
                interface Marker

                interface Submarker : Marker
    
                external class Test<A, B : Marker> {
                    companion object {
                        @DartConstructor
                        external fun <A, B : Submarker> create(a: A, b: B): Test<A, B>
                    }
                }
                """
            )
        }
}