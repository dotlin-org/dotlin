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
import DefaultValue
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Compile: Function")
class Function : BaseTest {
    @Test
    fun function() = assertCompile {
        kotlin("fun test() {}")
        dart(
            """
            void test() {}
            """
        )
    }

    @Test
    fun `private function`() = assertCompile {
        kotlin("private fun test() {}")
        dart(
            """
            void _test() {}
            """
        )
    }

    @Test
    fun `function with single parameter`() = assertCompile {
        kotlin("fun test(arg: String) {}")

        dart(
            """
            void test(String arg) {}
            """
        )
    }

    @Test
    fun `function with single parameter with default value`() = assertCompile {
        kotlin("""fun test(arg: String = "test") {}""")

        dart(
            """
            void test({String arg = "test"}) {}
            """
        )
    }

    @Test
    fun `function with single parameter with complex default value`() = assertCompile {
        kotlin(
            """
            fun returnsString(): String {
                return "test"
            }

            fun test(arg: String = returnsString()) {}
            """
        )

        dart(
            """
            String returnsString() {
              return "test";
            }

            void test({String? arg = null}) {
              arg = arg == null ? returnsString() : arg;
            }
            """
        )
    }

    @Test
    fun `function with single nullable parameter with default value`() = assertCompile {
        kotlin("fun test(arg: String? = null) {}")

        dart(
            """
            void test({String? arg = null}) {}
            """
        )
    }

    @Test
    fun `function with single nullable parameter with complex non-null default value`() = assertCompile {
        kotlin(
            """
            fun returnsString(): String {
                return "test"
            }

            fun test(arg: String? = returnsString()) {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;

            String returnsString() {
              return "test";
            }

            void test({dynamic arg = const _$DefaultValue()}) {
              arg = arg == const _$DefaultValue() ? returnsString() : arg as String?;
            }

            @sealed
            class _$DefaultValue {
              const _$DefaultValue();
              @nonVirtual
              dynamic noSuchMethod(Invocation invocation) {}
            }
            """
        )
    }

    @Test
    fun `function with single nullable implementable parameter with complex non-null default value`() = assertCompile {
        kotlin(
            """
            class Something

            fun returnsSomething(): Something = Something()

            fun test(arg: Something? = returnsSomething()) {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show sealed, nonVirtual;

            @sealed
            class Something {}

            Something returnsSomething() {
              return Something();
            }

            void test({Something? arg = const _${'$'}DefaultSomethingValue()}) {
              arg = arg == const _${'$'}DefaultSomethingValue() ? returnsSomething() : arg;
            }

            @sealed
            class _${'$'}DefaultSomethingValue implements Something {
              const _${'$'}DefaultSomethingValue();
              @nonVirtual
              dynamic noSuchMethod(Invocation invocation) {}
            }
            """
        )
    }

    @Test
    fun `function with single nullable enum parameter with complex non-null default value`() = assertCompile {
        kotlin(
            """
            enum class CharacterType {
                protagonist,
                antagonist,
            }

            fun returnsCharacterType(): CharacterType = CharacterType.protagonist

            fun test(arg: CharacterType? = returnsCharacterType()) {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;

            enum CharacterType {
              protagonist._(),
              antagonist._();

              const CharacterType._();
            }

            CharacterType returnsCharacterType() {
              return CharacterType.protagonist;
            }

            void test({dynamic arg = const _$DefaultValue()}) {
              arg = arg == const _$DefaultValue()
                  ? returnsCharacterType()
                  : arg as CharacterType?;
            }

            CharacterType ${'$'}CharacterType${'$'}valueOf(String value) =>
                CharacterType.values.firstWhere((CharacterType v) => v.name == value);

            @sealed
            class _$DefaultValue {
              const _$DefaultValue();
              @nonVirtual
              dynamic noSuchMethod(Invocation invocation) {}
            }
            """
        )
    }

    @Test
    fun `function with two parameters`() = assertCompile {
        kotlin("fun test(arg1: String, arg2: Int) {}")

        dart(
            """
            void test(
              String arg1,
              int arg2,
            ) {}
            """
        )
    }

    @Test
    fun `function with two parameters with default values`() = assertCompile {
        kotlin("""fun test(arg1: String = "test", arg2: Int = 96) {}""")

        dart(
            """
            void test({
              String arg1 = "test",
              int arg2 = 96,
            }) {}
            """
        )
    }

