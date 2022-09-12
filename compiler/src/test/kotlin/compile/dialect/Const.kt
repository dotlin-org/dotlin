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

@DisplayName("Compile: Dialect: Const")
class Const : BaseTest {
    @Test
    fun `const constructor`() = assertCompile {
        kotlin(
            """
            class Test const constructor()

            fun main() {
                Test()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              const Test() : super();
            }

            void main() {
              Test();
            }
            """
        )
    }

    @Test
    fun `const constructor call`() = assertCompile {
        kotlin(
            """
            class Test const constructor()

            fun main() {
                @const Test()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              const Test() : super();
            }

            void main() {
              const Test();
            }
            """
        )
    }

    @Test
    fun `global const variable`() = assertCompile {
        kotlin(
            """
            class Test const constructor(val message: String)

            const val t = Test("Test")
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';
            
            @sealed
            class Test {
              const Test(this.message) : super();
              @nonVirtual
              final String message;
            }
            
            const Test t = Test('Test');
            """
        )
    }

    @Test
    fun `local const variable`() = assertCompile {
        kotlin(
            """
            class Test const constructor(val message: String)

            fun test() {
                const val t = Test("Test")
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';
            
            @sealed
            class Test {
              const Test(this.message) : super();
              @nonVirtual
              final String message;
            }
            
            void test() {
              const Test t = Test('Test');
            }
            """
        )
    }

    @Test
    fun `const constructor with parameter with default value calling const constructor`() = assertCompile {
        kotlin(
            """
            class Testable const constructor()

            class Test const constructor(val testable: Testable = @const Testable())

            fun main() {
                @const Test()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Testable {
              const Testable() : super();
            }

            @sealed
            class Test {
              const Test({this.testable = const Testable()}) : super();
              @nonVirtual
              final Testable testable;
            }

            void main() {
              const Test();
            }
            """
        )
    }

    @Test
    fun `computed const val`() = assertCompile {
        kotlin(
            """
            const val x: Int = 9223372036854775800 - 20
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';
            
            const int x = 9223372036854775780;
            """
        )
    }

    @Test
    fun `enum values are const`() = assertCompile {
        kotlin(
            """
            enum class Temperature {
                COLD,
                CHILLY,
                WARM,
                HOT
            }

            const val temp = Temperature.WARM
            """
        )

        dart(
            """
            import 'dart:core' hide Enum, List;
            import 'dart:core' as core;
            import 'package:meta/meta.dart';
            
            @sealed
            class Temperature extends Enum<Temperature> {
              const Temperature._(
                String name,
                int ordinal,
              ) : super(name, ordinal);
              static const Temperature COLD = const Temperature._('COLD', 0);
              static const Temperature CHILLY = const Temperature._('CHILLY', 1);
              static const Temperature WARM = const Temperature._('WARM', 2);
              static const Temperature HOT = const Temperature._('HOT', 3);
              static core.List<Temperature> values() {
                return <Temperature>[
                  Temperature.COLD,
                  Temperature.CHILLY,
                  Temperature.WARM,
                  Temperature.HOT
                ];
              }
            
              static Temperature valueOf(String value) {
                return values().firstWhere((v) => v.name == value);
              }
            }
            
            const Temperature temp = Temperature.WARM;
            """
        )
    }
}