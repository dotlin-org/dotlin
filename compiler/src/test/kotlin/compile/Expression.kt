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

@DisplayName("Compile: Expression")
class Expression : BaseTest {
    @Test
    fun `access top-level property`() = assertCompile {
        kotlin(
            """
            val topLevelProperty: Int = 0

            fun main() {
                topLevelProperty
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            final int topLevelProperty = 0;
            void main() {
              topLevelProperty;
            }
            """
        )
    }

    @Test
    fun `access private top-level property`() = assertCompile {
        kotlin(
            """
            private val topLevelProperty: Int = 0

            fun main() {
                topLevelProperty
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            final int _topLevelProperty = 0;
            void main() {
              _topLevelProperty;
            }
            """
        )
    }

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
            import 'package:meta/meta.dart';

            @sealed
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
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
              @nonVirtual
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
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              @nonVirtual
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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

            @sealed
            class Gondor {
              @nonVirtual
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
            import 'package:meta/meta.dart';

            @sealed
            class Gondor {
              @nonVirtual
              Answer? callForAid() {
                return Answer();
              }
            }
            
            @sealed
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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

            @sealed
            class Hobbit {
              Hobbit(this.name) : super();
              @nonVirtual
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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

            @sealed
            class Test {}

            void main() {
              identical(Test(), Test());
            }
            """
        )
    }

    @Test
    fun `!==`() = assertCompile {
        kotlin(
            """
            class Test

            fun main() {
               Test() !== Test()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {}

            void main() {
              !identical(Test(), Test());
            }
            """
        )
    }

    @Test
    fun `Boolean not`() = assertCompile {
        kotlin(
            """
            fun main() {
               !(1 == 0)
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              !(1 == 0);
            }
            """
        )
    }

    @Test
    fun `overloaded not`() = assertCompile {
        kotlin(
            """
            class Test {
                operator fun not() = Test()
            }

            fun main() {
                !Test()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              @nonVirtual
              Test not() {
                return Test();
              }
            }

            void main() {
              Test().not();
            }
            """
        )
    }

    @Test
    fun elvis() = assertCompile {
        kotlin(
            """
            fun main() {
               null ?: ""
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              null ?? '';
            }
            """
        )
    }

    @Test
    fun `call super`() = assertCompile {
        kotlin(
            """
                open class Base {
                    open fun sayHello() = "Hello."
                }

                class Test : Base() {
                    override fun sayHello() = super.sayHello()
                }
                """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            class Base {
              String sayHello() {
                return 'Hello.';
              }
            }

            @sealed
            class Test extends Base {
              @override
              String sayHello() {
                return super.sayHello();
              }
            }
            """
        )
    }

    @Test
    fun `try-catch`() = assertCompile {
        kotlin(
            """
            fun main() {
                val x = try {
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
            import 'package:meta/meta.dart';

            void main() {
              final int x = () {
                try {
                  return thisThrows();
                } on Exception catch (e) {
                  return thisThrows();
                }
              }.call();
            }

            int thisThrows() {
              throw Exception('You done did it now');
            }
            """
        )
    }

    @Test
    fun `try-catch in getter`() = assertCompile {
        kotlin(
            """
            class Test {
                val x: Int
                    get() = try {
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
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              @nonVirtual
              int get x {
                return () {
                  try {
                    return thisThrows();
                  } on Exception catch (e) {
                    return thisThrows();
                  }
                }.call();
              }
            }

            int thisThrows() {
              throw Exception('You done did it now');
            }
            """
        )
    }

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
            import 'package:meta/meta.dart';

