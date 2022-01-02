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
import assertCompileFiles
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
              static final Lin ${'$'}companion = Lin.${'$'}instance;
            }

            void main() {
              Kot();
            }

            @sealed
            class Lin {
              Lin._() : super();
              static final Lin ${'$'}instance = Lin._();
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
              static final Lin ${'$'}companion = Lin.${'$'}instance;
            }

            void main() {
              Dot();
            }

            @sealed
            class Lin {
              Lin._() : super();
              static final Lin ${'$'}instance = Lin._();
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

            extension ${'$'}HobbitExtensions on Hobbit {
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

            extension ${'$'}HobbitExtensions on Hobbit {
              bool isProudfoot() {
                return false;
              }
            }

            extension ${'$'}ProudfootExtensions on Proudfoot {
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

            extension ${'$'}ProudfootExtensions on Proudfoot {
              bool isProudfoot() {
                return true;
              }
            }
            """
        )
    }

    @Test
    fun `@DartImportAlias`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartImportAlias("dart:core")
            external class List

            fun main() {
                List()
            }
            """
        )

        dart(
            """
            import 'dart:core' hide List;
            import 'dart:core' as core;
            import 'package:meta/meta.dart';

            void main() {
              core.List();
            }
            """
        )
    }

    @Test
    fun `@DartImportAlias separate input files`() = assertCompileFiles {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            package test

            @DartImportAlias("dart:core")
            external class List
            """
        )

        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            package test

            fun main() {
                List()
            }
            """
        )

        dart(
            """
            import 'dart:core' hide List;
            import 'dart:core' as core;
            import 'package:meta/meta.dart';

            void main() {
              core.List();
            }
            """
        )
    }

    @Test
    fun `@DartImportAlias type reference only`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartImportAlias("dart:core")
            external class List

            fun test(list: List) {}
            """
        )

        dart(
            """
            import 'dart:core' hide List;
            import 'dart:core' as core;
            import 'package:meta/meta.dart';

            void test(core.List list) {}
            """
        )
    }

    @Test
    fun `@DartHideImport`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartHideImport("dart:core")
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
    fun `@DartHideImport twice`() = assertCompile {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            @DartHideImport("dart:core")
            class Enum

            @DartHideImport("dart:core")
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
    fun `@DartHideImport twice, multiple files`() = assertCompileFiles {
        kotlin(
            """
            @file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

            package test

            @DartHideImport("dart:core")
            class Something

            @DartHideImport("dart:core")
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
    fun `@DartCatchAs`() = assertCompile {
        kotlin(
            """
            fun main() {
                try {
                    something()
                } catch (e: UnsupportedOperationException) {
                    e.message
                }
            }

            fun something(): Nothing = throw UnsupportedOperationException()
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              try {
                something();
              } on UnsupportedError catch (e) {
                final UnsupportedOperationException tmp0_catchAs =
                    e is! UnsupportedOperationException
                        ? UnsupportedOperationException.from(e)
                        : e;
                tmp0_catchAs.message;
              }
            }

            Never something() {
              throw UnsupportedOperationException.empty();
            }
            """
        )
    }
}