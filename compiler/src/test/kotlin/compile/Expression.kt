/*
 * Copyright 2021 Wilko Manger
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

@DisplayName("Compile: Expression")
class Expression : BaseTest {
    @Test
    fun `constructor call`() = assertCompile {
        kotlin(
            """
            class Test

            fun main() {
                Test()
            }
            """
        )

        dart(
            """
            class Test {}
            
            void main() {
              Test();
            }
            """
        )
    }

    @Test
    fun `private constructor call`() = assertCompile {
        kotlin(
            """
            class Test private constructor() {
                fun main() {
                    Test()
                }
            }
            """
        )

        dart(
            """
            class Test {
              Test._() : super();
              void main() {
                Test._();
              }
            }
            """
        )
    }


    @Test
    fun `method call`() = assertCompile {
        kotlin(
            """
            class Test {
                fun doIt() {}
            }

            fun main() {
                Test().doIt()
            }
            """
        )

        dart(
            """
            class Test {
              void doIt() {}
            }
            
            void main() {
              Test().doIt();
            }
            """
        )
    }

    @Test
    fun `if else`() = assertCompile {
        kotlin(
            """
            fun main() {
                val x = if (0 == 1) 0 else 1
            }
            """
        )

        dart(
            """
            void main() {
              final int x = 0 == 1 ? 0 : 1;
            }
            """
        )
    }

    @Test
    fun `if else if`() = assertCompile {
        kotlin(
            """
            fun test() = 3

            fun main() {
                val x = if (1 + 1 == 3) {
                    test()
                } else if (3 + 3 == 7){
                    test()
                } else {
                    test()
                }
            }
            """
        )

        dart(
            """
            int test() {
              return 3;
            }

            void main() {
              final int x = 1 + 1 == 3
                  ? test()
                  : 3 + 3 == 7
                      ? test()
                      : test();
            }
            """
        )
    }


    @Test
    fun `when`() = assertCompile {
        kotlin(
            """
            fun test() = 3

            fun main() {
                val x = when {
                    1 + 1 == 3 -> test()
                    3 + 3 == 7 -> test()
                    else -> test()
                }
            }
            """
        )

        dart(
            """
            int test() {
              return 3;
            }

            void main() {
              final int x = 1 + 1 == 3
                  ? test()
                  : 3 + 3 == 7
                      ? test()
                      : test();
            }
            """
        )
    }

    @Test
    fun `when with subject`() = assertCompile {
        kotlin(
            """
            fun test(x: Int): Int = x

            fun main() {
                val x = 34

                val y = when(x) {
                    4 -> test(2)
                    12 -> test(6)
                    else -> 0
                }
            }
            """
        )

        dart(
            """
            int test(int x) {
              return x;
            }
            
            void main() {
              final int x = 34;
              final int y = () {
                final int tmp0_subject = x;
                return tmp0_subject == 4
                    ? test(2)
                    : tmp0_subject == 12
                        ? test(6)
                        : 0;
              }.call();
            }
            """
        )
    }

    @Test
    fun `local anonymous function call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val x = fun (): Int {
                    return 68
                }()
            }
            """
        )

        dart(
            """
            void main() {
              final int x = () {
                return 68;
              }.call();
            }
            """
        )
    }

    @Test
    fun `local anonymous function call with one argument`() = assertCompile {
        kotlin(
            """
            fun main() {
                val x = fun (y: Int): Int {
                    return 68 * y
                }(34)
            }
            """
        )

        dart(
            """
            void main() {
              final int x = (int y) {
                return 68 * y;
              }.call(34);
            }
            """
        )
    }

    @Test
    fun `null safe method call`() = assertCompile {
        kotlin(
            """
            class Gondor {
                fun callForAid() {}
            }

            fun main() {
                val gondor: Gondor? = null
                gondor?.callForAid()
            }
            """
        )

        dart(
            """
            class Gondor {
              void callForAid() {}
            }
            
            void main() {
              final Gondor? gondor = null;
              gondor?.callForAid();
            }
            """
        )
    }

    @Test
    fun `null safe chained method call`() = assertCompile {
        kotlin(
            """
            class Gondor {
                fun callForAid(): Answer? = Answer()
            }

            class Answer

            fun main() {
                val gondor: Gondor? = null
                gondor?.callForAid()?.toString()
            }
            """
        )

        dart(
            """
            class Gondor {
              Answer? callForAid() {
                return Answer();
              }
            }
            
            class Answer {}
            
            void main() {
              final Gondor? gondor = null;
              gondor?.callForAid()?.toString();
            }
            """
        )
    }

    @Test
    fun `string interpolation with variable`() = assertCompile {
        kotlin(
            """
            fun main() {
                val amount = "Three"
                "${'$'}amount were given to the Elves, immortal, wisest and fairest of all beings."
            }
            """
        )

        dart(
            """
            void main() {
              final String amount = 'Three';
              '${'$'}{amount} were given to the Elves, immortal, wisest and fairest of all beings.';
            }
            """
        )
    }

    @Test
    fun `string interpolation with more complex expression`() = assertCompile {
        kotlin(
            """
            fun main() {
                val areDwarves = true
                "${'$'}{if (areDwarves) "Seven" else "Three"} to the Dwarf-Lords, great miners and craftsmen of ..."
            }
            """
        )

        dart(
            """
            void main() {
              final bool areDwarves = true;
              '${'$'}{areDwarves ? 'Seven' : 'Three'} to the Dwarf-Lords, great miners and craftsmen of ...';
            }
            """
        )
    }

    @Test
    fun conjunction() = assertCompile {
        kotlin(
            """
            fun main() {
                true && 2 == 0
            }
            """
        )

        dart(
            """
            void main() {
              true && 2 == 0;
            }
            """
        )
    }

    @Test
    fun disjunction() = assertCompile {
        kotlin(
            """
            fun main() {
                true || 2 == 0
            }
            """
        )

        dart(
            """
            void main() {
              true || 2 == 0;
            }
            """
        )
    }

    @Test
    fun `multiple conjunction`() = assertCompile {
        kotlin(
            """
            fun main() {
                3 == 0 && 2 == 0 && 1 == 0
            }
            """
        )

        dart(
            """
            void main() {
              3 == 0 && 2 == 0 && 1 == 0;
            }
            """
        )
    }

    @Test
    fun `multiple disjunction`() = assertCompile {
        kotlin(
            """
            fun main() {
                3 == 0 || 2 == 0 || 1 == 0
            }
            """
        )

        dart(
            """
            void main() {
              3 == 0 || 2 == 0 || 1 == 0;
            }
            """
        )
    }

    @Test
    fun `implicit cast`() = assertCompile {
        kotlin(
            """
            class Hobbit(val name: String)
            fun search(obj: Any) {
                if (obj is Hobbit && obj.name == "Frodo") {
                    "I see you"
                }
            }
            """
        )

        dart(
            """
            class Hobbit {
              Hobbit(this.name) : super();
              final String name;
            }

            void search(Object obj) {
              if (obj is Hobbit && (obj as Hobbit).name == 'Frodo') {
                'I see you';
              }
            }
            """
        )
    }

    @Test
    fun `instance of`() = assertCompile {
        kotlin(
            """
            fun main() {
                9 is Int
            }
            """
        )

        dart(
            """
            void main() {
              9 is int;
            }
            """
        )
    }

    @Test
    fun `not instance of`() = assertCompile {
        kotlin(
            """
            fun main() {
                9 !is Int
            }
            """
        )

        dart(
            """
            void main() {
              9 is! int;
            }
            """
        )
    }

    @Test
    fun `===`() = assertCompile {
        kotlin(
            """
            class Test

            fun main() {
               Test() === Test()
            }
            """
        )

        dart(
            """
            class Test {}

            void main() {
              identical(Test(), Test());
            }
            """
        )
    }
}