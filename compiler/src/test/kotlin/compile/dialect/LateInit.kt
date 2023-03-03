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

package compile.dialect

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Dialect: Lateinit")
class LateInit : BaseTest {
    @Test
    fun `lateinit on nullable`() = assertCompile {
        kotlin(
            """
            lateinit var x: String?
            """
        )

        dart(
            """
            late String? x;
            """
        )
    }

    @Test
    fun `lateinit on primitive`() = assertCompile {
        kotlin(
            """
            lateinit var x: Int
            """
        )

        dart(
            """
            late int x;
            """
        )
    }

    @Test
    fun `lateinit on type parameter with nullable upper bound`() = assertCompile {
        kotlin(
            """
            class Test<T> {
                lateinit var test: T
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;

            @sealed
            class Test<T> {
              @nonVirtual
              late T test;
            }
            """
        )
    }
}