    @Test
    fun `function with two parameters with complex default values`() = assertCompile {
        kotlin(
            """
            fun returnsString(): String {
                return "test"
            }

            fun test(arg1: String = returnsString(), arg2: String = returnsString()) {}
            """
        )

        dart(
            """
            String returnsString() {
              return "test";
            }

            void test({
              String? arg1 = null,
              String? arg2 = null,
            }) {
              arg1 = arg1 == null ? returnsString() : arg1;
              arg2 = arg2 == null ? returnsString() : arg2;
            }
            """
        )
    }

    @Test
    fun `function with two nullable parameters with default values`() = assertCompile {
        kotlin("fun test(arg1: String? = null, arg2: String? = null) {}")

        dart(
            """
            void test({
              String? arg1 = null,
              String? arg2 = null,
            }) {}
            """
        )
    }

    @Test
    fun `function with two nullable parameters with complex non-null default values`() = assertCompile {
        kotlin(
            """
            fun returnsString(): String {
                return "test"
            }

            fun test(arg1: String? = returnsString(), arg2: String? = returnsString()) {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart" show nonVirtual, sealed;

            String returnsString() {
              return "test";
            }

            void test({
              dynamic arg1 = const _$DefaultValue(),
              dynamic arg2 = const _$DefaultValue(),
            }) {
              arg1 = arg1 == const _$DefaultValue() ? returnsString() : arg1 as String?;
              arg2 = arg2 == const _$DefaultValue() ? returnsString() : arg2 as String?;
            }

            @sealed
            class _$DefaultValue {
              const _$DefaultValue();
              @nonVirtual
              dynamic noSuchMethod(Invocation invocation) {}
            }
            """
        )
    }

    @Test
    fun `function with two parameters, the second referencing the first`() = assertCompile {
        kotlin("fun test(arg1: Int = 1, arg2: Int = arg1) {}")

        dart(
            """
            void test({
              int arg1 = 1,
              int? arg2 = null,
            }) {
              arg2 = arg2 == null ? arg1 : arg2;
            }
            """
        )
    }

    @Test
    fun `function with vararg parameter`() = assertCompile {
        kotlin("fun test(vararg args: String) {}")

        dart(
            """
            void test(List<String> args) {}
            """
        )
    }

    @Test
    fun `calling a function with vararg parameter`() = assertCompile {
        kotlin(
            """
            fun test(vararg args: String) {}

            fun main() {
                test("abc", "def", "ghi")
            }
            """
        )

        dart(
            """
            void test(List<String> args) {}
            void main() {
              test(<String>["abc", "def", "ghi"]);
            }
            """
        )
    }

    @Test
    fun `calling a function with vararg parameter and normal parameter`() = assertCompile {
        kotlin(
            """
            fun test(vararg args: String, x: Int) {}

            fun main() {
                test("abc", "def", "ghi", x = 0)
            }
            """
        )

        dart(
            """
            void test(
              List<String> args,
              int x,
            ) {}
            void main() {
              test(<String>["abc", "def", "ghi"], 0);
            }
            """
        )
    }

    @Test
    fun `function with type parameter`() = assertCompile {
        kotlin("fun <T> test() {}")

        dart(
            """
            void test<T>() {}
            """
        )
    }

    @Test
    fun `function with two type parameters`() = assertCompile {
        kotlin("fun <T0, T1> test() {}")

        dart(
            """
            void test<T0, T1>() {}
            """
        )
    }

    @Test
    fun `function with type parameter bound`() = assertCompile {
        kotlin("fun <T : Int> test() {}")

        dart(
            """
            void test<T extends int>() {}
            """
        )
    }

    @Test
    fun `function with multiple type parameter bounds`() = assertCompile {
        kotlin(
            """
            interface Memoir {
                fun intrigue()
            }

            interface Novel {
                fun enjoy()
            }

            fun <T> test(book: T) where T : Memoir, T : Novel {
                book.enjoy()
                book.intrigue()
                lookUp(book)
            }

            fun lookUp(memoir: Memoir) {}
            """
        )

        dart(
            """
            abstract class Memoir {
              void intrigue();
            }

            abstract class Novel {
              void enjoy();
            }

            void test<T extends Object>(T book) {
              (book as Novel).enjoy();
              (book as Memoir).intrigue();
              lookUp(book as Memoir);
            }

            void lookUp(Memoir memoir) {}
            """
        )
    }

