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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Compile: Statement")
class Statement : BaseTest {
    @Nested
    inner class When {
        @Test
        fun `if else`() = assertCompile {
            kotlin(
                """
                fun main() {
                    if (0 == 1) {
                        main()
                    } else {
                        main()
                    }
                }
                """
            )

            dart(
                """
                void main() {
                  if (0 == 1) {
                    main();
                  } else {
                    main();
                  }
                }
                """
            )
        }

        @Test
        fun `if else if`() = assertCompile {
            kotlin(
                """
                fun main() {
                    if (1 + 1 == 3) {
                        main()
                    } else if (3 + 3 == 7){
                        main()
                    }
                }
                """
            )

            dart(
                """
                void main() {
                  if (1 + 1 == 3) {
                    main();
                  } else if (3 + 3 == 7) {
                    main();
                  }
                }
                """
            )
        }

        @Test
        fun `empty if`() = assertCompile {
            kotlin(
                """
                fun main() {
                    if (0 == 1) {

                    }
                }
                """
            )

            dart(
                """
                void main() {
                  if (0 == 1) {}
                }
                """
            )
        }

        @Test
        fun `when`() = assertCompile {
            kotlin(
                """
                fun main() {
                    when {
                        1 + 1 == 3 -> main()
                        3 + 3 == 7 -> main()
                    }
                }
                """
            )

            dart(
                """
                void main() {
                  if (1 + 1 == 3) {
                    main();
                  } else if (3 + 3 == 7) {
                    main();
                  }
                }
                """
            )
        }

        @Test
        fun `when with else`() = assertCompile {
            kotlin(
                """
                fun exec(x: Int) {
                    when {
                        1 + 1 == 3 -> exec(2)
                        3 + 3 == 7 -> exec(6)
                        else -> exec(10)
                    }
                }
                """
            )

            dart(
                """
                void exec(int x) {
                  if (1 + 1 == 3) {
                    exec(2);
                  } else if (3 + 3 == 7) {
                    exec(6);
                  } else {
                    exec(10);
                  }
                }
                """
            )
        }

        @Test
        fun `when with subject`() = assertCompile {
            kotlin(
                """
                fun test(y: Int) = y * 3

                fun exec(x: Int) {
                    when(x) {
                        4 -> exec(2)
                        12 -> exec(6)
                    }
                }
                """
            )

            dart(
                """
                int test(int y) {
                  return y * 3;
                }

                void exec(int x) {
                  {
                    final int tmp0_subject = x;
                    if (tmp0_subject == 4) {
                      exec(2);
                    } else if (tmp0_subject == 12) {
                      exec(6);
                    }
                  }
                }
                """
            )
        }

        @Test
        fun `exhaustive when`() = assertCompile {
            kotlin(
                """
                enum class PowerStatus {
                    OFF,
                    ON,
                    STANDBY
                }

                fun main() {
                    val status = PowerStatus.OFF
                    when (status) {
                        PowerStatus.OFF -> {}
                        PowerStatus.ON -> {}
                        PowerStatus.STANDBY -> {}
                    }
                }
                """
            )

            dart(
                """
                enum PowerStatus {
                  OFF._(),
                  ON._(),
                  STANDBY._();

                  const PowerStatus._();
                }

                void main() {
                  final PowerStatus status = PowerStatus.OFF;
                  {
                    final PowerStatus tmp0_subject = status;
                    if (tmp0_subject == PowerStatus.OFF) {
                    } else if (tmp0_subject == PowerStatus.ON) {
                    } else if (tmp0_subject == PowerStatus.STANDBY) {}
                  }
                }

                PowerStatus ${'$'}PowerStatus${'$'}valueOf(String value) =>
                    PowerStatus.values.firstWhere((PowerStatus v) => v.name == value);
                """
            )
        }

        @Test
        fun `exhaustive when in for-loop`() = assertCompile {
            kotlin(
                """
                var currentColor: Color = Color.red

                fun main() {
                    for (i in 0 until 68) {
                        when (currentColor) {
                            Color.red -> {}
                            Color.green -> {}
                            Color.blue -> {}
                        }
                    }
                }

                enum class Color {
                    red, green, blue
                }
                """
            )

            dart(
                """
                import "package:dotlin/src/kotlin/ranges/ranges_ext.dt.g.dart"
                    show IntRangeFactoryExt;
                import "package:dotlin/src/kotlin/ranges/ranges.dt.g.dart" show IntRange;

                Color currentColor = Color.red;
                void main() {
                  for (int i = 0; i < 68; i += 1) {
                    final Color tmp1_subject = currentColor;
                    if (tmp1_subject == Color.red) {
                    } else if (tmp1_subject == Color.green) {
                    } else if (tmp1_subject == Color.blue) {}
                  }
                }

                enum Color {
                  red._(),
                  green._(),
                  blue._();

                  const Color._();
                }

                Color ${'$'}Color${'$'}valueOf(String value) =>
                    Color.values.firstWhere((Color v) => v.name == value);
                """
            )
        }
    }

