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
import DefaultValue
import assertCompile
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Function")
class Function : BaseTest {
    @Test
    fun function() = assertCompile {
        kotlin("fun test() {}")
        dart("void test() {}")
    }

    @Test
    fun `private function`() = assertCompile {
        kotlin("private fun test() {}")
        dart("void _test() {}")
    }

    @Test
    fun `function with single parameter`() = assertCompile {
        kotlin("fun test(arg: String) {}")

        dart("void test(String arg) {}")
    }

    @Test
    fun `function with single parameter with default value`() = assertCompile {
        kotlin("""fun test(arg: String = "test") {}""")

        dart("void test({String arg = 'test'}) {}")
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

        dart("void test({String? arg = null}) {}")
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
            String returnsString() {
              return 'test';
            }
            
            void test({dynamic arg = const _$DefaultValue()}) {
              arg = arg == const _$DefaultValue() ? returnsString() : arg as String?;
            }
            
            class _$DefaultValue {
              const _$DefaultValue();
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
            
            class _$DefaultValue {
              const _$DefaultValue();
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

    @Disabled
    @Test
    fun `function with vararg parameter`() = assertCompile {
        kotlin("fun test(vararg args: String) {}")

        dart("void test(List<String> args) {}")
    }

    @Disabled
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
              test(<String>['abc', 'def', 'ghi']);
            }
            """
        )
    }

    @Disabled
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

            void test<T>(T? book) {
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

                void test<T extends Book?>(T? book) {
                  (book as Novel?)?.enjoy();
                  (book as Memoir?)?.intrigue();
                  lookUp(book as Memoir?);
                }

                void lookUp(Memoir? memoir) {}
                """
            )
        }
}