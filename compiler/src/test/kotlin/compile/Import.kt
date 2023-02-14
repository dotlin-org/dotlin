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
import kotlin.io.path.*

@DisplayName("Compile: Import")
class Import : BaseTest {
    @Test
    fun `import as`() = assertCompile {
        kotlin(
            """
            import kotlin.Int as Int64

            fun main() {
                val x: Int64 = 3
            }
            """
        )

        dart(
            """
            import "dart:core" as Int64 show int;
            import "dart:core" hide int;
            import "package:meta/meta.dart";

            void main() {
              final Int64.int x = 3;
            }
            """
        )
    }

    @Test
    fun `import multiple Kotlin functions from different files with wildcard`() = assertCompile {
        dependency {
            name = "yad"

            kotlin(
                """
                package dev.pub.yad.functions

                fun yetAnotherFunction1(): Int = 123
                """,
                Path("lib/test1.kt")
            )

            kotlin(
                """
                package dev.pub.yad.functions

                fun yetAnotherFunction2(): Int = 456
                """,
                Path("lib/test2.kt")
            )
        }

        kotlin(
            """
            import dev.pub.yad.functions.*

            fun main() {
                val result1 = yetAnotherFunction1()
                val result2 = yetAnotherFunction2()
            }
            """
        )

        dart(
            """
            import "package:yad/test1.dt.g.dart" show yetAnotherFunction1;
            import "package:yad/test2.dt.g.dart" show yetAnotherFunction2;
            import "package:meta/meta.dart";

            void main() {
              final int result1 = yetAnotherFunction1();
              final int result2 = yetAnotherFunction2();
            }
            """
        )
    }

    @Test
    fun `import multiple Dart functions with wildcard in same project`() = assertCompile {
        kotlin(
            """
            package pkg0

            fun yetAnotherFunction1() = 3
            fun yetAnotherFunction2() = 4
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            int yetAnotherFunction1() {
              return 3;
            }

            int yetAnotherFunction2() {
              return 4;
            }
            """
        )

        kotlin(
            """
            package pkg1

            import pkg0.*

            fun main() {
                val result1 = yetAnotherFunction1()
                val result2 = yetAnotherFunction2()
            }
            """
        )

        dart(
            """
            import "0.dt.g.dart" show yetAnotherFunction1, yetAnotherFunction2;
            import "package:meta/meta.dart";

            void main() {
              final int result1 = yetAnotherFunction1();
              final int result2 = yetAnotherFunction2();
            }
            """
        )
    }

    @Test
    fun `same project import`() = assertCompile {
        kotlin(
            """
            package pkg0

            fun calculate() = 30
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            int calculate() {
              return 30;
            }
            """
        )

        kotlin(
            """
            package pkg1

            import pkg0.calculate

            fun main() {
                val x = calculate()
            }
            """
        )

        dart(
            """
            import "0.dt.g.dart" show calculate;
            import "package:meta/meta.dart";

            void main() {
              final int x = calculate();
            }
            """
        )
    }

    @Test
    fun `import Kotlin enum entry`() = assertCompile {
        kotlin(
            """
            package pkg0

            enum class Temp {
                cool,
                hot
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            enum Temp {
              cool._(),
              hot._();

              const Temp._();
            }

            Temp ${'$'}Temp${'$'}valueOf(String value) =>
                Temp.values.firstWhere((Temp v) => v.name == value);
            """
        )

        kotlin(
            """
            package pkg1

            import pkg0.Temp.cool

            fun main() {
                val temp = cool
            }
            """
        )

        dart(
            """
            import "0.dt.g.dart" show Temp;
            import "package:meta/meta.dart";

            void main() {
              final Temp temp = Temp.cool;
            }
            """
        )
    }

