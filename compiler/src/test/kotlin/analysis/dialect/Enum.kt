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
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.DUPLICATE_ENUM_MEMBER_NAME
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.VAR_IN_ENUM
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Analysis: Dialect: Enum")
class Enum : BaseTest {
    @Test
    fun `error if using var in enum`() = assertCompilesWithError(VAR_IN_ENUM) {
        kotlin(
            """
            enum class Test(var x: Int) {
                alpha(30),
                beta(50),
            }
            """
        )
    }

    @Test
    fun `error if duplicate enum entry class member name`() = assertCompilesWithError(DUPLICATE_ENUM_MEMBER_NAME) {
        kotlin(
            """
            enum class Test(val x: Int) {
                alpha(30) {
                    val y: Int = 30  
                },
                beta(50) {
                    val y: Int = 50
                }
            }
            """
        )
    }
}