            void main() {
              try {
                null ?? (throw const ${'$'}Return<void>(null));
              } on ${'$'}Return<void> catch (tmp0_return) {
                return;
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
            import 'package:meta/meta.dart';

            void main() {
              test(null);
            }

            int test(int? x) {
              try {
                final int y = x ?? (throw const ${'$'}Return<int>(-1));
                return y;
              } on ${'$'}Return<int> catch (tmp0_return) {
                return tmp0_return.value;
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
            import 'package:meta/meta.dart';

            void main() {
              test(null, 3);
            }

            int test(
              int? x,
              int fallback,
            ) {
              try {
                final int y = x ?? (throw ${'$'}Return<int>(fallback));
                return y;
              } on ${'$'}Return<int> catch (tmp0_return) {
                return tmp0_return.value;
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
            import 'package:meta/meta.dart';

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
                      ? throw ${'$'}Return<int>(fallback)
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
                return tmp0_return.value;
              }
            }
            """
        )
    }

    @Test
    fun `when with subject not-null smart-cast`() = assertCompile {
        kotlin(
            """
            fun test(y: Int?) {
                val x = when (y) {
                        null -> y
                        else -> y * y
                    }
            }

            fun main() {
                test(0)
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void test(int? y) {
              final int? x = () {
                final int? tmp0_subject = y;
                return tmp0_subject == null ? y : y! * y!;
              }.call();
            }

            void main() {
              test(0);
            }
            """
        )
    }

    @Test
    fun `when with subject type smart-cast`() = assertCompile {
        kotlin(
            """
            fun test(y: Any) {
                val x = when (y) {
                        is Int -> y + y
                        else -> -1
                    }
            }

            fun main() {
                test(0)
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void test(Object y) {
              final int x = () {
                final Object tmp0_subject = y;
                return tmp0_subject is int ? (y as int) + (y as int) : -1;
              }.call();
            }

            void main() {
              test(0);
            }
            """
        )
    }

    @Test
    fun `when with subject type smart-cast using subject as-is`() = assertCompile {
        kotlin(
            """
            fun test(y: Any) {
                val x = when (y) {
                        is Int -> y
                        else -> -1
                    }
            }

            fun main() {
                test(0)
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void test(Object y) {
              final int x = () {
                final Object tmp0_subject = y;
                return tmp0_subject is int ? y as int : -1;
              }.call();
            }

            void main() {
              test(0);
            }
            """
        )
    }

    @Test
    fun `not-null assertion`() = assertCompile {
        kotlin(
            """
            fun main() {
                null!!
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              null!;
            }
            """
        )
    }

    @Test
    fun `unary minus on Int literal`() = assertCompile {
        kotlin(
            """
            fun main() {
                -1
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              -1;
            }
            """
        )
    }

    @Test
    fun `unary minus on Int variable`() = assertCompile {
        kotlin(
            """
            fun main() {
                val x = 3
                -x
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final int x = 3;
              -x;
            }
            """
        )
    }

    @Test
    fun `not equals`() = assertCompile {
        kotlin(
            """
            fun main() {
                0 != 1
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              0 != 1;
            }
            """
        )
    }

    @Test
    fun `shl on primitive number`() = assertCompile {
        kotlin(
            """
            fun main() {
                0 shl 1
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              0 << 1;
            }
            """
        )
    }

    @Test
    fun `shr on primitive number`() = assertCompile {
        kotlin(
            """
            fun main() {
                0 shr 1
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              0 >> 1;
            }
            """
        )
    }

    @Test
    fun `ushr on primitive number`() = assertCompile {
        kotlin(
            """
            fun main() {
                0 ushr 1
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              0 >>> 1;
            }
            """
        )
    }

    @Test
    fun `and on primitive number`() = assertCompile {
        kotlin(
            """
            fun main() {
                0 and 1
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              0 & 1;
            }
            """
        )
    }

    @Test
    fun `or on primitive number`() = assertCompile {
        kotlin(
            """
            fun main() {
                0 or 1
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              0 | 1;
            }
            """
        )
    }

    @Test
    fun `xor on primitive number`() = assertCompile {
        kotlin(
            """
            fun main() {
                0 xor 1
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              0 ^ 1;
            }
            """
        )
    }

    @Test
    fun `inv on primitive number`() = assertCompile {
        kotlin(
            """
            fun main() {
                1.inv()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              ~1;
            }
            """
        )
    }

    @Test
    fun `const Double Infinity`() = assertCompile {
        kotlin(
            """
            const val x = 1.0 / 0.0
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            const double x = 1.0 / 0.0;
            """
        )
    }

    @Test
    fun `const Double -Infinity`() = assertCompile {
        kotlin(
            """
            const val x = -(1.0 / 0.0)
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            const double x = -(1.0 / 0.0);
            """
        )
    }

    @Test
    fun `const Double NaN`() = assertCompile {
        kotlin(
            """
            const val x = -(0.0 / 0.0)
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            const double x = -(0.0 / 0.0);
            """
        )
    }

    @Nested
    @DisplayName("Increment & Decrement")
    inner class IncrementDecrement {
        @Test
        fun `prefix increment`() = assertCompile {
            kotlin(
                """
                fun main() {
                    var x = 1
                    ++x
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                void main() {
                  int x = 1;
                  ++x;
                }
                """
            )
        }

        @Test
        fun `prefix decrement`() = assertCompile {
            kotlin(
                """
                fun main() {
                    var x = 1
                    --x
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                void main() {
                  int x = 1;
                  --x;
                }
                """
            )
        }

        @Test
        fun `postfix increment`() = assertCompile {
            kotlin(
                """
                fun main() {
                    var x = 1
                    x++
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                void main() {
                  int x = 1;
                  x++;
                }
                """
            )
        }

        @Test
        fun `postfix decrement`() = assertCompile {
            kotlin(
                """
                fun main() {
                    var x = 1
                    x--
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                void main() {
                  int x = 1;
                  x--;
                }
                """
            )
        }

        @Test
        fun `overloaded prefix increment`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun inc() = Test()
                }

                fun main() {
                    var x = Test()
                    ++x
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  Test inc() {
                    return Test();
                  }
                }

                void main() {
                  Test x = Test();
                  x = x.inc();
                }
                """
            )
        }

        @Test
        fun `overloaded prefix decrement`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun dec() = Test()
                }

                fun main() {
                    var x = Test()
                    --x
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  Test dec() {
                    return Test();
                  }
                }

                void main() {
                  Test x = Test();
                  x = x.dec();
                }
                """
            )
        }

        @Test
        fun `overloaded postfix increment`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun inc() = Test()
                }

                fun main() {
                    var x = Test()
                    x++
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  Test inc() {
                    return Test();
                  }
                }

                void main() {
                  Test x = Test();
                  () {
                    final Test tmp0 = x;
                    x = tmp0.inc();
                    return tmp0;
                  }.call();
                }
                """
            )
        }

        @Test
        fun `overloaded postfix decrement`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun dec() = Test()
                }

                fun main() {
                    var x = Test()
                    x--
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  Test dec() {
                    return Test();
                  }
                }

                void main() {
                  Test x = Test();
                  () {
                    final Test tmp0 = x;
                    x = tmp0.dec();
                    return tmp0;
                  }.call();
                }
                """
            )
        }
    }

    @Nested
    @DisplayName("Indexed Access")
    inner class IndexedAccess {
        @Test
        fun `indexed get`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun get(index: Int): Int {
                        return index
                    }
                }

                fun main() {
                    val t = Test()
                    t[0]
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  int get(int index) {
                    return index;
                  }

                  @nonVirtual
                  int operator [](int index) => this.get(index);
                }

                void main() {
                  final Test t = Test();
                  t[0];
                }
                """
            )
        }

        @Test
        fun `index get with multiple parameters`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun get(index: Int, index2: Int): Int {
                        return index + index2
                    }
                }

                fun main() {
                    Test()[1, 2]
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  int get(
                    int index,
                    int index2,
                  ) {
                    return index + index2;
                  }
                }

                void main() {
                  Test().get(1, 2);
                }
                """
            )
        }

        @Test
        fun `indexed set`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun set(index: Int, value: Boolean) = value
                }

                fun main() {
                    Test()[4] = true
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  bool set(
                    int index,
                    bool value,
                  ) {
                    return value;
                  }

                  @nonVirtual
                  void operator []=(
                    int index,
                    bool value,
                  ) =>
                      this.set(index, value);
                }

                void main() {
                  Test()[4] = true;
                }
                """
            )
        }

