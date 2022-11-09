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

@DisplayName("Compile: Lambda")
class Lambda : BaseTest {
    @Test
    fun lambda() = assertCompile {
        kotlin(
            """
            fun compute(action: () -> Unit) {}

            fun main() {
                compute {
                    doSomething()
                }
            }

            fun doSomething() {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void compute(void Function() action) {}
            void main() {
              compute(() {
                doSomething();
              });
            }

            void doSomething() {}
            """
        )
    }

    @Test
    fun `lambda with captured value`() = assertCompile {
        kotlin(
            """
            fun compute(action: () -> Unit) {}

            fun main() {
                val answer = 42

                compute {
                    doSomething(answer)
                }
            }

            fun doSomething(x: Int) {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void compute(void Function() action) {}
            void main() {
              final int answer = 42;
              compute(() {
                doSomething(answer);
              });
            }

            void doSomething(int x) {}
            """
        )
    }

    @Test
    fun `lambda with parameter`() = assertCompile {
        kotlin(
            """
            fun compute(action: (Int) -> Unit) {}

            fun main() {
                compute { x ->
                    doSomething(x)
                }
            }

            fun doSomething(x: Int) {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void compute(void Function(int) action) {}
            void main() {
              compute((int x) {
                doSomething(x);
              });
            }

            void doSomething(int x) {}
            """
        )
    }

    @Test
    fun `lambda with return`() = assertCompile {
        kotlin(
            """
            fun compute(action: (Int) -> Int) {}

            fun main() {
                compute { x ->
                    doSomething(x)
                }
            }

            fun doSomething(x: Int): Int = x + x
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void compute(int Function(int) action) {}
            void main() {
              compute((int x) {
                return doSomething(x);
              });
            }

            int doSomething(int x) {
              return x + x;
            }
            """
        )
    }
}