    @Test
    fun `function with multiple type parameter bounds with common super type`() = assertCompile {
        kotlin(
            """
            interface Book

            interface Memoir : Book {
                fun intrigue()
            }

            interface Novel : Book {
                fun enjoy()
            }

            fun <T> test(book: T) where T : Memoir, T : Novel {
                book.enjoy()
                book.intrigue()
                lookUp(book)
            }

            fun lookUp(memoir: Memoir) {}
            """
        )

        dart(
            """
            abstract class Book {}

            abstract class Memoir implements Book {
              void intrigue();
            }

            abstract class Novel implements Book {
              void enjoy();
            }

            void test<T extends Book>(T book) {
              (book as Novel).enjoy();
              (book as Memoir).intrigue();
              lookUp(book as Memoir);
            }

            void lookUp(Memoir memoir) {}
            """
        )
    }

    @Test
    fun `function with multiple type parameter bounds with common super type calling method from common super type`() =
        assertCompile {
            kotlin(
                """
                interface Book {
                    fun read()
                }

                interface Memoir : Book {
                    fun intrigue()
                }

                interface Novel : Book {
                    fun enjoy()
                }

                fun <T> test(book: T) where T : Memoir, T : Novel {
                    book.read()
                    book.enjoy()
                    book.intrigue()
                    lookUp(book)
                }

                fun lookUp(memoir: Memoir) {}
                """
            )

            dart(
                """
                abstract class Book {
                  void read();
                }

                abstract class Memoir implements Book {
                  void intrigue();
                }

                abstract class Novel implements Book {
                  void enjoy();
                }

                void test<T extends Book>(T book) {
                  book.read();
                  (book as Novel).enjoy();
                  (book as Memoir).intrigue();
                  lookUp(book as Memoir);
                }

                void lookUp(Memoir memoir) {}
                """
            )
        }

    @Test
    fun `function with multiple type parameter bounds one of which is nullable`() = assertCompile {
        kotlin(
            """
            interface Memoir {
                fun intrigue()
            }

            interface Novel {
                fun enjoy()
            }

            fun <T> test(book: T) where T : Memoir?, T : Novel {
                book.enjoy()
                book.intrigue()
                lookUp(book)
            }

            fun lookUp(memoir: Memoir) {}
            """
        )

        dart(
            """
            abstract class Memoir {
              void intrigue();
            }

            abstract class Novel {
              void enjoy();
            }

            void test<T extends Object>(T book) {
              (book as Novel).enjoy();
              (book as Memoir).intrigue();
              lookUp(book as Memoir);
            }

            void lookUp(Memoir memoir) {}
            """
        )
    }

    // Technically this doesn't make much sense, but good to translate it as-is, since the Kotlin compiler doesn't
    // give a warning.
    @Test
    fun `function with multiple type parameter bounds one of which is nullable and a null safe operator is used on`() =
        assertCompile {
            kotlin(
                """
                interface Memoir {
                    fun intrigue()
                }

                interface Novel {
                    fun enjoy()
                }

                fun <T> test(book: T) where T : Memoir?, T : Novel {
                    book.enjoy()
                    book?.intrigue()
                    lookUp(book)
                }

                fun lookUp(memoir: Memoir) {}
                """
            )

            dart(
                """
                abstract class Memoir {
                  void intrigue();
                }

                abstract class Novel {
                  void enjoy();
                }

                void test<T extends Object>(T book) {
                  (book as Novel).enjoy();
                  (book as Memoir?)?.intrigue();
                  lookUp(book as Memoir);
                }

                void lookUp(Memoir memoir) {}
                """
            )
        }

    @Test
    fun `function with multiple type parameter bounds with common super type one which is nullable`() = assertCompile {
        kotlin(
            """
            interface Book

            interface Memoir : Book {
                fun intrigue()
            }

            interface Novel : Book {
                fun enjoy()
            }

            fun <T> test(book: T) where T : Memoir?, T : Novel {
                book.enjoy()
                book.intrigue()
                lookUp(book)
            }

            fun lookUp(memoir: Memoir) {}
            """
        )

        dart(
            """
            abstract class Book {}

            abstract class Memoir implements Book {
              void intrigue();
            }

            abstract class Novel implements Book {
              void enjoy();
            }

            void test<T extends Book>(T book) {
              (book as Novel).enjoy();
              (book as Memoir).intrigue();
              lookUp(book as Memoir);
            }

            void lookUp(Memoir memoir) {}
            """
        )
    }