    @Nested
    @DisplayName("Try-catch")
    inner class TryCatch {
        @Test
        fun `try-catch`() = assertCompile {
            kotlin(
                """
                fun main() {
                    try {
                        thisThrows()
                    } catch (e: Exception) {
                        thisThrows()
                    }
                }

                fun thisThrows(): Int {
                    throw Exception("You done did it now")
                }
                """
            )

            dart(
                """
                void main() {
                  try {
                    thisThrows();
                  } on Exception catch (e) {
                    thisThrows();
                  }
                }

                int thisThrows() {
                  throw Exception("You done did it now");
                }
                """
            )
        }

        @Test
        fun `multiple statements in body`() = assertCompile {
            kotlin(
                """
                fun main() {
                    try {
                        thisThrows()
                        thisThrows()
                    } catch (e: Exception) {
                        thisThrows()
                        thisThrows()
                    }
                }

                fun thisThrows(): Int {
                    throw Exception("You done did it now")
                }
                """
            )

            dart(
                """
                void main() {
                  try {
                    thisThrows();
                    thisThrows();
                  } on Exception catch (e) {
                    thisThrows();
                    thisThrows();
                  }
                }

                int thisThrows() {
                  throw Exception("You done did it now");
                }
                """
            )
        }
    }

    @Nested
    inner class Jump {
        @Test
        fun `return without value`() = assertCompile {
            kotlin(
                """
                fun main() {
                    return
                }
                """
            )

            dart(
                """
                void main() {
                  return;
                }
                """
            )
        }


        @Test
        fun `return in if`() = assertCompile {
            kotlin(
                """
                fun doIt(x: Int?): String {
                    if (x == null) return "null"
                    return x.toString()
                }
                """
            )

            dart(
                """
                String doIt(int? x) {
                  if (x == null) {
                    return "null";
                  }
                  return x.toString();
                }
                """
            )
        }

        @Test
        fun `continue`() = assertCompile {
            kotlin(
                """
                fun main() {
                    while (true) {
                        if (false) continue
                    }
                }
                """
            )

            dart(
                """
                void main() {
                  while (true) {
                    if (false) {
                      continue;
                    }
                  }
                }
                """
            )
        }

        @Test
        fun `nested continue targeting outer loop`() = assertCompile {
            kotlin(
                """
                fun main() {
                    loop0@ while (true)  {
                        while (false) {
                            continue@loop0
                        }
                    }
                }
                """
            )

            dart(
                """
                import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Continue;

                void main() {
                  while (true) {
                    try {
                      while (false) {
                        throw const ${'$'}Continue(3370151);
                      }
                    } on ${'$'}Continue catch (tmp0_continue) {
                      if (tmp0_continue.target == 3370151) {
                        continue;
                      } else {
                        throw tmp0_continue;
                      }
                    }
                  }
                }
                """
            )
        }

        @Test
        fun `break`() = assertCompile {
            kotlin(
                """
                fun main() {
                    while (true) {
                        if (false) break
                    }
                }
                """
            )

            dart(
                """
                void main() {
                  while (true) {
                    if (false) {
                      break;
                    }
                  }
                }
                """
            )
        }

        @Test
        fun `nested break targeting outer loop`() = assertCompile {
            kotlin(
                """
                fun main() {
                    loop0@ while (true)  {
                        while (false) {
                            break@loop0
                        }
                    }
                }
                """
            )

            dart(
                """
                import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Break;

                void main() {
                  while (true) {
                    try {
                      while (false) {
                        throw const ${'$'}Break(3370058);
                      }
                    } on ${'$'}Break catch (tmp0_break) {
                      if (tmp0_break.target == 3370058) {
                        break;
                      } else {
                        throw tmp0_break;
                      }
                    }
                  }
                }
                """
            )
        }

        @Test
        fun `for loop with conditional continue and nested for each loop with single break`() = assertCompile {
            kotlin(
                """
                fun process() {
                    for (i in 0 until 10) {
                        if (true) {
                            continue
                        }

                        for (x in emptyList<Int>()) {
                            break
                        }
                    }
                }
                """
            )

            dart(
                """
                import "package:dotlin/src/kotlin/ranges/ranges_ext.dt.g.dart"
                    show IntRangeFactoryExt;
                import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart"
                    show ${'$'}Continue, ${'$'}Break;
                import "dart:collection" show UnmodifiableListView;
                import "package:dotlin/src/kotlin/ranges/ranges.dt.g.dart" show IntRange;

                void process() {
                  for (int i = 0; i < 10; i += 1) {
                    try {
                      {
                        if (true) {
                          throw const ${'$'}Continue(-309494340);
                        }
                        for (int x in UnmodifiableListView<int>(<int>[])) {
                          try {
                            throw const ${'$'}Break(-309413802);
                          } on ${'$'}Break catch (tmp1_break) {
                            if (tmp1_break.target == -309413802) {
                              break;
                            } else {
                              throw tmp1_break;
                            }
                          }
                        }
                      }
                    } on ${'$'}Continue catch (tmp0_continue) {
                      if (tmp0_continue.target == -309494340) {
                        continue;
                      } else {
                        throw tmp0_continue;
                      }
                    }
                  }
                }
                """
            )
        }

        @Test
        fun `multiple continues`() = assertCompile {
            kotlin(
                """
                fun exec() {
                    for (item in listOf<String>()) {
                        if (item == "x") {
                            continue
                        }

                        if (item == "y") {
                            continue
                        }
                    }
                }
                """
            )

            dart(
                """
                import "dart:collection" show UnmodifiableListView;
                import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Continue;

                void exec() {
                  for (String item in UnmodifiableListView<String>(<String>[])) {
                    try {
                      {
                        if (item == "x") {
                          throw const ${'$'}Continue(3149110);
                        }
                        if (item == "y") {
                          throw const ${'$'}Continue(3149110);
                        }
                      }
                    } on ${'$'}Continue catch (tmp0_continue) {
                      if (tmp0_continue.target == 3149110) {
                        continue;
                      } else {
                        throw tmp0_continue;
                      }
                    }
                  }
                }
                """
            )
        }

        @Test
        fun `break and continue in same loop`() = assertCompile {
            kotlin(
                """
                fun exec() {
                    for (value in listOf<String>()) {
                        if (value == "x") {
                            break
                        }

                        if (value == "y") {
                            continue
                        }
                    }
                }
                """
            )

            dart(
                """
                import "dart:collection" show UnmodifiableListView;
                import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart"
                    show ${'$'}Break, ${'$'}Continue;

                void exec() {
                  for (String value in UnmodifiableListView<String>(<String>[])) {
                    try {
                      try {
                        {
                          if (value == "x") {
                            throw const ${'$'}Break(3149110);
                          }
                          if (value == "y") {
                            throw const ${'$'}Continue(3149110);
                          }
                        }
                      } on ${'$'}Continue catch (tmp0_continue) {
                        if (tmp0_continue.target == 3149110) {
                          continue;
                        } else {
                          throw tmp0_continue;
                        }
                      }
                    } on ${'$'}Break catch (tmp1_break) {
                      if (tmp1_break.target == 3149110) {
                        break;
                      } else {
                        throw tmp1_break;
                      }
                    }
                  }
                }
                """
            )
        }
    }

