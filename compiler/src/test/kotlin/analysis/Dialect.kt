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
import assertCanCompile
import assertCompilesWithError
import assertCompilesWithErrors
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Analysis: Dialect")
class Dialect : BaseTest {
    @Test
    fun `using Long literal on Int`() = assertCanCompile {
        kotlin(
            """
            fun main() {
                val x: Int = 9223372036854775807
            }
            """
        )
    }

    @Test
    fun `calling times() on Long literal`() = assertCanCompile {
        kotlin(
            """
            fun main() {
                val x: Int = 9223372036854775807 * 2
            }
            """
        )
    }

    @Test
    fun `error when using literal larger than Long on Int`() = assertCompilesWithErrors("INT_LITERAL_OUT_OF_RANGE") {
        kotlin(
            """
            fun main() {
                val x: Int = 100223372036854775807
            }
            """
        )
    }

    @Test
    fun `error when using String literal on Int`() = assertCompilesWithError("TYPE_MISMATCH") {
        kotlin(
            """
            fun main() {
                val x: Int = "ABC"
            }
            """
        )
    }

    @Disabled
    @Test
    fun `error when using Long`() = assertCompilesWithError("") {
        kotlin(
            """
            fun main() {
                val x = 9223372036854775807
            }
            """
        )
    }

    @Disabled
    @Test
    fun `error when using Char`() = assertCompilesWithError("") {
        kotlin(
            """
            fun main() {
                val x = 'a'
            }
            """
        )
    }

    @Disabled
    @Test
    fun `error when using Float`() = assertCompilesWithError("") {
        kotlin(
            """
            fun main() {
                val x = 0f
            }
            """
        )
    }
}