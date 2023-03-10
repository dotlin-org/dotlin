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

package compile.expression

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Expression: Jump")
class Jump : BaseTest {
    @Test
    fun `return`() = assertCompile {
        kotlin(
            """
            fun main() {
               null ?: return
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Return;

            void main() {
              try {
                null ?? (throw const ${'$'}Return<void>(null, 3344793));
              } on ${'$'}Return<void> catch (tmp0_return) {
                if (tmp0_return.target == 3344793) {
                  return;
                } else {
                  throw tmp0_return;
                }
              }
            }
            """
        )
    }

    @Test
    fun `return with const value`() = assertCompile {
        kotlin(
            """
            fun main() {
                test(null)
            }

            fun test(x: Int?): Int {
                val y = x ?: return -1
                return y
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Return;

            void main() {
              test(null);
            }

            int test(int? x) {
              try {
                final int y = x ?? (throw const ${'$'}Return<int>(-1, 3589296));
                return y;
              } on ${'$'}Return<int> catch (tmp0_return) {
                if (tmp0_return.target == 3589296) {
                  return tmp0_return.value;
                } else {
                  throw tmp0_return;
                }
              }
            }
            """
        )
    }

    @Test
    fun `return with non-const value`() = assertCompile {
        kotlin(
            """
            fun main() {
                test(x = null, fallback = 3)
            }

            fun test(x: Int?, fallback: Int): Int {
                val y = x ?: return fallback
                return y
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Return;

            void main() {
              test(null, 3);
            }

            int test(
              int? x,
              int fallback,
            ) {
              try {
                final int y = x ?? (throw ${'$'}Return<int>(fallback, 3607803));
                return y;
              } on ${'$'}Return<int> catch (tmp0_return) {
                if (tmp0_return.target == 3607803) {
                  return tmp0_return.value;
                } else {
                  throw tmp0_return;
                }
              }
            }
            """
        )
    }

    @Test
    fun `return in when`() = assertCompile {
        kotlin(
            """
            fun main() {
                test(x = null, fallback = 3)
            }

            fun test(x: Int?, fallback: Int): Int {
                val y = when (x) {
                    null -> return fallback
                    1 -> x * x
                    2 -> x / x
                    3 -> x + x
                    else -> x
                }

                return y
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Return;

            void main() {
              test(null, 3);
            }

            int test(
              int? x,
              int fallback,
            ) {
              try {
                final int y = () {
                  final int? tmp0_subject = x;
                  return tmp0_subject == null
                      ? throw ${'$'}Return<int>(fallback, 3611027)
                      : tmp0_subject == 1
                          ? x! * x!
                          : tmp0_subject == 2
                              ? x! ~/ x!
                              : tmp0_subject == 3
                                  ? x! + x!
                                  : x!;
                }.call();
                return y;
              } on ${'$'}Return<int> catch (tmp0_return) {
                if (tmp0_return.target == 3611027) {
                  return tmp0_return.value;
                } else {
                  throw tmp0_return;
                }
              }
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
                    null ?: continue
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
                  null ?? (throw const ${'$'}Continue(3362060));
                } on ${'$'}Continue catch (tmp0_continue) {
                  if (tmp0_continue.target == 3362060) {
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
    fun `continue in for loop`() = assertCompile {
        kotlin(
            """
            fun main() {
                for (i in 0..10) {
                    null ?: continue
                }
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/native/int.dt.g.dart" show IntRangeTo;
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Continue;
            import "package:dotlin/src/kotlin/ranges/ranges.dt.g.dart" show IntRange;
            
            void main() {
              for (int i = 0; i <= 10; i += 1) {
                try {
                  null ?? (throw const ${'$'}Continue(3362184));
                } on ${'$'}Continue catch (tmp0_continue) {
                  if (tmp0_continue.target == 3362184) {
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
    fun `continue in when`() = assertCompile {
        kotlin(
            """
            fun main() {
                val x = 0
                while (true) {
                    when (x) {
                        0 -> {}
                        else -> continue
                    }
                }
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Continue;
            
            void main() {
              final int x = 0;
              while (true) {
                try {
                  final int tmp0_subject = x;
                  if (tmp0_subject == 0) {
                  } else {
                    throw const ${'$'}Continue(3377591);
                  }
                } on ${'$'}Continue catch (tmp0_continue) {
                  if (tmp0_continue.target == 3377591) {
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
    fun `nested continue targeting outer loop`() = assertCompile {
        kotlin(
            """
            fun main() {
                loop0@ while (true)  {
                    while (false) {
                        null ?: continue@loop0
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
                    null ?? (throw const ${'$'}Continue(3370399));
                  }
                } on ${'$'}Continue catch (tmp0_continue) {
                  if (tmp0_continue.target == 3370399) {
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
                    null ?: break
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
                  null ?? (throw const ${'$'}Break(3361967));
                } on ${'$'}Break catch (tmp0_break) {
                  if (tmp0_break.target == 3361967) {
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
    fun `break in for loop`() = assertCompile {
        kotlin(
            """
            fun main() {
                for (i in 0..10) {
                    null ?: break
                }
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/native/int.dt.g.dart" show IntRangeTo;
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Break;
            import "package:dotlin/src/kotlin/ranges/ranges.dt.g.dart" show IntRange;
            
            void main() {
              for (int i = 0; i <= 10; i += 1) {
                try {
                  null ?? (throw const ${'$'}Break(3362091));
                } on ${'$'}Break catch (tmp0_break) {
                  if (tmp0_break.target == 3362091) {
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
    fun `break in when`() = assertCompile {
        kotlin(
            """
            fun main() {
                val x = 0
                while (true) {
                    when (x) {
                        0 -> {}
                        else -> break
                    }
                }
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Break;
            
            void main() {
              final int x = 0;
              while (true) {
                try {
                  final int tmp0_subject = x;
                  if (tmp0_subject == 0) {
                  } else {
                    throw const ${'$'}Break(3377498);
                  }
                } on ${'$'}Break catch (tmp0_break) {
                  if (tmp0_break.target == 3377498) {
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
    fun `nested break targeting outer loop`() = assertCompile {
        kotlin(
            """
            fun main() {
                loop0@ while (true)  {
                    while (false) {
                        null ?: break@loop0
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
                    null ?? (throw const ${'$'}Break(3370306));
                  }
                } on ${'$'}Break catch (tmp0_break) {
                  if (tmp0_break.target == 3370306) {
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
}