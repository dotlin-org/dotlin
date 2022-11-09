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
import assertCompilesWithWarning
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Analysis: Dialect: Operators")
class Operators : BaseTest {
    @Test
    fun `error when defining set operator that does not have the return type of its value`() =
        assertCompilesWithError(ErrorsDart.WRONG_SET_OPERATOR_RETURN_TYPE) {
            kotlin(
                """
                class Test {
                    operator fun set(index: Int, value: Boolean) {}
                }
                """
            )
        }

    @Test
    fun `warning when defining set operator that does not return its value`() =
        assertCompilesWithWarning(ErrorsDart.WRONG_SET_OPERATOR_RETURN) {
            kotlin(
                """
                class Test {
                    operator fun set(index: Int, value: Boolean): Boolean = false
                }
                """
            )
        }
}