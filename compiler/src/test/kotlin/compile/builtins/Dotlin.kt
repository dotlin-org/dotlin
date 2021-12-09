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

package compile.builtins

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Built-ins: Dotlin")
class Dotlin : BaseTest {
    @Test
    fun `@DartName`() = assertCompile {
        kotlin(
            """
            @DartName("Dar")
            class Kot

            fun main() {
                Kot()
            }
            """
        )

        dart(
            """
            class Dar {
              Dar() : super();
            }
            
            void main() {
              Dar();
            }
            """
        )
    }

    @Test
    fun `@DartGetter`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            class Hobbit {
                @DartGetter
                fun isProudfoot(): Boolean = true
            }

            fun main() {
                Hobbit().isProudfoot()
            }
            """
        )

        dart(
            """
            class Hobbit {
              Hobbit() : super();
              bool get isProudfoot {
                return true;
              }
            }

            void main() {
              Hobbit().isProudfoot;
            }
            """
        )
    }

    @Test
    fun `@DartGetter override`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            open class Hobbit {
                @DartGetter
                open fun isProudfoot(): Boolean = false
            }

            class Proudfoot : Hobbit() {
                override fun isProudfoot(): Boolean = true
            }

            fun main() {
                Proudfoot().isProudfoot()
            }
            """
        )

        dart(
            """
            class Hobbit {
              Hobbit() : super();
              bool get isProudfoot {
                return false;
              }
            }

            class Proudfoot extends Hobbit {
              Proudfoot() : super();
              @override
              bool get isProudfoot {
                return true;
              }
            }

            void main() {
              Proudfoot().isProudfoot;
            }
            """
        )
    }

    @Test
    fun `@DartBuiltIn`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartBuiltIn
            class Test

            fun main() {
                Test()
            }
            """
        )

        dart(
            """
            void main() {
              Test();
            }
            """
        )
    }
}