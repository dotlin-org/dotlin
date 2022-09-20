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

package compile

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Name Mangling")
class NameMangling : BaseTest {
    @Test
    fun `top-level property and method with the same name`() = assertCompile {
        kotlin(
            """
            fun test() {}

            val test = 3
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void test() {}
            final int test${'$'}property = 3;
            """
        )
    }

    @Test
    fun `class with property and method with the same name`() = assertCompile {
        kotlin(
            """
            class Example {
                fun test() {}

                val test = 3
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Example {
              @nonVirtual
              void test() {}
              @nonVirtual
              final int test${'$'}property = 3;
            }
            """
        )
    }

    @Test
    fun `top-level property and class method with the same name`() = assertCompile {
        kotlin(
            """
            val test = 3

            class Example {
                fun test() {}
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            final int test = 3;

            @sealed
            class Example {
              @nonVirtual
              void test() {}
            }
            """
        )
    }

    @Test
    fun `local variable and method with the same name`() = assertCompile {
        kotlin(
            """
            class Example {
                fun test() {}

                fun something() {
                    val test = 0
                }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Example {
              @nonVirtual
              void test() {}
              @nonVirtual
              void something() {
                final int test = 0;
              }
            }
            """
        )
    }
}