    @Test
    fun `function with multiple type parameter bounds both of which are nullable`() = assertCompile {
        kotlin(
            """
            interface Memoir {
                fun intrigue()
            }

            interface Novel {
                fun enjoy()
            }

            fun <T> test(book: T) where T : Memoir?, T : Novel? {
                book?.enjoy()
                book?.intrigue()
                lookUp(book)
            }

            fun lookUp(memoir: Memoir?) {}
            """
        )

        dart(
            """
            abstract class Memoir {
              void intrigue();
            }

            abstract class Novel {
              void enjoy();
            }

            void test<T>(T book) {
              (book as Novel?)?.enjoy();
              (book as Memoir?)?.intrigue();
              lookUp(book as Memoir?);
            }

            void lookUp(Memoir? memoir) {}
            """
        )
    }

    @Test
    fun `function with multiple type parameter bounds with common super type both of which are nullable`() =
        assertCompile {
            kotlin(
                """
                interface Book

                interface Memoir : Book {
                    fun intrigue()
                }

                interface Novel : Book {
                    fun enjoy()
                }

                fun <T> test(book: T) where T : Memoir?, T : Novel? {
                    book?.enjoy()
                    book?.intrigue()
                    lookUp(book)
                }

                fun lookUp(memoir: Memoir?) {}
                """
            )

            dart(
                """
                abstract class Book {}

                abstract class Memoir implements Book {
                  void intrigue();
                }

                abstract class Novel implements Book {
                  void enjoy();
                }

                void test<T extends Book?>(T book) {
                  (book as Novel?)?.enjoy();
                  (book as Memoir?)?.intrigue();
                  lookUp(book as Memoir?);
                }

                void lookUp(Memoir? memoir) {}
                """
            )
        }

