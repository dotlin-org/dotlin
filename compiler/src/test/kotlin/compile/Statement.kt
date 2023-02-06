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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                fun main(x: Int) {
                    when {
                        1 + 1 == 3 -> main(2)
                        3 + 3 == 7 -> main(6)
                        else -> main(10)
                    }
                }
                """
            )

            dart(
                """
                import "package:meta/meta.dart";

                void main(int x) {
                  if (1 + 1 == 3) {
                    main(2);
                  } else if (3 + 3 == 7) {
                    main(6);
                  } else {
                    main(10);
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

                fun main(x: Int) {
                    when(x) {
                        4 -> test(2)
                        12 -> test(6)
                    }
                }
                """
            )

            dart(
                """
                import "package:meta/meta.dart";

                int test(int y) {
                  return y * 3;
                }

                void main(int x) {
                  {
                    final int tmp0_subject = x;
                    if (tmp0_subject == 4) {
                      test(2);
                    } else if (tmp0_subject == 12) {
                      test(6);
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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
    inner class Return {
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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

                String doIt(int? x) {
                  if (x == null) {
                    return "null";
                  }
                  return x.toString();
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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

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
                import "package:meta/meta.dart";

                void main() {
                  late int test;
                  test = 3;
                }
                """
            )
        }
    }
}