    @Nested
    inner class Assignment {
        @Test
        fun `=`() = assertCompile {
            kotlin(
                """
                fun main() {
                    var x = 0
                    x = 3
                }
                """
            )

            dart(
                """
                void main() {
                  int x = 0;
                  x = 3;
                }
                """
            )
        }

        @Test
        fun `+=`() = assertCompile {
            kotlin(
                """
                fun main() {
                    var x = 0
                    x += 3
                }
                """
            )

            dart(
                """
                void main() {
                  int x = 0;
                  x += 3;
                }
                """
            )
        }

        @Test
        fun `-=`() = assertCompile {
            kotlin(
                """
                fun main() {
                    var x = 0
                    x -= 3
                }
                """
            )

            dart(
                """
                void main() {
                  int x = 0;
                  x -= 3;
                }
                """
            )
        }

        @Test
        fun `*=`() = assertCompile {
            kotlin(
                """
                fun main() {
                    var x = 0
                    x *= 3
                }
                """
            )

            dart(
                """
                void main() {
                  int x = 0;
                  x *= 3;
                }
                """
            )
        }

        @Test
        fun `div=`() = assertCompile {
            kotlin(
                """
                fun main() {
                    var x = 0
                    x /= 3
                }
                """
            )

            dart(
                """
                void main() {
                  int x = 0;
                  x ~/= 3;
                }
                """
            )
        }

        @Test
        fun `div= on Double`() = assertCompile {
            kotlin(
                """
                fun main() {
                    var x = 0.0
                    x /= 3
                }
                """
            )

            dart(
                """
                void main() {
                  double x = 0.0;
                  x /= 3;
                }
                """
            )
        }

        @Test
        fun `= on property`() = assertCompile {
            kotlin(
                """
                class Test {
                    var x = 0
                }

                fun main() {
                    Test().x = 3
                }
                """
            )

            dart(
                """
                import "package:meta/meta.dart" show nonVirtual, sealed;

                @sealed
                class Test {
                  @nonVirtual
                  int x = 0;
                }

                void main() {
                  Test().x = 3;
                }
                """
            )
        }

        @Test
        fun `+= on property`() = assertCompile {
            kotlin(
                """
                class Test {
                    var x = 0
                }

                fun main() {
                    Test().x += 3
                }
                """
            )

            dart(
                """
                import "package:meta/meta.dart" show nonVirtual, sealed;

                @sealed
                class Test {
                  @nonVirtual
                  int x = 0;
                }

                void main() {
                  Test().x += 3;
                }
                """
            )
        }

        @Test
        fun `-= on property`() = assertCompile {
            kotlin(
                """
                class Test {
                    var x = 0
                }

                fun main() {
                    Test().x -= 3
                }
                """
            )

            dart(
                """
                import "package:meta/meta.dart" show nonVirtual, sealed;

                @sealed
                class Test {
                  @nonVirtual
                  int x = 0;
                }

                void main() {
                  Test().x -= 3;
                }
                """
            )
        }

        @Test
        fun `*= on property`() = assertCompile {
            kotlin(
                """
                class Test {
                    var x = 0
                }

                fun main() {
                    Test().x *= 3
                }
                """
            )

            dart(
                """
                import "package:meta/meta.dart" show nonVirtual, sealed;

                @sealed
                class Test {
                  @nonVirtual
                  int x = 0;
                }

                void main() {
                  Test().x *= 3;
                }
                """
            )
        }

        @Test
        fun `div= on property`() = assertCompile {
            kotlin(
                """
                class Test {
                    var x = 0
                }

                fun main() {
                    Test().x /= 3
                }
                """
            )

            dart(
                """
                import "package:meta/meta.dart" show nonVirtual, sealed;

                @sealed
                class Test {
                  @nonVirtual
                  int x = 0;
                }

                void main() {
                  Test().x ~/= 3;
                }
                """
            )
        }

        @Test
        fun `div= on Double property`() = assertCompile {
            kotlin(
                """
                class Test {
                    var x = 0.0
                }

                fun main() {
                    Test().x /= 3
                }
                """
            )

            dart(
                """
                import "package:meta/meta.dart" show nonVirtual, sealed;

                @sealed
                class Test {
                  @nonVirtual
                  double x = 0.0;
                }

                void main() {
                  Test().x /= 3;
                }
                """
            )
        }

        @Test
        fun `lateinit var`() = assertCompile {
            kotlin(
                """
                fun main() {
                    lateinit var test: Int

                    test = 3
                }
                """
            )

            dart(
                """
                void main() {
                  late int test;
                  test = 3;
                }
                """
            )
        }
    }
}