    @Nested
    inner class Overloading : BaseTest {
        @Test
        fun `function with overload`() = assertCompile {
            kotlin(
                """
                fun giveCookie() {}

                fun giveCookie(message: String) {}
                """
            )

            dart(
                """
                void giveCookie() {}
                void giveCookie${'$'}m639e992809d99644(String message) {}
                """
            )
        }

        @Test
        fun `function with overload with @DartName`() = assertCompile {
            kotlin(
                """
                fun giveCookie() {}

                @DartName("giveCookieWhileSaying")
                fun giveCookie(message: String) {}
                """
            )

            dart(
                """
                void giveCookie() {}
                void giveCookieWhileSaying(String message) {}
                """
            )
        }

        @Test
        fun `function with @DartName with overload`() = assertCompile {
            kotlin(
                """
                @DartName("gimme")
                fun giveCookie() {}

                fun giveCookie(message: String) {}
                """
            )

            dart(
                """
                void gimme() {}
                void giveCookie(String message) {}
                """
            )
        }

        @Test
        fun `function with two overloads`() = assertCompile {
            kotlin(
                """
                fun giveCookie() {}

                fun giveCookie(message: String) {}

                fun giveCookie(message: String, wrapping: String) {}
                """
            )

            dart(
                """
                void giveCookie() {}
                void giveCookie${'$'}m639e992809d99644(String message) {}
                void giveCookie${'$'}m6543678d01b9145(
                  String message,
                  String wrapping,
                ) {}
                """
            )
        }

        @Test
        fun `function with three overloads`() = assertCompile {
            kotlin(
                """
                fun giveCookie() {}

                fun giveCookie(message: String) {}

                fun giveCookie(message: String, wrapping: String) {}

                fun giveCookie(message: String, wrapping: String, extra: String) {}
                """
            )

            dart(
                """
                void giveCookie() {}
                void giveCookie${'$'}m639e992809d99644(String message) {}
                void giveCookie${'$'}m6543678d01b9145(
                  String message,
                  String wrapping,
                ) {}
                void giveCookie${'$'}4a16651884738102(
                  String message,
                  String wrapping,
                  String extra,
                ) {}
                """
            )
        }

        @Test
        fun `function with overload that already has parameters`() = assertCompile {
            kotlin(
                """
                fun sayHello(greeting: String) {}

                fun sayHello(greeting: String, friendly: Boolean) {}
                """
            )

            dart(
                """
                void sayHello(String greeting) {}
                void sayHello${'$'}4849c719b52c91b3(
                  String greeting,
                  bool friendly,
                ) {}
                """
            )
        }


        @Test
        fun `function with overload that have same amount of parameters`() = assertCompile {
            kotlin(
                """
                fun compare(a: Int, b: Int) {}
                fun compare(that: String, other: String) {}
                """
            )

            dart(
                """
                void compare(
                  int a,
                  int b,
                ) {}
                void compare${'$'}m5fe76181385ff432(
                  String that,
                  String other,
                ) {}
                """
            )
        }

        @Test
        fun `function with overload that have same amount of parameters and same names`() = assertCompile {
            kotlin(
                """
                fun compare(a: Int, b: Int) {}
                fun compare(a: String, b: String) {}
                fun compare(a: Double, b: Double) {}
                """
            )

            dart(
                """
                void compare(
                  int a,
                  int b,
                ) {}
                void compare${'$'}m5fe76181385ff432(
                  String a,
                  String b,
                ) {}
                void compare${'$'}m28c5005e60a77136(
                  double a,
                  double b,
                ) {}
                """
            )
        }

        @Test
        fun `function with overload that have same amount of parameters, same names and same Dart types`() =
            assertCompile {
                kotlin(
                    """
                    fun compare(that: Set<Int>, other: Set<Int>) {}
                    fun compare(that: List<Int>, other:  List<Int>) {}
                    fun compare(that: ImmutableList<Int>, other: ImmutableList<Int>) {}
                    """
                )

                dart(
                    """
                    void compare(
                      Set<int> that,
                      Set<int> other,
                    ) {}
                    void compare${'$'}7f270b79c3b577da(
                      List<int> that,
                      List<int> other,
                    ) {}
                    void compare${'$'}67242747a13aa7ae(
                      List<int> that,
                      List<int> other,
                    ) {}
                    """
                )
            }

        @Test
        fun `function with three overloads, two of which have similar parameters`() = assertCompile {
            kotlin(
                """
                fun giveCookie() {}

                fun giveCookie(message: String) {}

                fun giveCookie(message: String, wrapping: String) {}

                fun giveCookie(message: String, wrapping: Int) {}
                """
            )

            dart(
                """
                void giveCookie() {}
                void giveCookie${'$'}m639e992809d99644(String message) {}
                void giveCookie${'$'}m6543678d01b9145(
                  String message,
                  String wrapping,
                ) {}
                void giveCookie${'$'}f0f03bfebe04b1c(
                  String message,
                  int wrapping,
                ) {}
                """
            )
        }

        @Test
        fun `function with overload that has the same parameters but one is generic`() = assertCompile {
            kotlin(
                """
                fun <T> compare(a: Int, b: Int) {}
                fun compare(a: Int, b: Int) {}
                """
            )

            dart(
                """
                void compare<T>(
                  int a,
                  int b,
                ) {}
                void compare${'$'}m1e5a0905b7b7657b(
                  int a,
                  int b,
                ) {}
                """
            )
        }

        @Test
        fun `function with overload that has the same parameters are generic and have same type parameter name`() =
            assertCompile {
                kotlin(
                    """
                    fun <T> compare(a: Int, b: Int) {}
                    fun <T : String> compare(a: T, b: T) {}
                    """
                )

                dart(
                    """
                    void compare<T>(
                      int a,
                      int b,
                    ) {}
                    void compare${'$'}m1c704916d538b69a<T extends String>(
                      T a,
                      T b,
                    ) {}
                    """
                )
            }

        @Test
        fun `function with overload that has the same parameters are generic and have same type parameter name but multiple bounds`() =
            assertCompile {
                kotlin(
                    """
                    interface Marker1
                    interface Marker2

                    fun <T> compare(a: Int, b: Int) {}
                    fun <T> compare(a: T, b: T) where T : Marker1, T : Marker2 {}
                    """
                )

                dart(
                    """
                    abstract class Marker1 {}

                    abstract class Marker2 {}

                    void compare<T>(
                      int a,
                      int b,
                    ) {}
                    void compare${'$'}1bd3a8ce55bd8b17<T extends Object>(
                      T a,
                      T b,
                    ) {}
                    """
                )
            }

        @Test
        fun `function with overload that has the same parameters and both are generic`() = assertCompile {
            kotlin(
                """
                fun <T> compare(a: T, b: Int) {}
                fun <T, A : Int> compare(a: A, b: Int) {}
                """
            )

            dart(
                """
                void compare<T>(
                  T a,
                  int b,
                ) {}
                void compare${'$'}m7891300775455212<T, A extends int>(
                  A a,
                  int b,
                ) {}
                """
            )
        }

        @Test
        fun `function with two overloads that have the same parameters and are generic`() = assertCompile {
            kotlin(
                """
                fun <T, A> compare(a: T, b: Int) {}
                fun <T, A : Set<Int>> compare(a: A, b: Int) {}
                fun <T, A : ImmutableSet<Int>> compare(a: A, b: Int) {}
                """
            )

            dart(
                """
                void compare<T, A>(
                  T a,
                  int b,
                ) {}
                void compare${'$'}53bde3f737855ad9<T, A extends Set<int>>(
                  A a,
                  int b,
                ) {}
                void compare${'$'}330909c5ce1d4043<T, A extends Set<int>>(
                  A a,
                  int b,
                ) {}
                """
            )
        }


        @Test
        fun `function with overload that has the same parameters are generic but has more type parameters`() =
            assertCompile {
                kotlin(
                    """
                    fun <T> compare(a: Int, b: Int) {}
                    fun <T, I, J, K> compare(a: T, b: Int) {}
                    """
                )

                dart(
                    """
                    void compare<T>(
                      int a,
                      int b,
                    ) {}
                    void compare${'$'}m6717f1a76eb95cd5<T, I, J, K>(
                      T a,
                      int b,
                    ) {}
                    """
                )
            }
    }

