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
            import 'package:meta/meta.dart';

            @sealed
            class Dar {}

            void main() {
              Dar();
            }
            """
        )
    }

    @Test
    fun `@DartName on primary constructor`() = assertCompile {
        kotlin(
            """
            class Human @DartName("withName") constructor(val name: String)

            fun main() {
                Human("Faramir")
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Human {
              Human.withName(this.name) : super();
              @nonVirtual
              final String name;
            }

            void main() {
              Human.withName('Faramir');
            }
            """
        )
    }

    @Test
    fun `@DartName on secondary constructor`() = assertCompile {
        kotlin(
            """
            class Human(val name: String) {
                @DartName("nameless")
                constructor() : this("")
            }

            fun main() {
                Human()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Human {
              Human(this.name) : super();
              @nonVirtual
              final String name;
              Human.nameless() : this('');
            }

            void main() {
              Human.nameless();
            }
            """
        )
    }

    @Test
    fun `@DartName on simple property`() = assertCompile {
        kotlin(
            """
            class Cool {
                @DartName("CHILL")
                val chill = 0
            }

            fun main() {
                Cool().chill
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Cool {
              @nonVirtual
              final int CHILL = 0;
            }

            void main() {
              Cool().CHILL;
            }
            """
        )
    }

    @Test
    fun `@DartName on property with getter`() = assertCompile {
        kotlin(
            """
            class Cool {
                @DartName("CHILL")
                val chill: Int
                    get() = 0
            }

            fun main() {
                Cool().chill
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Cool {
              @nonVirtual
              int get CHILL {
                return 0;
              }
            }

            void main() {
              Cool().CHILL;
            }
            """
        )
    }

    @Test
    fun `@DartName on companion object`() = assertCompile {
        kotlin(
            """
            class Kot {
              @DartName("Lin")
              companion object
            }

            fun main() {
                Kot()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Kot {
              static const Lin ${'$'}companion = Lin.${'$'}instance;
            }

            void main() {
              Kot();
            }

            @sealed
            class Lin {
              const Lin._() : super();
              static const Lin ${'$'}instance = const Lin._();
            }
            """
        )
    }

    @Test
    fun `@DartName on class and companion object`() = assertCompile {
        kotlin(
            """
            @DartName("Dot")
            class Kot {
              @DartName("Lin")
              companion object
            }

            fun main() {
                Kot()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Dot {
              static const Lin ${'$'}companion = Lin.${'$'}instance;
            }

            void main() {
              Dot();
            }

            @sealed
            class Lin {
              const Lin._() : super();
              static const Lin ${'$'}instance = const Lin._();
            }
            """
        )
    }

    @Test
    fun `@DartConst constructor`() = assertCompile {
        kotlin(
            """
            class Test @DartConst constructor()

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
    fun `@DartConst constructor call`() = assertCompile {
        kotlin(
            """
            class Test @DartConst constructor()

            fun main() {
                @DartConst Test()
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
    fun `@DartConst constructor with parameter with default value calling const constructor`() = assertCompile {
        kotlin(
            """
            class Testable @DartConst constructor()

            class Test @DartConst constructor(val testable: Testable = Testable())

            fun main() {
                Test()
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
              Test();
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
            import 'package:meta/meta.dart';

            @sealed
            class Hobbit {
              @nonVirtual
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
            import 'package:meta/meta.dart';

            class Hobbit {
              bool get isProudfoot {
                return false;
              }
            }

            @sealed
            class Proudfoot extends Hobbit {
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
    fun `@DartExtension`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            class Hobbit {
                @DartExtension
                fun isProudfoot(): Boolean = true
            }

            fun main() {
                Hobbit().isProudfoot()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Hobbit {}

            void main() {
              Hobbit().isProudfoot();
            }

            extension ${'$'}HobbitExtensions${'$'}21ad63ef on Hobbit {
              bool isProudfoot() {
                return true;
              }
            }
            """
        )
    }

    @Test
    fun `@DartExtension override`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            open class Hobbit {
                @DartExtension
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
            import 'package:meta/meta.dart';

            class Hobbit {}

            @sealed
            class Proudfoot extends Hobbit {}

            void main() {
              Proudfoot().isProudfoot();
            }

            extension ${'$'}HobbitExtensions${'$'}21ad63ef on Hobbit {
              bool isProudfoot() {
                return false;
              }
            }

            extension ${'$'}ProudfootExtensions${'$'}21ad63ef on Proudfoot {
              bool isProudfoot() {
                return true;
              }
            }
            """
        )
    }

    // TODO: Analysis error if @DartExtension is used on non-external abstract method, or is override from
    // non-external abstract method
    @Test
    fun `@DartExtension on abstract method`() = assertCompile {
        kotlin(
            """
            @file:Suppress(
                "INVISIBLE_MEMBER",
                "INVISIBLE_REFERENCE",
                "WRONG_BODY_OF_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
                "EXTERNAL_DELEGATED_CONSTRUCTOR_CALL" // TODO: Fix in analyzer
            )

            abstract external class Hobbit {
                @DartExtension
                abstract fun isProudfoot(): Boolean
            }

            external class Proudfoot : Hobbit() {
                override fun isProudfoot(): Boolean = true
            }

            fun main() {
                Proudfoot().isProudfoot()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              Proudfoot().isProudfoot();
            }

            extension ${'$'}ProudfootExtensions${'$'}21ad63ef on Proudfoot {
              bool isProudfoot() {
                return true;
              }
            }
            """
        )
    }

    @Test
    fun `@DartLibrary`() = assertCompile {
        kotlin(
            """
            @DartLibrary("dart:typed_data")
            external class Something

            fun test(s: Something) {}
            """
        )

        dart(
            """
            import 'dart:typed_data';
            import 'package:meta/meta.dart';

            void test(Something s) {}
            """
        )
    }

    @Test
    fun `@DartLibrary aliased`() = assertCompile {
        kotlin(
            """
            @DartLibrary("dart:core", aliased = true)
            external class List

            fun main() {
                List()
            }
            """
        )

        dart(
            """
            import 'dart:core' as core;
            import 'dart:core' hide List;
            import 'package:meta/meta.dart';

            void main() {
              core.List();
            }
            """
        )
    }

    @Test
    fun `@DartLibrary aliased separate input files`() = assertCompile {
        kotlin(
            """
            package test

            @DartLibrary("dart:core", aliased = true)
            external class List
            """
        )

        kotlin(
            """
            package test

            fun main() {
                List()
            }
            """
        )

        dart(
            """
            import 'dart:core' as core;
            import 'dart:core' hide List;
            import 'package:meta/meta.dart';

            void main() {
              core.List();
            }
            """
        )
    }

    @Test
    fun `@DartLibrary aliased type reference only`() = assertCompile {
        kotlin(
            """
            @DartLibrary("dart:core", aliased = true)
            external class List

            fun test(list: List) {}
            """
        )

        dart(
            """
            import 'dart:core' as core;
            import 'dart:core' hide List;
            import 'package:meta/meta.dart';

            void test(core.List list) {}
            """
        )
    }

    @Test
    fun `@DartHideNameFromCore`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartHideNameFromCore
            class Enum

            fun main() {
                Enum()
            }
            """
        )

        dart(
            """
            import 'dart:core' hide Enum;
            import 'package:meta/meta.dart';

            @sealed
            class Enum {}

            void main() {
              Enum();
            }
            """
        )
    }

    @Test
    fun `@DartHideNameFromCore twice`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartHideNameFromCore
            class Enum

            @DartHideNameFromCore
            class List

            fun main() {
                Enum()
            }
            """
        )

        dart(
            """
            import 'dart:core' hide Enum, List;
            import 'package:meta/meta.dart';

            @sealed
            class Enum {}

            @sealed
            class List {}

            void main() {
              Enum();
            }
            """
        )
    }

    @Test
    fun `@DartHideNameFromCore twice, multiple files`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            package test

            @DartHideNameFromCore
            class Something

            @DartHideNameFromCore
            class SomethingElse
            """
        )

        kotlin(
            """
            package test

            fun main() {
                Something()
            }
            """
        )

        dart(
            """
            import 'dart:core' hide Something, SomethingElse;
            import 'package:meta/meta.dart';

            @sealed
            class Something {}

            @sealed
            class SomethingElse {}
            """
        )

        dart(
            """
            import '0.g.dart';
            import 'dart:core' hide Something;
            import 'package:meta/meta.dart';

            void main() {
              Something();
            }
            """
        )
    }

    @Test
    fun `dart()`() = assertCompile {
        kotlin(
            """
            fun main() {
                val x = dart("[]")
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final dynamic x = [];
            }
            """
        )
    }

    @Test
    fun `dart() as body`() = assertCompile {
        kotlin(
            """
            fun main() {
                dart(
                    ${"\"\"\""}
                    final x = [0, 1, 2];
                    for (int n in x) {
                      print(n);
                    }
                    ${"\"\"\""}
                )
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final x = [0, 1, 2];
              for (int n in x) {
                print(n);
              }
              ;
            }
            """
        )
    }

    @Test
    fun `@DartPositional`() = assertCompile {
        kotlin(
            """
            @DartPositional
            fun test(x: Int = 0, y: Int? = null, z: Int) {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void test(
              int z, [
              int x = 0,
              int? y = null,
            ]) {}
            """
        )
    }

    @Test
    fun `@DartPositional on constructor`() = assertCompile {
        kotlin(
            """
            class Test @DartPositional constructor(val x: Int, val y: Int? = null)
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test(
                this.x, [
                this.y = null,
              ]) : super();
              @nonVirtual
              final int x;
              @nonVirtual
              final int? y;
            }
            """
        )
    }

    @Test
    fun `@DartPositional on method with override`() = assertCompile {
        kotlin(
            """
            open class A {
                @DartPositional
                open fun doTest(x: Int, y: Int?, z: Int = -1) {}
            }

            class B : A() {
                override fun doTest(x: Int, y: Int?, z: Int) {}
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            class A {
              void doTest(
                int x,
                int? y, [
                int z = -1,
              ]) {}
            }

            @sealed
            class B extends A {
              @override
              void doTest(
                int x,
                int? y, [
                int z = -1,
              ]) {}
            }
            """
        )
    }

    @Test
    fun `@DartStatic`() = assertCompile {
        kotlin(
            """
            @file:Suppress(
                "NESTED_CLASS_IN_EXTERNAL_INTERFACE", // TODO: Fix in analyzer
                "NESTED_EXTERNAL_DECLARATION" // TODO: Fix in analyzer
            )

            external interface DateTime {
                @DartStatic
                external companion object {
                    fun now(): DateTime
                }
            }

            fun main() {
                DateTime.now()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              DateTime.now();
            }
            """
        )
    }

    // TODO: Add analysis check that the parent object must be external.
    @Test
    fun `@DartStatic on method`() = assertCompile {
        kotlin(
            """
            @file:Suppress(
                "NESTED_CLASS_IN_EXTERNAL_INTERFACE", // TODO: Fix in analyzer
                "NESTED_EXTERNAL_DECLARATION" // TODO: Fix in analyzer
            )

            external interface DateTime {
                external companion object {
                    @DartStatic
                    fun now(): DateTime
                }
            }

            fun main() {
                DateTime.now()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              DateTime.now();
            }
            """
        )
    }

    @Test
    fun `@DartStatic on property`() = assertCompile {
        kotlin(
            """
            @file:Suppress(
                "NESTED_CLASS_IN_EXTERNAL_INTERFACE", // TODO: Fix in analyzer
                "NESTED_EXTERNAL_DECLARATION" // TODO: Fix in analyzer
            )

            external interface DateTime {
                external companion object {
                    @DartStatic
                    val now: DateTime
                }
            }

            fun main() {
                DateTime.now
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              DateTime.now;
            }
            """
        )
    }

    @Test
    fun `@DartStatic on method but in non-external companion object`() = assertCompile {
        kotlin(
            """
            @file:Suppress(
                "NESTED_CLASS_IN_EXTERNAL_INTERFACE", // TODO: Fix in analyzer
                "NESTED_EXTERNAL_DECLARATION" // TODO: Fix in analyzer
            )

            external interface DateTime {
                companion object {
                    @DartStatic
                    external fun now(): DateTime
                }
            }

            fun main() {
                DateTime.now()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              DateTime.now();
            }

            @sealed
            class DateTime${'$'}Companion {
              const DateTime${'$'}Companion._();
              static const DateTime${'$'}Companion ${'$'}instance = const DateTime${'$'}Companion._();
            }
            """
        )
    }

    @Test
    fun `@DartExtensionName`() = assertCompile {
        kotlin(
            """
            @DartExtensionName("NegativeIntExtensions")
            fun Int.toNegative() = when {
                this > 0 -> -this
                else -> this
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            extension NegativeIntExtensions on int {
              int toNegative() {
                return this > 0 ? -this : this;
              }
            }
            """
        )
    }

    @Test
    fun `@DartExtensionName with same type but different names in same file`() = assertCompile {
        kotlin(
            """
            @DartExtensionName("NegativeIntExtensions")
            fun Int.toNegative() = when {
                this > 0 -> -this
                else -> this
            }

            @DartExtensionName("PositiveIntExtensions")
            fun Int.toPositive() = abs()
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            extension NegativeIntExtensions on int {
              int toNegative() {
                return this > 0 ? -this : this;
              }
            }

            extension PositiveIntExtensions on int {
              int toPositive() {
                return this.abs();
              }
            }
            """
        )
    }

    @Test
    fun `@DartExtensionName on file`() = assertCompile {
        kotlin(
            """
            @file:DartExtensionName("IntExtensions")

            fun Int.toNegative() = when {
                this > 0 -> -this
                else -> this
            }

            fun Int.toPositive() = abs()
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            extension IntExtensions on int {
              int toNegative() {
                return this > 0 ? -this : this;
              }

              int toPositive() {
                return this.abs();
              }
            }
            """
        )
    }
}