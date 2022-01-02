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

@DisplayName("Compile: Object")
class Object : BaseTest {
    @Test
    fun `object`() = assertCompile {
        kotlin(
            """
            object Test
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
              static final Test ${'$'}instance = Test._();
            }
            """
        )
    }

    @Test
    fun `object with method`() = assertCompile {
        kotlin(
            """
            object Test {
                fun compute(x: Int, y: Float): Int {
                    return 343
                }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
              @nonVirtual
              int ${'$'}compute(
                int x,
                double y,
              ) {
                return 343;
              }
            
              static final Test ${'$'}instance = Test._();
              static int compute(
                int x,
                double y,
              ) =>
                  Test.${'$'}instance.${'$'}compute(x, y);
            }
            """
        )
    }

    @Test
    fun `companion object`() = assertCompile {
        kotlin(
            """
            class Test {
                companion object
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              static final Test${'$'}Companion ${'$'}companion = Test${'$'}Companion.${'$'}instance;
            }
            
            @sealed
            class Test${'$'}Companion {
              Test${'$'}Companion._() : super();
              static final Test${'$'}Companion ${'$'}instance = Test${'$'}Companion._();
            }
            """
        )
    }

    @Test
    fun `companion object with method`() = assertCompile {
        kotlin(
            """
            class Test {
                companion object {
                    fun compute() = 343
                }
            }

            fun main() {
                Test.compute()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              static final Test${'$'}Companion ${'$'}companion = Test${'$'}Companion.${'$'}instance;
              static int compute() => Test${'$'}Companion.${'$'}instance.${'$'}compute();
            }

            void main() {
              Test${'$'}Companion.${'$'}instance.${'$'}compute();
            }

            @sealed
            class Test${'$'}Companion {
              Test${'$'}Companion._() : super();
              @nonVirtual
              int ${'$'}compute() {
                return 343;
              }

              static final Test${'$'}Companion ${'$'}instance = Test${'$'}Companion._();
            }
            """
        )
    }

    @Test
    fun `object with property`() = assertCompile {
        kotlin(
            """
            object Test {
                val property = 3
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
              @nonVirtual
              final int ${'$'}property = 3;
              static final Test ${'$'}instance = Test._();
              static final int property = Test.${'$'}instance.${'$'}property;
            }
            """
        )
    }

    @Test
    fun `object with property getter`() = assertCompile {
        kotlin(
            """
            object Test {
                val property: Int
                    get() = 42
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
              @nonVirtual
              int get ${'$'}property {
                return 42;
              }

              static final Test ${'$'}instance = Test._();
              static int get property => Test.${'$'}instance.${'$'}property;
            }
            """
        )
    }

    @Test
    fun `object with property getter and setter`() = assertCompile {
        kotlin(
            """
            object Test {
                var property: Int
                    get() = 42
                    set(value) {}
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
              @nonVirtual
              int get ${'$'}property {
                return 42;
              }

              @nonVirtual
              void set ${'$'}property(int value) {}
              static final Test ${'$'}instance = Test._();
              static int get property => Test.${'$'}instance.${'$'}property;
              static void set property(int value) => Test.${'$'}instance.${'$'}property = value;
            }
            """
        )
    }

    @Test
    fun `object with property getter and setter with backing field`() = assertCompile {
        kotlin(
            """
            object Test {
                var property: Int = 0
                    get() = 42
                    set(value) { field = value }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
              @nonVirtual
              int _${'$'}propertyBackingField = 0;
              @nonVirtual
              int get ${'$'}property {
                return 42;
              }

              @nonVirtual
              void set ${'$'}property(int value) {
                this._${'$'}propertyBackingField = value;
              }

              static final Test ${'$'}instance = Test._();
              static int get property => Test.${'$'}instance.${'$'}property;
              static void set property(int value) => Test.${'$'}instance.${'$'}property = value;
            }
            """
        )
    }

    @Test
    fun `object with private property getter and setter with backing field`() = assertCompile {
        kotlin(
            """
            object Test {
                private var property: Int = 0
                    get() = 42
                    set(value) { field = value }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
              @nonVirtual
              int _${'$'}propertyBackingField = 0;
              @nonVirtual
              int get _${'$'}property {
                return 42;
              }

              @nonVirtual
              void set _${'$'}property(int value) {
                this._${'$'}propertyBackingField = value;
              }

              static final Test ${'$'}instance = Test._();
              static int get _property => Test.${'$'}instance._${'$'}property;
              static void set _property(int value) => Test.${'$'}instance._${'$'}property = value;
            }
            """
        )
    }
}