    @Test
    fun `function with built-in Dart identifier in name`() = assertCompile {
        kotlin("fun covariant() {}")
        dart(
            """
            void covariant() {}
            """
        )
    }

    @Test
    fun `function with reserved Dart word in name`() = assertCompile {
        kotlin("fun with() {}")
        dart(
            """
            void ${'$'}with() {}
            """
        )
    }

    @Test
    fun `function with multiple type parameter bounds that has an extra parameter thats implicitly bounded`() =
        assertCompile {
            kotlin(
                """
                interface ImplicitBound<T>

                interface Marker1<T : ImplicitBound<T>> {
                    fun execute()
                }

                interface Marker2<T>

                fun <T, M> test(obj: T, marked: M) where M : Marker1<T>, M : Marker2<T> {
                    marked.execute()
                }
                """
            )

            dart(
                """
                abstract class ImplicitBound<T> {}

                abstract class Marker1<T extends ImplicitBound<T>> {
                  void execute();
                }

                abstract class Marker2<T> {}

                void test<T extends ImplicitBound<T>, M extends Object>(
                  T obj,
                  M marked,
                ) {
                  (marked as Marker1<T>).execute();
                }
                """
            )
        }

    @Test
    fun `function with multiple type parameter bounds that has an extra parameter thats implicitly bounded by multiple types`() =
        assertCompile {
            kotlin(
                """
                interface ImplicitBound1<T>
                interface ImplicitBound2<T>

                interface Marker1<T> where T : ImplicitBound1<T>, T : ImplicitBound2<T> {
                    fun execute()
                }

                interface Marker2<T>

                fun <T, M> test(obj: T, marked: M) where M : Marker1<T>, M : Marker2<T> {
                    marked.execute()
                }
                """
            )

            dart(
                """
                abstract class ImplicitBound1<T> {}

                abstract class ImplicitBound2<T> {}

                abstract class Marker1<T extends Object> {
                  void execute();
                }

                abstract class Marker2<T> {}

                void test<T extends Object, M extends Object>(
                  T obj,
                  M marked,
                ) {
                  (marked as Marker1<dynamic>).execute();
                }
                """
            )
        }

    @Test
    fun `function with multiple type parameter bounds that is generic and its type argument is a type parameter`() =
        assertCompile {
            kotlin(
                """
                interface Marker0

                interface Marker1<T : Marker0> {
                    fun execute()
                }

                interface Marker2<T>

                fun <T : Marker0, M> test(obj: T, marked: M) where M : Marker1<T>, M : Marker2<T> {
                    marked.execute()
                }
                """
            )

            dart(
                """
                abstract class Marker0 {}

                abstract class Marker1<T extends Marker0> {
                  void execute();
                }

                abstract class Marker2<T> {}

                void test<T extends Marker0, M extends Object>(
                  T obj,
                  M marked,
                ) {
                  (marked as Marker1<T>).execute();
                }
                """
            )
        }

    @Test
    fun `function with multiple type parameter bounds that is generic and has an argument that is a type parameter`() =
        assertCompile {
            kotlin(
                """
                interface Marker0

                interface Marker1

                interface Marked0<Y : Marker0> {
                    fun execute()
                }

                interface Marked1

                fun <T : Marker0, M> test(obj: T, marked: M) where M : Marked0<T>, M : Marked1 {
                    marked.execute()
                }
                """
            )

            dart(
                """
                abstract class Marker0 {}

                abstract class Marker1 {}

                abstract class Marked0<Y extends Marker0> {
                  void execute();
                }

                abstract class Marked1 {}

                void test<T extends Marker0, M extends Object>(
                  T obj,
                  M marked,
                ) {
                  (marked as Marked0<T>).execute();
                }
                """
            )
        }