    @Test
    fun `import Kotlin enum entry if used as argument`() = assertCompile {
        kotlin(
            """
            package pkg0

            enum class Temp {
                cool,
                hot
            }

            fun isBearable(temp: Temp) = temp == Temp.cool
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            enum Temp {
              cool._(),
              hot._();

              const Temp._();
            }

            bool isBearable(Temp temp) {
              return temp == Temp.cool;
            }

            Temp ${'$'}Temp${'$'}valueOf(String value) =>
                Temp.values.firstWhere((Temp v) => v.name == value);
            """
        )

        kotlin(
            """
            package pkg1

            import pkg0.isBearable
            import pkg0.Temp.cool

            fun main() {
                val x = isBearable(cool)
            }
            """
        )

        dart(
            """
            import "0.dt.g.dart" show Temp, isBearable;
            import "package:meta/meta.dart";

            void main() {
              final bool x = isBearable(Temp.cool);
            }
            """
        )
    }

    @Test
    fun `import Kotlin enum entries`() = assertCompile {
        kotlin(
            """
            package pkg0

            enum class Temp {
                cool,
                hot
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            enum Temp {
              cool._(),
              hot._();

              const Temp._();
            }

            Temp ${'$'}Temp${'$'}valueOf(String value) =>
                Temp.values.firstWhere((Temp v) => v.name == value);
            """
        )

        kotlin(
            """
            package pkg1

            import pkg0.Temp.*

            fun main() {
                val temp = cool
                val temp2 = hot
            }
            """
        )

        dart(
            """
            import "0.dt.g.dart" show Temp;
            import "package:meta/meta.dart";

            void main() {
              final Temp temp = Temp.cool;
              final Temp temp2 = Temp.hot;
            }
            """
        )
    }

    @Test
    fun `import Kotlin enum entries if used as arguments`() = assertCompile {
        kotlin(
            """
            package pkg0

            enum class Temp {
                cool,
                hot
            }

            fun isBearable(temp: Temp) = temp == Temp.cool
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            enum Temp {
              cool._(),
              hot._();

              const Temp._();
            }

            bool isBearable(Temp temp) {
              return temp == Temp.cool;
            }

            Temp ${'$'}Temp${'$'}valueOf(String value) =>
                Temp.values.firstWhere((Temp v) => v.name == value);
            """
        )

        kotlin(
            """
            package pkg1

            import pkg0.isBearable
            import pkg0.Temp.*

            fun main() {
                val x = isBearable(cool)
                val y = isBearable(hot)
            }
            """
        )

        dart(
            """
            import "0.dt.g.dart" show Temp, isBearable;
            import "package:meta/meta.dart";

            void main() {
              final bool x = isBearable(Temp.cool);
              final bool y = isBearable(Temp.hot);
            }
            """
        )
    }

    @Test
    fun `import type alias`() = assertCompile {
        kotlin(
            """
            package pkg0

            typealias MyInt = Int
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            typedef MyInt = int;
            """
        )

        kotlin(
            """
            package pkg1

            import pkg0.MyInt

            fun calc(): MyInt = 3
            """
        )

        dart(
            """
            import "0.dt.g.dart" show MyInt;
            import "package:meta/meta.dart";

            MyInt calc() {
              return 3;
            }
            """
        )
    }

    @Test
    fun `import type argument of expression`() = assertCompile {
        kotlin(
            """
            package pkg0

            class A
            class B<T>
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            @sealed
            class A {}

            @sealed
            class B<T> {}
            """
        )

        kotlin(
            """
            package pkg1

            import pkg0.A
            import pkg0.B

            fun calc() = B<A>()
            """
        )

        dart(
            """
            import "0.dt.g.dart" show B, A;
            import "package:meta/meta.dart";

            B<A> calc() {
              return B<A>();
            }
            """
        )
    }

    @Test
    fun `import type argument of type`() = assertCompile {
        kotlin(
            """
            import kotlin.reflect.KType

            interface A {
                val list: List<KType>
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/reflect/ktype.dt.g.dart" show KType;
            import "package:meta/meta.dart";

            abstract class A {
              abstract final List<KType> list;
            }
            """
        )
    }
}