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
import assertCanCompile
import assertCompilesWithError
import assertCompilesWithErrors
import assertCompilesWithWarning
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Analysis: Dialect: Throwing")
class Throwing : BaseTest {
    @Test
    fun `can throw non-Throwable`() = assertCanCompile {
        kotlin(
            """
            fun main() {
                val x = 11
                throw x
            }
            """
        )
    }

    @Test
    fun `can throw non-Throwable literal`() = assertCanCompile {
        kotlin(
            """
            fun main() {
                throw 10
            }
            """
        )
    }

    @Test
    fun `can catch dynamic`() = assertCanCompile {
        kotlin(
            """
            fun main() {
                try {} catch (e: dynamic) {}
            }
            """
        )
    }

    @Test
    fun `can catch Any`() = assertCanCompile {
        kotlin(
            """
            fun main() {
                try {} catch (e: Any) {}
            }
            """
        )
    }
}