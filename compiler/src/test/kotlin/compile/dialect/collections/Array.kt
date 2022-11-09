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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

            void main() {
              final List<int> array = List<int>.empty(growable: false);
            }
            """
        )
    }

    @Test
    fun `use Kotlin Array as Dart List in external function`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                test(arrayOf())
            }

            external fun test(list: Flex<AnyList<Int>, List<Int>>)
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              test(List<int>.empty(growable: false));
            }
            """
        )
    }

    @Test
    fun `use Kotlin String vararg as Dart List in external function`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun process(vararg numbers: String) {
                test(numbers)
            }

            external fun test(list: Flex<AnyList<String>, List<String>>)
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void process(List<String> numbers) {
              test(numbers);
            }
            """
        )
    }

    @Test
    fun `use Kotlin Int vararg as Dart List in external function`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun process(vararg words: String) {
                test(words)
            }

            external fun test(list: Flex<AnyList<String>, List<String>>)
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void process(List<String> words) {
              test(words);
            }
            """
        )
    }

    @Test
    fun `assign Dart List to Kotlin Array`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val myArray: Array<Int> = calculate()
            }

            external fun calculate(): Flex<AnyList<Int>, List<Int>>
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final List<int> myArray = calculate();
            }
            """
        )
    }

    @Test
    fun `assign Dart List subtype to Kotlin Array`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*
            import dart.math.Random

            fun main() {
                val myList: Array<Int> = MyList<Int>()
            }

            external class MyList<E> : Flex<AnyList<E>, List<E>> {
                override var size: Int = 0
                override fun <R> cast() = MyList<R>()
                override operator fun get(index: Int): E = throw UnsupportedError("Empty")
                override fun reversed() = this
                override fun indexOf(element: @UnsafeVariance E, start: Int): Int = -1
                override fun indexOfFirst(start: Int, predicate: (element: E) -> Boolean): Int = -1
                override fun indexOfLast(start: Int?, predicate: (element: E) -> Boolean): Int = -1
                override fun lastIndexOf(element: @UnsafeVariance E, start: Int?): Int = -1
                override operator fun plus(other: List<@UnsafeVariance E>) = other
                override fun subList(start: Int, end: Int?) = MyList<E>()
                override fun slice(start: Int, end: Int) = MyList<E>()
                override fun asMap(): Map<Int, E> = throw UnsupportedError("Empty")
                override fun equals(other: Any?) = other is List<E> && other.isEmpty()
                override fun iterator(): MutableIterator<E> = throw UnsupportedError("Empty")
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = elements
                override fun <T> map(transform: (element: E) -> T) = MyList<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = MyList<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = MyList<T>()
                override fun contains(element: Any?): Boolean = false
                override fun forEach(action: (element: E) -> Unit) {}
                override fun reduce(operator: (acc: E, element: E) -> @UnsafeVariance E): E = throw UnsupportedError("Empty")
                override fun <T> fold(
                    initial: T,
                    operation: (acc: T, element: E) -> T
                ): T = throw UnsupportedError("Empty")
                override fun all(predicate: (element: E) -> Boolean): Boolean = false
                override fun joinToString(separator: String): String = ""
                override fun any(predicate: (element: E) -> Boolean): Boolean = false
                override fun toList(growable: Boolean) = this
                override fun isEmpty(): Boolean = true
                override fun isNotEmpty(): Boolean = false
                override fun take(n: Int) = this
                override fun takeWhile(predicate: (value: E) -> Boolean) = this
                override fun drop(n: Int) = this
                override fun dropWhile(predicate: (value: E) -> Boolean) = this
                override var first: E = throw UnsupportedError("Empty")
                override var last: E = throw UnsupportedError("Empty")
                override fun single(): E = throw UnsupportedError("Empty")
                override fun first(
                    orElse: (() -> @UnsafeVariance E)?,
                    predicate: (element: E) -> Boolean
                ): E = throw UnsupportedError("Empty")
                override fun last(
                    orElse: (() -> @UnsafeVariance E)?,
                    predicate: (element: E) -> Boolean
                ): E = throw UnsupportedError("Empty")
                override fun single(
                    orElse: (() -> @UnsafeVariance E)?,
                    predicate: (element: E) -> Boolean
                ): E = throw UnsupportedError("Empty")
                override fun elementAt(index: Int): E = throw UnsupportedError("Empty")
                override fun toSet(): Set<E> = throw UnsupportedError("Empty")

                override operator fun set(index: Int, value: E): E = throw UnsupportedError("")
                override fun setAll(index: Int, @DartName("iterable") elements: Iterable<E>) {}

                override fun setSlice(
                    start: Int,
                    end: Int,
                    elements: Iterable<E>,
                    dropCount: Int
                ) {}

                override fun fillSlice(start: Int, end: Int, fill: E?) {}
                override fun sort(selector: ((a: E, b: E) -> Int)?) {}
                override fun shuffle(random: Random?) {}
 
                override fun add(value: E): Unit {}
            
                override fun addAll(elements: Iterable<E>): Unit {}
            
                override fun clear(): Unit {}
            
                override fun remove(value: Any?): Boolean = false
                override fun removeIf(predicate: (element: E) -> Boolean): Unit {}
                override fun retainIf(predicate: (element: E) -> Boolean): Unit {}

                override fun add(index: Int, element: E): Unit {}
                
                override fun addAll(index: Int, elements: Iterable<E>): Unit {}
                
                override fun removeAt(index: Int): E = throw "no"
            
                override fun removeLast(): E = throw "no"
                
                override fun removeSlice(start: Int, end: Int): Unit {}
                override fun replaceSlice(start: Int, end: Int, @DartName("replacements") elements: Iterable<E>): Unit {}
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final List<int> myList = MyList<int>();
            }
            """
        )
    }
}