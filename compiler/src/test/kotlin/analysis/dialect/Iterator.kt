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
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.jetbrains.kotlin.diagnostics.Errors
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Analysis: Dialect: Iterator")
class Iterator : BaseTest {
    @Test
    fun `error if using Kotlin iterator methods`() = assertCompilesWithError(ErrorsDart.KOTLIN_ITERATOR_METHOD_USAGE) {
        kotlin(
            """
            fun main() {
                val iterator = arrayOf(1, 2).iterator()

                while (iterator.hasNext()) {
                    iterator.next()
                }
            }
            """
        )
    }

    @Test
    fun `error if using Kotlin iterator methods on Iterator subtype`() =
        assertCompilesWithError(ErrorsDart.KOTLIN_ITERATOR_METHOD_USAGE) {
            kotlin(
                """
                class TestIterator : Iterator<Int>() {
                    override fun moveNext() = false
                    override val current = 0
                }

                fun main() {
                    val iterator = TestIterator()
    
                    while (iterator.hasNext()) {
                        iterator.next()
                    }
                }
                """
            )
        }

    @Test
    fun `error if using 'operator' keyword on Kotlin iterator method`() =
        assertCompilesWithError(Errors.INAPPLICABLE_OPERATOR_MODIFIER) {
            kotlin(
                """
                class TestIterator {
                    operator fun hasNext(): Boolean = false
                    operator fun next(): Int = 0
                }
                """
            )
    }
}