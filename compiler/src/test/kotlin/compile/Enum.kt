/*
 * Copyright 2021 Wilko Manger
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

package compile

import BaseTest
import assertCompile
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@Disabled
@DisplayName("Compile: Enum")
class Enum : BaseTest {
    @Test
    fun `enum`() = assertCompile {
        kotlin(
            """
            enum class Test {
                ALPHA,
                BETA,
            }
            """
        )

        dart(
            """
            class Test extends ${'$'}Enum {
              const Test._(this.ordinal) : super();
              
              final int ordinal;
              
              static const ALPHA = Test._(0);
              static const BETA = Test._(1);
            }
            """
        )
    }

    @Test
    fun `enum with one extra value`() = assertCompile {
        kotlin(
            """
            enum class Test(val lowercase: String) {
                ALPHA("α"),
                BETA("β"),
            }
            """
        )

        dart(
            """
            class Test extends ${'$'}Enum {
              const Test._(this.ordinal) : super();
              
              final int ordinal;
              
              static const ALPHA = Test._(0);
              static const BETA = Test._(1);
            }
            """
        )
    }

    @Test
    fun `enum with two extra values`() = assertCompile {
        kotlin(
            """
            enum class Test(val lowercase: String, val uppercase: String) {
                ALPHA("α", "Α"),
                BETA("β", "Β"),
            }
            """
        )

        dart(
            """
            class Test extends ${'$'}Enum {
              const Test._(this.ordinal) : super();
              
              final int ordinal;
              
              static const ALPHA = Test._(0);
              static const BETA = Test._(1);
            }
            """
        )
    }
}