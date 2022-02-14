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
import org.junit.jupiter.api.Disabled
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
            import 'package:meta/meta.dart';

            void test() {}
            """
        )
    }

    @Test
    fun `private function`() = assertCompile {
        kotlin("private fun test() {}")
        dart(
            """
            import 'package:meta/meta.dart';

            void _test() {}
            """
        )
    }

    @Test
    fun `function with single parameter`() = assertCompile {
        kotlin("fun test(arg: String) {}")

        dart(
            """
            import 'package:meta/meta.dart';

            void test(String arg) {}
            """
        )
    }

    @Test
    fun `function with single parameter with default value`() = assertCompile {
        kotlin("""fun test(arg: String = "test") {}""")

        dart(
            """
            import 'package:meta/meta.dart';

            void test({String arg = 'test'}) {}
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
            import 'package:meta/meta.dart';

            String returnsString() {
              return 'test';
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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

            String returnsString() {
              return 'test';
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
    fun `function with two parameters`() = assertCompile {
        kotlin("fun test(arg1: String, arg2: Int) {}")

        dart(
            """
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

            void test({
              String arg1 = 'test',
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
            import 'package:meta/meta.dart';

            String returnsString() {
              return 'test';
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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

            String returnsString() {
              return 'test';
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
            import 'package:meta/meta.dart';

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
            import 'dart:core' as core;
            import 'dart:core' hide List;
            import 'package:meta/meta.dart';

            void test(core.List<String> args) {}
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
            import 'dart:core' as core;
            import 'dart:core' hide List;
            import 'package:meta/meta.dart';

            void test(core.List<String> args) {}
            void main() {
              test(<String>['abc', 'def', 'ghi']);
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
            import 'dart:core' as core;
            import 'dart:core' hide List;
            import 'package:meta/meta.dart';

            void test(
              core.List<String> args,
              int x,
            ) {}
            void main() {
              test(<String>['abc', 'def', 'ghi'], 0);
            }
            """
        )
    }

    @Test
    fun `function with type parameter`() = assertCompile {
        kotlin("fun <T> test() {}")

        dart(
            """
            import 'package:meta/meta.dart';

            void test<T>() {}
            """
        )
    }

    @Test
    fun `function with two type parameters`() = assertCompile {
        kotlin("fun <T0, T1> test() {}")

        dart(
            """
            import 'package:meta/meta.dart';

            void test<T0, T1>() {}
            """
        )
    }

    @Test
    fun `function with type parameter bound`() = assertCompile {
        kotlin("fun <T : Int> test() {}")

        dart(
            """
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
                import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
                import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
                import 'package:meta/meta.dart';

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
                import 'package:meta/meta.dart';

                void giveCookie() {}
                void giveCookieWithMessage(String message) {}
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
                import 'package:meta/meta.dart';

                void giveCookie() {}
                void giveCookieWhileSaying(String message) {}
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
                import 'package:meta/meta.dart';

                void giveCookie() {}
                void giveCookieWithMessage(String message) {}
                void giveCookieWithMessageAndWrapping(
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
                import 'package:meta/meta.dart';

                void giveCookie() {}
                void giveCookieWithMessage(String message) {}
                void giveCookieWithMessageAndWrapping(
                  String message,
                  String wrapping,
                ) {}
                void giveCookieWithMessageWrappingAndExtra(
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
                import 'package:meta/meta.dart';

                void sayHello(String greeting) {}
                void sayHelloWithFriendly(
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
                import 'package:meta/meta.dart';

                void compare(
                  int a,
                  int b,
                ) {}
                void compareWithThatAndOther(
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
                import 'package:meta/meta.dart';

                void compare(
                  int a,
                  int b,
                ) {}
                void compareString(
                  String a,
                  String b,
                ) {}
                void compareDouble(
                  double a,
                  double b,
                ) {}
                """
            )
        }

        @Disabled // TODO: Do this test for Dart interface and its Impl
        @Test
        fun `function with overload that have same amount of parameters, same names and same Dart types`() =
            assertCompile {
                kotlin(
                    """
                    fun compare(that: Int, other: Int) {}
                    fun compare(that: Float, other: Float) {}
                    fun compare(that: Double, other: Double) {}
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    void compare(
                      int that,
                      int other,
                    ) {}
                    void compareFloat(
                      double that,
                      double other,
                    ) {}
                    void compareDouble(
                      double that,
                      double other,
                    ) {}
                    """
                )
        }

        @Disabled // TODO: Do this test for Dart interface and its Impl
        @Test
        fun `function with overload that have same amount of parameters, but not the same names but same Dart types`() =
            assertCompile {
                kotlin(
                    """
                    fun compare(a: Int, b: Int) {}
                    fun compare(that: Float, other: Float) {}
                    fun compare(that: Double, other: Double) {}
                    """
                )

                dart(
                    """
                    import 'package:meta/meta.dart';

                    void compare(
                      int a,
                      int b,
                    ) {}
                    void compareWithThatAndOther(
                      double that,
                      double other,
                    ) {}
                    void compareWithThatAndOtherDouble(
                      double that,
                      double other,
                    ) {}
                    """
                )
            }

        @Disabled // TODO: Do this test for Dart interface and its Impl
        @Test
        fun `function with overload that are equivalent in Dart but not in Kotlin`() = assertCompile {
            kotlin(
                """
                fun compare(a: Int, b: Int) {}

                fun compare(a: Long, b: Long) {}
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                void compare(
                  int a,
                  int b,
                ) {}
                void compareLong(
                  int a,
                  int b,
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
                import 'package:meta/meta.dart';

                void giveCookie() {}
                void giveCookieWithMessage(String message) {}
                void giveCookieWithMessageAndWrapping(
                  String message,
                  String wrapping,
                ) {}
                void giveCookieWithMessageAndWrappingInt(
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
                import 'package:meta/meta.dart';

                void compareWithGenericT<T>(
                  int a,
                  int b,
                ) {}
                void compare(
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
                    import 'package:meta/meta.dart';

                    void compare<T>(
                      int a,
                      int b,
                    ) {}
                    void compareWithGenericTMustBeString<T extends String>(
                      T a,
                      T b,
                    ) {}
                    """
                )
            }

        @Disabled // TODO: Analysis error
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
                import 'package:meta/meta.dart';

                void compare<T>(
                  T a,
                  int b,
                ) {}
                void compareWithGenericA<T, A extends int>(
                  A a,
                  int b,
                ) {}
                """
            )
        }

        @Disabled // TODO: Do this test but with Dart interface and its Impl
        @Test
        fun `function with two overloads that have the same parameters and are generic`() = assertCompile {
            kotlin(
                """
                fun <T, A> compare(a: T, b: Int) {}
                fun <T, A : Int> compare(a: A, b: Int) {}
                fun <T, A : Long> compare(a: A, b: Int) {}
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                void compare<T, A>(
                  T a,
                  int b,
                ) {}
                void compareWithGenericAMustBeInt<T, A extends int>(
                  A a,
                  int b,
                ) {}
                void compareWithGenericAMustBeLong<T, A extends int>(
                  A a,
                  int b,
                ) {}
                """
            )
        }

        // TODO (possibly): Not ideal, but correct with the current logic.
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
                    import 'package:meta/meta.dart';

                    void compare<T>(
                      int a,
                      int b,
                    ) {}
                    void compareWithA<T, I, J, K>(
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
            import 'package:meta/meta.dart';

            void covariant() {}
            """
        )
    }

    @Test
    fun `function with reserved Dart word in name`() = assertCompile {
        kotlin("fun with() {}")
        dart(
            """
            import 'package:meta/meta.dart';

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
                import 'package:meta/meta.dart';

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
                import 'package:meta/meta.dart';

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
                import 'package:meta/meta.dart';

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
                import 'package:meta/meta.dart';

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
                import 'package:meta/meta.dart';

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
}