        @Test
        fun `indexed set with multiple parameters`() = assertCompile {
            kotlin(
                """
                class Test {
                    operator fun set(index: Int, index2: Int, value: Boolean) {}
                }

                fun main() {
                    Test()[3, 4] = true
                }
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class Test {
                  @nonVirtual
                  void set(
                    int index,
                    int index2,
                    bool value,
                  ) {}
                }

                void main() {
                  Test().set(3, 4, true);
                }
                """
            )
        }
    }

    @Test
    fun `call infix method`() = assertCompile {
        kotlin(
            """
            class Test {
                infix fun add(other: Test) = Test()
            }

            fun main() {
                Test() add Test()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              @nonVirtual
              Test add(Test other) {
                return Test();
              }
            }

            void main() {
              Test().add(Test());
            }
            """
        )
    }

    @Test
    fun `call infix extension method`() = assertCompile {
        kotlin(
            """
            class Test

            infix fun Test.add(other: Test) = Test()

            fun main() {
                Test() add Test()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {}

            void main() {
              Test().add(Test());
            }

            extension ${'$'}TestExtensions${'$'}21ad63ef on Test {
              Test add(Test other) {
                return Test();
              }
            }
            """
        )
    }

    @Test
    fun `call extension method on Int`() = assertCompile {
        kotlin(
            """
            fun Int.add(other: Int) = this + other

            fun main() {
                1.add(2)
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              ${'$'}KotlinIntExtensions${'$'}21ad63ef(1).add(2);
            }

            extension ${'$'}KotlinIntExtensions${'$'}21ad63ef on int {
              int add(int other) {
                return this + other;
              }
            }
            """
        )
    }

    @Test
    fun `constructor call with type arguments`() = assertCompile {
        kotlin(
            """
            interface WritingSurface
            interface Stone : WritingSurface
            interface Paper : WritingSurface

            class Scribe<T : WritingSurface> {
                fun write() {}
            }

            fun writeDown() {
                val scribe = Scribe<Paper>()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            abstract class WritingSurface {}

            abstract class Stone implements WritingSurface {}

            abstract class Paper implements WritingSurface {}

            @sealed
            class Scribe<T extends WritingSurface> {
              @nonVirtual
              void write() {}
            }

            void writeDown() {
              final Scribe<Paper> scribe = Scribe<Paper>();
            }
            """
        )
    }

    @Test
    fun `set top-level property with explicit setter`() = assertCompile {
        kotlin(
            """
            var x: Boolean
                get() = false
                set(value) {}

            fun main () {
                x = true
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            bool get x {
              return false;
            }

            void set x(bool value) {}
            void main() {
              x = true;
            }
            """
        )
    }
}