    @Test
    fun `function with multiple type parameter bounds that is generic and has an argument that is a type parameter with multiple super types`() =
        assertCompile {
            kotlin(
                """
                interface Marker0

                interface Marker1

                interface Marked0<Y : Marker0> {
                    fun execute()
                }

                interface Marked1

                fun <T, M> test(obj: T, marked: M) where T : Marker0, T : Marker1, M : Marked0<T>, M : Marked1 {
                    marked.execute()
                }
                """
            )

            dart(
                """
                abstract class Marker0 {}

                abstract class Marker1 {}

                abstract class Marked0<Y extends Marker0> {
                  void execute();
                }

                abstract class Marked1 {}

                void test<T extends Object, M extends Object>(
                  T obj,
                  M marked,
                ) {
                  (marked as Marked0<dynamic>).execute();
                }
                """
            )
        }

    @Test
    fun `local function`() =
        assertCompile {
            kotlin(
                """
                fun main() {
                    fun localFun() {}
                }
                """
            )

            dart(
                """
                void main() {
                  void localFun() {}
                }
                """
            )
        }

    @Test
    fun `inline function`() = assertCompile {
        kotlin(
            """
            inline fun add(x: Int) = x + 6

            fun main() {
                add(56)
            }
            """
        )

        dart(
            """
            @pragma("vm:always-consider-inlining")
            int add(int x) {
              return x + 6;
            }

            void main() {
              add(56);
            }
            """
        )
    }

    @Test
    fun `inline function with multiple statements`() = assertCompile {
        kotlin(
            """
            inline fun add(x: Int, y: Int = 2, z: Int): Int {
                val intermediate = x + x * x * 34
                z * y * x
                return intermediate + y * y * z
            }

            fun main() {
                val test = add(1, z = 3)
            }
            """
        )

        dart(
            """
            @pragma("vm:always-consider-inlining")
            int add(
              int x,
              int z, {
              int y = 2,
            }) {
              final int intermediate = x + x * x * 34;
              z * y * x;
              return intermediate + y * y * z;
            }

            void main() {
              final int test = add(1, 3);
            }
            """
        )
    }

    @Test
    fun `inline function with lambda parameter`() = assertCompile {
        kotlin(
            """
            inline fun add(compute: (Int) -> Int) = compute(34 * 2)

            fun main() {
                add { it + 123 }
            }
            """
        )

        dart(
            """
            @pragma("vm:always-consider-inlining")
            int add(int Function(int) compute) {
              return compute.call(34 * 2);
            }

            void main() {
              add((int it) {
                return it + 123;
              });
            }
            """
        )
    }

    @Test
    fun `inline function with multiple statements and lambda`() = assertCompile {
        kotlin(
            """
            inline fun process(x: Int, y: Int = 2, z: (Int) -> Int): Int {
                val intermediate = x + z(200) * x * 34
                z(636) * y * x
                return intermediate + y * y * z(274)
            }

            fun main() {
                val test = process(1) {
                    val x = 34
                    it * x
                }
            }
            """
        )

        dart(
            """
            @pragma("vm:always-consider-inlining")
            int process(
              int x,
              int Function(int) z, {
              int y = 2,
            }) {
              final int intermediate = x + z.call(200) * x * 34;
              z.call(636) * y * x;
              return intermediate + y * y * z.call(274);
            }

            void main() {
              final int test = process(1, (int it) {
                final int x = 34;
                return it * x;
              });
            }
            """
        )
    }

