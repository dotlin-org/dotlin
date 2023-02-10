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
import org.junit.jupiter.api.Disabled
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
    fun `using class from Dart's typed_data`() = assertCompile {
        kotlin(
            """
            import dart.typed_data.ByteData

            fun main() {
                val buffer = ByteData(8)
            }
            """
        )

        dart(
            """
            import "dart:typed_data" show ByteData;
            import "package:meta/meta.dart";

            void main() {
              final ByteData buffer = ByteData(8);
            }
            """
        )
    }

    @Test
    fun `import Dart function from dependency`() = assertCompile {
        dependency {
            name = "yad"

            dart(
                """
                int yetAnotherFunction() {
                  return 123;
                }
                """,
                Path("lib/test.dart")
            )
        }

        kotlin(
            """
            import dev.pub.yad.test.yetAnotherFunction

            fun main() {
                val result = yetAnotherFunction()
            }
            """
        )

        dart(
            """
            import "package:yad/test.dart" show yetAnotherFunction;
            import "package:meta/meta.dart";

            void main() {
              final int result = yetAnotherFunction();
            }
            """
        )
    }

    @Test
    fun `import Dart class from dependency`() = assertCompile {
        dependency {
            name = "yad"

            dart(
                """
                class YetAnotherClass {
                  final yetAnotherField = 3;
                }
                """,
                Path("lib/yet_another_class.dart")
            )
        }

        kotlin(
            """
            import dev.pub.yad.yet_another_class.YetAnotherClass

            fun main() {
                lateinit var x: YetAnotherClass
            }
            """
        )

        dart(
            """
            import "package:yad/yet_another_class.dart" show YetAnotherClass;
            import "package:meta/meta.dart";

            void main() {
              late YetAnotherClass x;
            }
            """
        )
    }

    @Test
    fun `import multiple Dart functions with wildcard`() = assertCompile {
        dependency {
            name = "yad"

            dart(
                """
                int yetAnotherFunction1() {
                  return 123;
                }

                int yetAnotherFunction2() {
                  return 456;
                }
                """,
                Path("lib/test.dart")
            )
        }

        kotlin(
            """
            import dev.pub.yad.test.*

            fun main() {
                val result1 = yetAnotherFunction1()
                val result2 = yetAnotherFunction2()
            }
            """
        )

        dart(
            """
            import "package:yad/test.dart" show yetAnotherFunction1, yetAnotherFunction2;
            import "package:meta/meta.dart";

            void main() {
              final int result1 = yetAnotherFunction1();
              final int result2 = yetAnotherFunction2();
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

    @Disabled
    @Test
    fun `import Kotlin enum entry`() = assertCompile {
        kotlin(
            """
            package pkg0

            enum class Temp {
                COOL,
                HOT
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            enum Temp {
              COOL._(),
              HOT._();

              const Temp._();
            }

            Temp ${'$'}Temp${'$'}valueOf(String value) =>
                Temp.values.firstWhere((Temp v) => v.name == value);
            """
        )

        kotlin(
            """
            package pkg1

            import pkg0.Temp

            fun main() {
                val x = Temp.COOL
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Temp x = Temp.cool;
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
}