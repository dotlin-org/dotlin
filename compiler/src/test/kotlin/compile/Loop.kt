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

@DisplayName("Compile: Loop")
class Loop : BaseTest {
    @Test
    fun `while loop`() = assertCompile {
        kotlin(
            """
            fun main() {
                var i = 0
                while (i <= 10) {
                    process(i)
                    i++
                }
            }

            fun process(number: Int) {}
            """
        )

        dart(
            """
            void main() {
              int i = 0;
              while (i <= 10) {
                process(i);
                i++;
              }
            }

            void process(int number) {}
            """
        )
    }

    @Test
    fun `while loop with single statement body`() = assertCompile {
        kotlin(
            """
            fun main() {
                var i = 0
                while (i <= 10) i++
            }
            """
        )

        dart(
            """
            void main() {
              int i = 0;
              while (i <= 10) {
                i++;
              }
            }
            """
        )
    }

    @Test
    fun `do while loop`() = assertCompile {
        kotlin(
            """
            fun main() {
                var i = 0
                do {
                    process(i)
                    i++
                } while (i <= 10)
            }

            fun process(number: Int) {}
            """
        )

        dart(
            """
            void main() {
              int i = 0;
              do {
                process(i);
                i++;
              } while (i <= 10);
            }

            void process(int number) {}
            """
        )
    }

    @Test
    fun `for loop`() = assertCompile {
        kotlin(
            """
            fun main() {
                for (i in 0 until 10) {
                    process(i)
                }
            }

            fun process(number: Int) {}
            """
        )

        // TODO: Remove unnecessary imports.
        dart(
            """
            import "package:dotlin/src/kotlin/ranges/ranges_ext.dt.g.dart"
                show IntRangeFactoryExt;
            import "package:dotlin/src/kotlin/ranges/ranges.dt.g.dart" show IntRange;

            void main() {
              for (int i = 0; i < 10; i += 1) {
                process(i);
              }
            }

            void process(int number) {}
            """
        )
    }

    @Test
    fun `inclusive for loop`() = assertCompile {
        kotlin(
            """
            fun main() {
                for (i in 0..10) {
                    process(i)
                    process(i)
                }
            }

            fun process(number: Int) {}
            """
        )

        // TODO: Remove unnecessary imports.
        dart(
            """
            import "package:dotlin/src/kotlin/native/int.dt.g.dart" show IntRangeTo;
            import "package:dotlin/src/kotlin/ranges/ranges.dt.g.dart" show IntRange;

            void main() {
              for (int i = 0; i <= 10; i += 1) {
                process(i);
                process(i);
              }
            }

            void process(int number) {}
            """
        )
    }

    @Test
    fun `reversed for loop`() = assertCompile {
        kotlin(
            """
            fun main() {
                for (i in 10 downTo 0) {
                    process(i)
                    process(i)
                }
            }

            fun process(number: Int) {}
            """
        )

        // TODO: Remove unnecessary imports.
        dart(
            """
            import "package:dotlin/src/kotlin/ranges/ranges_ext.dt.g.dart"
                show IntRangeFactoryExt;
            import "package:dotlin/src/kotlin/ranges/progressions.dt.g.dart"
                show IntProgression;

            void main() {
              for (int i = 10; i >= 0; i -= 1) {
                process(i);
                process(i);
              }
            }

            void process(int number) {}
            """
        )
    }

    @Test
    fun `for loop with custom step`() = assertCompile {
        kotlin(
            """
            fun main() {
                for (i in 0 until 14 step 2) {
                    process(i)
                }
            }

            fun process(number: Int) {}
            """
        )

        // TODO: Remove unnecessary imports.
        dart(
            """
            import "package:dotlin/src/kotlin/ranges/ranges_ext.dt.g.dart"
                show IntRangeFactoryExt, IntProgressionExt;
            import "package:dotlin/src/kotlin/ranges/progressions.dt.g.dart"
                show IntProgression;
            import "package:dotlin/src/kotlin/ranges/ranges.dt.g.dart" show IntRange;

            void main() {
              for (int i = 0; i < 14; i += 2) {
                process(i);
              }
            }

            void process(int number) {}
            """
        )
    }

    // Note that this doesn't make sense in Kotlin, but it compiles and works (using the last step call), so
    // we should be able to convert it as well.
    @Test
    fun `for loop with multiple step calls`() = assertCompile {
        kotlin(
            """
            fun main() {
                for (i in 0 until 14 step 1 step 2 step 3) {
                    process(i)
                }
            }

            fun process(number: Int) {}
            """
        )

        // TODO: Remove unnecessary imports.
        dart(
            """
            import "package:dotlin/src/kotlin/ranges/ranges_ext.dt.g.dart"
                show IntRangeFactoryExt, IntProgressionExt;
            import "package:dotlin/src/kotlin/ranges/progressions.dt.g.dart"
                show IntProgression;
            import "package:dotlin/src/kotlin/ranges/ranges.dt.g.dart" show IntRange;

            void main() {
              for (int i = 0; i < 14; i += 3) {
                process(i);
              }
            }

            void process(int number) {}
            """
        )
    }

    @Test
    fun `for each loop`() = assertCompile {
        kotlin(
            """
            fun main() {
                val elements = arrayOf(1, 2, 3, 4, 5)
                for (i in elements) {
                    process(i)
                }
            }

            fun process(number: Int) {}
            """
        )

        dart(
            """
            void main() {
              final List<int> elements =
                  List<int>.of(<int>[1, 2, 3, 4, 5], growable: false);
              for (int i in elements) {
                process(i);
              }
            }

            void process(int number) {}
            """
        )
    }

    @Test
    fun `while loop with single statement`() = assertCompile {
        kotlin(
            """
            fun main() {
                var i = 0
                while (i <= 10) {
                    i++
                }
            }
            """
        )

        dart(
            """
            void main() {
              int i = 0;
              while (i <= 10) {
                i++;
              }
            }
            """
        )
    }

    @Test
    fun `do while loop with single statement`() = assertCompile {
        kotlin(
            """
            fun main() {
                var i = 0
                do {
                    i++
                } while (i <= 10)
            }
            """
        )

        dart(
            """
            void main() {
              int i = 0;
              do {
                i++;
              } while (i <= 10);
            }
            """
        )
    }

    @Test
    fun `for loop with single statement`() = assertCompile {
        kotlin(
            """
            fun main() {
                for (i in 0..10) {
                    i
                }
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/native/int.dt.g.dart" show IntRangeTo;
            import "package:dotlin/src/kotlin/ranges/ranges.dt.g.dart" show IntRange;

            void main() {
              for (int i = 0; i <= 10; i += 1) {
                i;
              }
            }
            """
        )
    }
}