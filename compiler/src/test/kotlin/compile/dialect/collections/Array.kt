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

package compile.dialect.collections

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@DisplayName("Compile: Dialect: Collections: Array")
class Array : BaseTest {
    @Test
    fun `arrayOf call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val array = arrayOf(0, 1, 2)
            }
            """
        )

        dart(
            """
            void main() {
              final List<int> array = List<int>.of(<int>[0, 1, 2], growable: false);
            }
            """
        )
    }

    @Test
    fun `arrayOfNulls call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val array = arrayOfNulls<Int>(10)
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/kotlin/collections/array_factories.dt.g.dart"
                show arrayOfNulls;

            void main() {
              final List<int?> array = arrayOfNulls<int>(10);
            }
            """
        )
    }

    @Test
    fun `emptyArray call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val array = emptyArray<String>()
            }
            """
        )

        dart(
            """
            void main() {
              final List<String> array = List<String>.empty(growable: false);
            }
            """
        )
    }

    @Test
    fun `empty arrayOf call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val array = arrayOf<Int>()
            }
            """
        )

        dart(
            """
            void main() {
              final List<int> array = List<int>.empty(growable: false);
            }
            """
        )
    }

    @Test
    fun `use Kotlin Array as Dart List in Dart function`() = assertCompile {
        dart(
            """
            void calc(List<int> list) {}
            """,
            Path("lib/calculate.dart"),
            assert = false
        )

        kotlin(
            """
            import pkg.test.calculate.calc

            fun main() {
                calc(arrayOf())
            }
            """
        )

        dart(
            """
            import "calculate.dart" show calc;

            void main() {
              calc(List<int>.empty(growable: false));
            }
            """
        )
    }

    @Test
    fun `use Kotlin String vararg as Dart List in Dart function`() = assertCompile {
        kotlin(
            """
            import pkg.test.calculate.calc

            fun process(vararg numbers: String) {
                calc(numbers)
            }
            """
        )

        dart(
            """
            void calc(List<String> list) {}
            """,
            Path("lib/calculate.dart"),
            assert = false
        )

        dart(
            """
            import "calculate.dart" show calc;

            void process(List<String> numbers) {
              calc(numbers);
            }
            """
        )
    }

    @Test
    fun `use Kotlin Int vararg as Dart List in Dart function`() = assertCompile {
        dart(
            """
            void calc(List<String> list) {}
            """,
            Path("lib/calculator.dart"),
            assert = false
        )

        kotlin(
            """
            import pkg.test.calculator.calc

            fun process(vararg words: String) {
                calc(words)
            }
            """
        )

        dart(
            """
            import "calculator.dart" show calc;

            void process(List<String> words) {
              calc(words);
            }
            """
        )
    }

    @Test
    fun `assign Dart List to Kotlin Array`() = assertCompile {
        dart(
            """
            List<int> calculate() => [];
            """,
            Path("lib/calculate.dart"),
            assert = false
        )

        kotlin(
            """
            import pkg.test.calculate.calculate

            fun main() {
                val myArray: Array<Int> = calculate()
            }
            """
        )

        dart(
            """
            import "calculate.dart" show calculate;

            void main() {
              final List<int> myArray = calculate();
            }
            """
        )
    }

    @Test
    fun `assign Dart List subtype to Kotlin Array`() = assertCompile {
        dart(
            """
            class MyList<E> implements List<E> {
              dynamic noSuchMethod(Invocation invocation) {}
            }
            """,
            Path("lib/my_list.dart"),
            assert = false
        )

        kotlin(
            """
            import pkg.test.my_list.MyList

            fun main() {
                val myList: Array<Int> = MyList<Int>()
            }
            """
        )

        dart(
            """
            import "my_list.dart" show MyList;

            void main() {
              final List<int> myList = MyList<int>();
            }
            """
        )
    }
}