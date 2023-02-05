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
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Analysis: Name Mangling")
class NameMangling : BaseTest {
    @Test
    fun `name clash`() = assertCompilesWithError(ErrorsDart.DART_NAME_CLASH) {
        kotlin(
            """
            fun main() {}

            @DartName("main")
            fun main2() {}
            """
        )
    }

    @Test
    fun `multiple name clashes`() = assertCompilesWithError(ErrorsDart.DART_NAME_CLASH) {
        kotlin(
            """
            fun main() {}

            @DartName("main")
            fun main2() {}

            @DartName("main")
            fun main3() {}
            """
        )
    }

    @Test
    fun `backing field name clash`() = assertCompilesWithError(ErrorsDart.DART_NAME_CLASH) {
        kotlin(
            """
            class Test {
                val property: Int = 0
                    get() = field
                private val `_${'$'}propertyBackingField` = 1
            }
            """
        )
    }

    @Test
    fun `property and function same name explicit clash`() = assertCompilesWithError(ErrorsDart.DART_NAME_CLASH) {
        kotlin(
            """
            fun test() {}

            val test = 3

            val `test${'$'}property` = 4
            """
        )
    }

    @Test
    fun `override with same name clash`() =
        assertCompilesWithError(ErrorsDart.DART_NAME_CLASH) {
            kotlin(
                """
                abstract class A {
                    fun execute(arg: Any?) {}
                }

                interface B<T> {
                    fun execute(arg: T)
                }

                class C : A(), B<C> {
                    override fun execute(arg: C) {}
                }
                """
            )
        }
}