    @Test
    fun `function with non-local return`() = assertCompile {
        kotlin(
            """
            inline fun process(x: Int, y: Int = 2, z: (Int) -> Int): Int {
                val intermediate = x + z(200) * x * 34
                z(636) * y * x
                return intermediate + y * y * z(274)
            }

            fun main() {
                val test = process(1) {
                    val x = 34

                    if (x == 100) return

                    it * x
                }
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Return;

            @pragma("vm:always-consider-inlining")
            int process(
              int x,
              int Function(int) z, {
              int y = 2,
            }) {
              final int intermediate = x + z.call(200) * x * 34;
              z.call(636) * y * x;
              return intermediate + y * y * z.call(274);
            }

            void main() {
              try {
                final int test = process(1, (int it) {
                  final int x = 34;
                  if (x == 100) {
                    throw const ${'$'}Return<void>(null, 3514952);
                  }
                  return it * x;
                });
              } on ${'$'}Return<void> catch (tmp0_return) {
                if (tmp0_return.target == 3514952) {
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
    fun `function with non-local return and local return in lambda`() = assertCompile {
        kotlin(
            """
            inline fun process(x: Int, y: Int = 2, z: (Int) -> Int): Int {
                val intermediate = x + z(200) * x * 34
                z(636) * y * x
                return intermediate + y * y * z(274)
            }

            fun main() {
                val test = process(1) {
                    val x = 34

                    when (x) {
                        100 -> return
                        101 -> return@process 0
                    }

                    it * x
                }
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Return;

            @pragma("vm:always-consider-inlining")
            int process(
              int x,
              int Function(int) z, {
              int y = 2,
            }) {
              final int intermediate = x + z.call(200) * x * 34;
              z.call(636) * y * x;
              return intermediate + y * y * z.call(274);
            }

            void main() {
              try {
                final int test = process(1, (int it) {
                  try {
                    final int x = 34;
                    {
                      final int tmp0_subject = x;
                      if (tmp0_subject == 100) {
                        throw const ${'$'}Return<void>(null, 3516874);
                      } else if (tmp0_subject == 101) {
                        throw const ${'$'}Return<int>(0, 1585849394);
                      }
                    }
                    return it * x;
                  } on ${'$'}Return<int> catch (tmp0_return) {
                    if (tmp0_return.target == 1585849394) {
                      return tmp0_return.value;
                    } else {
                      throw tmp0_return;
                    }
                  }
                });
              } on ${'$'}Return<void> catch (tmp0_return) {
                if (tmp0_return.target == 3516874) {
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
    fun `function with non-local return and return expression`() = assertCompile {
        kotlin(
            """
            inline fun process(x: Int, y: Int = 2, z: (Int) -> Int): Int {
                val intermediate = x + z(200) * x * 34
                z(636) * y * x
                return intermediate + y * y * z(274)
            }

            fun main() {
                val test = process(1) {
                    val x = 34

                    val y = when (x) {
                        100 -> return
                        101 -> return@process 40
                        else -> 10
                    }

                    it * x * y
                }
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Return;

            @pragma("vm:always-consider-inlining")
            int process(
              int x,
              int Function(int) z, {
              int y = 2,
            }) {
              final int intermediate = x + z.call(200) * x * 34;
              z.call(636) * y * x;
              return intermediate + y * y * z.call(274);
            }

            void main() {
              try {
                final int test = process(1, (int it) {
                  try {
                    final int x = 34;
                    final int y = () {
                      final int tmp0_subject = x;
                      return tmp0_subject == 100
                          ? throw const ${'$'}Return<void>(null, 3517990)
                          : tmp0_subject == 101
                              ? throw const ${'$'}Return<int>(40, 1585850510)
                              : 10;
                    }.call();
                    return it * x * y;
                  } on ${'$'}Return<int> catch (tmp0_return) {
                    if (tmp0_return.target == 1585850510) {
                      return tmp0_return.value;
                    } else {
                      throw tmp0_return;
                    }
                  }
                });
              } on ${'$'}Return<void> catch (tmp0_return) {
                if (tmp0_return.target == 3517990) {
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
    fun `function with non-local return and return expression of same type`() = assertCompile {
        kotlin(
            """
            inline fun process(z: (Int) -> Unit) {
                z(300)
            }

            fun main() {
                val test = process {
                    val x = 34

                    val y = when (x) {
                        100 -> return
                        101 -> return@process
                        else -> 10
                    }
                }
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/jump.dt.g.dart" show ${'$'}Return;

            @pragma("vm:always-consider-inlining")
            void process(void Function(int) z) {
              z.call(300);
            }

            void main() {
              try {
                final void test = process((int it) {
                  try {
                    final int x = 34;
                    final int y = () {
                      final int tmp0_subject = x;
                      return tmp0_subject == 100
                          ? throw const ${'$'}Return<void>(null, 3402112)
                          : tmp0_subject == 101
                              ? throw const ${'$'}Return<void>(null, 1585731749)
                              : 10;
                    }.call();
                  } on ${'$'}Return<void> catch (tmp0_return) {
                    if (tmp0_return.target == 1585731749) {
                      return;
                    } else {
                      throw tmp0_return;
                    }
                  }
                });
              } on ${'$'}Return<void> catch (tmp0_return) {
                if (tmp0_return.target == 3402112) {
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
    fun `function with always nullable generic return type`() = assertCompile {
        kotlin(
            """
            fun <T> execute(): T? = null
            """
        )

        dart(
            """
            T? execute<T>() {
              return null;
            }
            """
        )
    }
}