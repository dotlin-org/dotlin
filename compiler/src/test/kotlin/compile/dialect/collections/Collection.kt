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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@DisplayName("Compile: Dialect: Collections: Collection")
class Collection : BaseTest {
    @Test
    fun `assign Dart List to Kotlin Collection variable`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val myCollection: Collection<Int> = calculate()
            }

            @DartLibrary("calc.dart")
            external fun calculate(): Flex<AnyList<Int>, List<Int>>
            """
        )

        dart(
            """
            List<int> calculate() => [];
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/flex.dt.g.dart" show AnyCollection;
            import "package:meta/meta.dart";

            void main() {
              final AnyCollection<int> myCollection = calculate();
            }
            """
        )
    }

    @Test
    fun `assign Dart List to Kotlin Collection property`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            val myCollection: Collection<Int> = calculate()

            @DartLibrary("calc.dart")
            external fun calculate(): Flex<AnyList<Int>, List<Int>>
            """
        )

        dart(
            """
            List<int> calculate() => [];
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/flex.dt.g.dart" show AnyCollection;
            import "package:meta/meta.dart";

            final AnyCollection<int> myCollection = calculate();
            """
        )
    }

    @Test
    fun `pass Dart List to Kotlin Collection parameter`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val myList = calculate()
                process(myList)
            }

            fun process(collection: Collection<Int>) {}

            @DartLibrary("calc.dart")
            external fun calculate(): Flex<AnyList<Int>, List<Int>>
            """
        )

        dart(
            """
            List<int> calculate() => [];
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/flex.dt.g.dart" show AnyCollection;
            import "package:meta/meta.dart";

            void main() {
              final List<int> myList = calculate();
              process(myList);
            }
            
            void process(AnyCollection<int> collection) {}
            """
        )
    }

    // TODO: Fix in #53.
    @Disabled
    @Test
    fun `(dynamic) is Collection`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj is Collection<Int>) {
                    obj.cast<Number>()
                }
            }

            @DartLibrary("calc.dart")
            external fun calculate(): dynamic
            """
        )

        dart(
            """
            dynamic calculate() => null;
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show DotlinTypeIntrinsics;
            import "package:dotlin/src/kotlin/collections/collection.dt.g.dart"
                show Collection;
            import "package:dotlin/src/dotlin/intrinsics/flex.dt.g.dart" show AnyCollection;
            import "package:meta/meta.dart";

            void main() {
              final dynamic obj = calculate();
              if (obj.isCollection<int>()) {
                (obj as AnyCollection<int>).cast<num>();
              }
            }
            """
        )
    }

    @Test
    fun `(Any) is Collection`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj is Collection<Int>) {
                    obj.cast<Number>()
                }
            }

            @DartLibrary("calc.dart")
            external fun calculate(): Any
            """
        )

        dart(
            """
            Object calculate() => 0;
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show DotlinTypeIntrinsics;
            import "package:dotlin/src/kotlin/collections/collection.dt.g.dart"
                show Collection;
            import "package:dotlin/src/dotlin/intrinsics/flex.dt.g.dart" show AnyCollection;
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (obj.isCollection<int>()) {
                (obj as AnyCollection<int>).cast<num>();
              }
            }
            """
        )
    }

    // TODO: Fix in #53.
    @Disabled
    @Test
    fun `(dynamic) !is Collection`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj !is Collection<Int>) {
                    
                }
            }

            @DartLibrary("calc.dart")
            external fun calculate(): dynamic
            """
        )

        dart(
            """
            dynamic calculate() => null;
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show DotlinTypeIntrinsics;
            import "package:meta/meta.dart";

            void main() {
              final dynamic obj = calculate();
              if (!obj.isCollection<int>()) {}
            }
            """
        )
    }

    // TODO: Fix in #53.
    @Disabled
    @Test
    fun `(Any) !is Collection`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj !is Collection<Int>) {

                }
            }

            @DartLibrary("calc.dart")
            external fun calculate(): Any
            """
        )

        dart(
            """
            Object calculate() => 3;
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show DotlinTypeIntrinsics;
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (!obj.isCollection<int>()) {}
            }
            """
        )
    }

    @Test
    fun `assign Dart List subtype to Kotlin Collection`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*
            import dart.math.*

            fun main() {
                val myList: Collection<Int> = MyList<Int>()
            }

            @DartLibrary("my_list.dart")
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
            class MyList<E> implements List<E> {
              dynamic noSuchMethod(Invocation invocation) {}
            }
            """,
            Path("lib/my_list.dart"),
            assert = false
        )

        dart(
            """
            import "my_list.dart" show MyList;
            import "package:dotlin/src/dotlin/intrinsics/flex.dt.g.dart" show AnyCollection;
            import "package:meta/meta.dart";

            void main() {
              final AnyCollection<int> myList = MyList<int>();
            }
            """
        )
    }

    // TODO: Extension on Collection
    @Disabled
    @Test
    fun `extension on MutableCollection`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                lateinit var bag: MutableCollection<Int>
                bag.calc()

                val list = mutableListOf(1, 2, 3)
                list.calc()
            }

            fun <E> MutableCollection<E>.calc() {}
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final dynamic obj = calculate();
              if (!obj.isCollection<int>()) {}
            }
            """
        )
    }
}