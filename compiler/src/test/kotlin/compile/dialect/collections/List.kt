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

@DisplayName("Compile: Dialect: Collections: List")
class List : BaseTest {
    @Test
    fun `listOf call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val list = listOf(0, 1, 2)
            }
            """
        )

        dart(
            """
            import "dart:collection";
            import "package:meta/meta.dart";

            void main() {
              final List<int> list = UnmodifiableListView<int>(<int>[0, 1, 2]);
            }
            """
        )
    }

    @Test
    fun `listOf const call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val list = @const listOf(0, 1, 2)
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final List<int> list = const <int>[0, 1, 2];
            }
            """
        )
    }

    @Test
    fun `listOf const call in const val`() = assertCompile {
        kotlin(
            """
            fun main() {
                const val list = listOf(0, 1, 2)
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              const List<int> list = <int>[0, 1, 2];
            }
            """
        )
    }

    @Test
    fun `emptyList call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val list = emptyList<String>()
            }
            """
        )

        dart(
            """
            import "dart:collection";
            import "package:meta/meta.dart";

            void main() {
              final List<String> list = UnmodifiableListView<String>(<String>[]);
            }
            """
        )
    }

    @Test
    fun `emptyList const call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val list = @const emptyList<String>()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final List<String> list = const <String>[];
            }
            """
        )
    }

    @Test
    fun `emptyList const call in const val`() = assertCompile {
        kotlin(
            """
            fun main() {
                const val list = emptyList<String>()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              const List<String> list = <String>[];
            }
            """
        )
    }

    @Test
    fun `mutableListOf call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val list = mutableListOf(0, 1, 2)
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final List<int> list = <int>[0, 1, 2];
            }
            """
        )
    }

    @Test
    fun `mutableListOfNulls call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val list = mutableListOfNulls<Int>(40)
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final List<int?> list = mutableListOfNulls<int>(40);
            }
            """
        )
    }

    @Disabled
    @Test
    fun `List call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val list = List(30) { it * 2 }
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final List<int> list = kotlin.List<int>(30, (int it) {
                return it * 2;
              });
            }
            """
        )
    }

    @Disabled
    @Test
    fun `MutableList call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val list = MutableList(30) { it * 2 }
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final List<int> list = MutableList<int>(30, (int it) {
                return it * 2;
              });
            }
            """
        )
    }

    @Test
    fun `use Kotlin ImmutableList as Dart List in external function`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                test(listOf())
            }

            external fun test(list: Flex<AnyList<Int>, List<Int>>)
            """
        )

        dart(
            """
            import "dart:collection";
            import "package:meta/meta.dart";

            void main() {
              test(UnmodifiableListView<int>(<int>[]));
            }
            """
        )
    }

    @Test
    fun `use Kotlin MutableList as Dart List in external function`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                test(mutableListOf())
            }

            external fun test(list: Flex<AnyList<Int>, List<Int>>)
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              test(<int>[]);
            }
            """
        )
    }

    @Test
    fun `assign Dart List subtype to Kotlin MutableList`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*
            import dart.math.Random

            fun main() {
                val myList: MutableList<Int> = MaybeMutableList<Int>()
            }

            external class MaybeMutableList<E> : Flex<AnyList<E>, List<E>> {
                override var size: Int = 0
                override fun <R> cast() = MaybeMutableList<R>()
                override operator fun get(index: Int): E = throw UnsupportedError("Empty")
                override fun reversed() = this
                override fun indexOf(element: @UnsafeVariance E, start: Int): Int = -1
                override fun indexOfFirst(start: Int, predicate: (element: E) -> Boolean): Int = -1
                override fun indexOfLast(start: Int?, predicate: (element: E) -> Boolean): Int = -1
                override fun lastIndexOf(element: @UnsafeVariance E, start: Int?): Int = -1
                override operator fun plus(other: List<@UnsafeVariance E>) = other
                override fun subList(start: Int, end: Int?) = MaybeMutableList<E>()
                override fun slice(start: Int, end: Int) = MaybeMutableList<E>()
                override fun asMap(): Map<Int, E> = throw UnsupportedError("Empty")
                override fun equals(other: Any?) = other is List<E> && other.isEmpty()
                override fun iterator(): MutableIterator<E> = throw UnsupportedError("Empty")
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = elements
                override fun <T> map(transform: (element: E) -> T) = MaybeMutableList<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = MaybeMutableList<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = MaybeMutableList<T>()
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
            import "package:meta/meta.dart";

            void main() {
              final List<int> myList = MaybeMutableList<int>();
            }
            """
        )
    }

    @Test
    fun `assign Dart List to Kotlin ImmutableList`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val myList: ImmutableList<Int> = calculate()
            }

            external fun calculate(): Flex<AnyList<Int>, List<Int>>
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final List<int> myList = calculate();
            }
            """
        )
    }

    @Test
    fun `assign Dart List subtype to Kotlin ImmutableList`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*
            import dart.math.*

            fun main() {
                val myList: ImmutableList<Int> = MaybeMutableList<Int>()
            }

            external class MaybeMutableList<E> : Flex<AnyList<E>, List<E>> {
                override var size: Int = 0
                override fun <R> cast() = MaybeMutableList<R>()
                override operator fun get(index: Int): E = throw UnsupportedError("Empty")
                override fun reversed() = this
                override fun indexOf(element: @UnsafeVariance E, start: Int): Int = -1
                override fun indexOfFirst(start: Int, predicate: (element: E) -> Boolean): Int = -1
                override fun indexOfLast(start: Int?, predicate: (element: E) -> Boolean): Int = -1
                override fun lastIndexOf(element: @UnsafeVariance E, start: Int?): Int = -1
                override operator fun plus(other: List<@UnsafeVariance E>) = other
                override fun subList(start: Int, end: Int?) = MaybeMutableList<E>()
                override fun slice(start: Int, end: Int) = MaybeMutableList<E>()
                override fun asMap(): Map<Int, E> = throw UnsupportedError("Empty")
                override fun equals(other: Any?) = other is List<E> && other.isEmpty()
                override fun iterator(): MutableIterator<E> = throw UnsupportedError("Empty")
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = elements
                override fun <T> map(transform: (element: E) -> T) = MaybeMutableList<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = MaybeMutableList<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = MaybeMutableList<T>()
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
            import "package:meta/meta.dart";

            void main() {
              final List<int> myList = MaybeMutableList<int>();
            }
            """
        )
    }

    @Test
    fun `is List`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj is List<Int>) {
                    obj[0]
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (obj is List<int>) {
                (obj as List<int>)[0];
              }
            }
            """
        )
    }

    @Test
    fun `!is List`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj !is List<Int>) {

                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (obj is! List<int>) {}
            }
            """
        )
    }

    @Test
    fun `is ImmutableList`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj is ImmutableList<Int>) {
                    obj[0]
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (obj.isImmutableList<int>()) {
                (obj as List<int>)[0];
              }
            }
            """
        )
    }

    @Test
    fun `!is ImmutableList`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj !is ImmutableList<Int>) {
                    
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (!obj.isImmutableList<int>()) {}
            }
            """
        )
    }

    @Test
    fun `is WriteableList`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj is WriteableList<Int>) {
                    obj[0]
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (obj.isWriteableList<int>()) {
                (obj as List<int>)[0];
              }
            }
            """
        )
    }

    @Test
    fun `!is WriteableList`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj !is WriteableList<Int>) {
                    
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (!obj.isWriteableList<int>()) {}
            }
            """
        )
    }

    @Test
    fun `is Array`() = assertCompile {
            kotlin(
                """
            fun main() {
                val obj = calculate()
                if (obj is Array<Int>) {
                    obj[0]
                }
            }

            external fun calculate(): Any
            """
            )

            dart(
                """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (obj.isFixedSizeList<int>()) {
                (obj as List<int>)[0];
              }
            }
            """
            )
        }

    @Test
    fun `!is Array`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj !is Array<Int>) {
                    
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (!obj.isFixedSizeList<int>()) {}
            }
            """
        )
    }

    // TODO: The typealias should compile directly to "List".
    // @DartTransparent?
    @Disabled
    @Test
    fun `is FixedSizeList`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj is FixedSizeList<Int>) {
                    obj[0]
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (obj.isFixedSizeList<int>()) {
                (obj as List<int>)[0];
              }
            }
            """
        )
    }

    // TODO: The typealias should compile directly to "List".
    // @DartTransparent?
    @Disabled
    @Test
    fun `!is FixedSizeList`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj !is FixedSizeList<Int>) {
                    
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (!obj.isFixedSizeList<int>()) {}
            }
            """
        )
    }

    @Test
    fun `is MutableList`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj is MutableList<Int>) {
                    obj[0]
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (obj.isMutableList<int>()) {
                (obj as List<int>)[0];
              }
            }
            """
        )
    }

    @Test
    fun `!is MutableList`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj !is MutableList<Int>) {
                    
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object obj = calculate();
              if (!obj.isMutableList<int>()) {}
            }
            """
        )
    }

    @Test
    fun `class implementing ImmutableList`() = assertCompile {
        kotlin(
            """
            class EmptyImmutableList<out E> : ImmutableList<E> {
                override val size: Int = 0
                override fun <R> cast() = EmptyImmutableList<R>()
                override operator fun get(index: Int): E = throw UnsupportedError("Empty")
                override fun reversed() = this
                override fun indexOf(element: @UnsafeVariance E, start: Int): Int = -1
                override fun indexOfFirst(start: Int, predicate: (element: E) -> Boolean): Int = -1
                override fun indexOfLast(start: Int?, predicate: (element: E) -> Boolean): Int = -1
                override fun lastIndexOf(element: @UnsafeVariance E, start: Int?): Int = -1
                override operator fun plus(other: List<@UnsafeVariance E>) = other
                override fun subList(start: Int, end: Int?) = EmptyImmutableList<E>()
                override fun slice(start: Int, end: Int) = EmptyImmutableList<E>()
                override fun asMap(): Map<Int, E> = throw UnsupportedError("Empty")
                override fun equals(other: Any?) = other is List<E> && other.isEmpty()
                override fun iterator(): Iterator<E> = throw UnsupportedError("Empty")
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = elements
                override fun <T> map(transform: (element: E) -> T) = EmptyImmutableList<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = EmptyImmutableList<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = EmptyImmutableList<T>()
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
                override val first: E = throw UnsupportedError("Empty")
                override val last: E = throw UnsupportedError("Empty")
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
            }
            """
        )

        dart(
            """
            import "dart:math";
            import "package:meta/meta.dart";
            
            @sealed
            class EmptyImmutableList<E> implements List<E>, ImmutableListMarker {
              @nonVirtual
              int _${"$"}lengthBackingField = 0;
              @override
              int get length {
                return this._${"$"}lengthBackingField;
              }
            
              @override
              void set length(int ${"$"}value) {
                throw UnsupportedError("Cannot change the size of an immutable list");
              }
            
              @override
              EmptyImmutableList<R> cast<R>() {
                return EmptyImmutableList<R>();
              }
            
              @override
              E get(int index) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E operator [](int index) => this.get(index);
              @override
              EmptyImmutableList<E> get reversed {
                return this;
              }
            
              @override
              int indexOf(
                E element, [
                int start = 0,
              ]) {
                return -1;
              }
            
              @override
              int indexWhere(
                bool Function(E) test, [
                int start = 0,
              ]) {
                return -1;
              }
            
              @override
              int lastIndexWhere(
                bool Function(E) test, [
                int? start = 0,
              ]) {
                return -1;
              }
            
              @override
              int lastIndexOf(
                E element, [
                int? start = null,
              ]) {
                return -1;
              }
            
              @override
              List<E> plus(List<E> other) {
                return other;
              }
            
              @override
              List<E> operator +(List<E> other) => this.plus(other);
              @override
              EmptyImmutableList<E> sublist(
                int start, [
                int? end = null,
              ]) {
                return EmptyImmutableList<E>();
              }
            
              @override
              EmptyImmutableList<E> getRange(
                int start,
                int end,
              ) {
                return EmptyImmutableList<E>();
              }
            
              @override
              Map<int, E> asMap() {
                throw UnsupportedError("Empty");
              }
            
              bool equals(Object? other) {
                return other is List<E> && (other as List<E>).isEmpty;
              }
            
              @override
              bool operator ==(Object? other) => this.equals(other);
              @override
              Iterator<E> get iterator {
                throw UnsupportedError("Empty");
              }
            
              @override
              Iterable<E> followedBy(Iterable<E> other) {
                return other;
              }
            
              @override
              EmptyImmutableList<T> map<T>(T Function(E) toElement) {
                return EmptyImmutableList<T>();
              }
            
              @override
              EmptyImmutableList<E> where(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyImmutableList<T> whereType<T>() {
                return EmptyImmutableList<T>();
              }
            
              @override
              EmptyImmutableList<T> expand<T>(Iterable<T> Function(E) toElements) {
                return EmptyImmutableList<T>();
              }
            
              @override
              bool contains(Object? element) {
                return false;
              }
            
              @override
              void forEach(void Function(E) action) {}
              @override
              E reduce(
                  E Function(
                E,
                E,
              )
                      combine) {
                throw UnsupportedError("Empty");
              }
            
              @override
              T fold<T>(
                T initialValue,
                T Function(
                  T,
                  E,
                )
                    combine,
              ) {
                throw UnsupportedError("Empty");
              }
            
              @override
              bool every(bool Function(E) test) {
                return false;
              }
            
              @override
              String join([String separator = ", "]) {
                return "";
              }
            
              @override
              bool any(bool Function(E) test) {
                return false;
              }
            
              @override
              EmptyImmutableList<E> toList({bool growable = true}) {
                return this;
              }
            
              @override
              bool get isEmpty {
                return true;
              }
            
              @override
              bool get isNotEmpty {
                return false;
              }
            
              @override
              EmptyImmutableList<E> take(int count) {
                return this;
              }
            
              @override
              EmptyImmutableList<E> takeWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyImmutableList<E> skip(int count) {
                return this;
              }
            
              @override
              EmptyImmutableList<E> skipWhile(bool Function(E) test) {
                return this;
              }
            
              @nonVirtual
              E _${"$"}firstBackingField = throw UnsupportedError("Empty");
              @override
              E get first {
                return this._${"$"}firstBackingField;
              }
            
              @override
              void set first(E ${"$"}value) {
                throw UnsupportedError("Cannot modify an immutable list");
              }
            
              @nonVirtual
              E _${"$"}lastBackingField = throw UnsupportedError("Empty");
              @override
              E get last {
                return this._${"$"}lastBackingField;
              }
            
              @override
              void set last(E ${"$"}value) {
                throw UnsupportedError("Cannot modify an immutable list");
              }
            
              @override
              E get single {
                throw UnsupportedError("Empty");
              }
            
              @override
              E firstWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E lastWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E singleWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E elementAt(int index) {
                throw UnsupportedError("Empty");
              }
            
              @override
              Set<E> toSet() {
                throw UnsupportedError("Empty");
              }
            
              @override
              void insert(
                int index,
                E element,
              ) {
                throw UnsupportedError("Cannot add to an immutable list");
              }
            
              @override
              void insertAll(
                int index,
                Iterable<E> iterable,
              ) {
                throw UnsupportedError("Cannot add to an immutable list");
              }
            
              @override
              E removeAt(int index) {
                throw UnsupportedError("Cannot remove from an immutable list");
              }
            
              @override
              E removeLast() {
                throw UnsupportedError("Cannot remove from an immutable list");
              }
            
              @override
              void removeRange(
                int start,
                int end,
              ) {
                throw UnsupportedError("Cannot remove from an immutable list");
              }
            
              @override
              void replaceRange(
                int start,
                int end,
                Iterable<E> replacements,
              ) {
                throw UnsupportedError("Cannot remove from an immutable list");
              }
            
              @override
              E set(
                int index,
                E value,
              ) {
                throw UnsupportedError("Cannot modify an immutable list");
              }
            
              @override
              void operator []=(
                int index,
                E value,
              ) =>
                  this.set(index, value);
              @override
              void setAll(
                int index,
                Iterable<E> iterable,
              ) {
                throw UnsupportedError("Cannot modify an immutable list");
              }
            
              @override
              void setRange(
                int start,
                int end,
                Iterable<E> iterable, [
                int skipCount = 0,
              ]) {
                throw UnsupportedError("Cannot modify an immutable list");
              }
            
              @override
              void fillRange(
                int start,
                int end, [
                E? fillValue = null,
              ]) {
                throw UnsupportedError("Cannot modify an immutable list");
              }
            
              @override
              void sort(
                  [int Function(
                    E,
                    E,
                  )?
                      compare = null]) {
                throw UnsupportedError("Cannot modify an immutable list");
              }
            
              @override
              void shuffle([Random? random = null]) {
                throw UnsupportedError("Cannot modify an immutable list");
              }
            
              @override
              void add(E value) {
                throw UnsupportedError("Cannot add to an immutable list");
              }
            
              @override
              void addAll(Iterable<E> iterable) {
                throw UnsupportedError("Cannot add to an immutable list");
              }
            
              @override
              void clear() {
                throw UnsupportedError("Cannot clear an immutable list");
              }
            
              @override
              bool remove(Object? value) {
                throw UnsupportedError("Cannot remove from an immutable list");
              }
            
              @override
              void removeWhere(bool Function(E) test) {
                throw UnsupportedError("Cannot remove from an immutable list");
              }
            
              @override
              void retainWhere(bool Function(E) test) {
                throw UnsupportedError("Cannot remove from an immutable list");
              }
            }
            """
        )
    }

    @Test
    fun `class implementing WriteableList`() = assertCompile {
        kotlin(
            """
            import dart.math.Random

            class EmptyWriteableList<E> : WriteableList<E> {
                override val size: Int = 0
                override fun <R> cast() = EmptyWriteableList<R>()
                override operator fun get(index: Int): E = throw UnsupportedError("Empty")
                override fun reversed() = this
                override fun indexOf(element: @UnsafeVariance E, start: Int): Int = -1
                override fun indexOfFirst(start: Int, predicate: (element: E) -> Boolean): Int = -1
                override fun indexOfLast(start: Int?, predicate: (element: E) -> Boolean): Int = -1
                override fun lastIndexOf(element: @UnsafeVariance E, start: Int?): Int = -1
                override operator fun plus(other: List<@UnsafeVariance E>) = other
                override fun subList(start: Int, end: Int?) = EmptyWriteableList<E>()
                override fun slice(start: Int, end: Int) = EmptyWriteableList<E>()
                override fun asMap(): Map<Int, E> = throw UnsupportedError("Empty")
                override fun equals(other: Any?) = other is List<E> && other.isEmpty()
                override fun iterator(): Iterator<E> = throw UnsupportedError("Empty")
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = elements
                override fun <T> map(transform: (element: E) -> T) = EmptyWriteableList<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = EmptyWriteableList<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = EmptyWriteableList<T>()
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
            }
            """
        )

        dart(
            """
            import "dart:math";
            import "package:meta/meta.dart";
            
            @sealed
            class EmptyWriteableList<E> implements List<E>, WriteableListMarker {
              @nonVirtual
              int _${"$"}lengthBackingField = 0;
              @override
              int get length {
                return this._${"$"}lengthBackingField;
              }
            
              @override
              void set length(int ${"$"}value) {
                throw UnsupportedError("Cannot change the size of a writeable list");
              }
            
              @override
              EmptyWriteableList<R> cast<R>() {
                return EmptyWriteableList<R>();
              }
            
              @override
              E get(int index) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E operator [](int index) => this.get(index);
              @override
              EmptyWriteableList<E> get reversed {
                return this;
              }
            
              @override
              int indexOf(
                E element, [
                int start = 0,
              ]) {
                return -1;
              }
            
              @override
              int indexWhere(
                bool Function(E) test, [
                int start = 0,
              ]) {
                return -1;
              }
            
              @override
              int lastIndexWhere(
                bool Function(E) test, [
                int? start = 0,
              ]) {
                return -1;
              }
            
              @override
              int lastIndexOf(
                E element, [
                int? start = null,
              ]) {
                return -1;
              }
            
              @override
              List<E> plus(List<E> other) {
                return other;
              }
            
              @override
              List<E> operator +(List<E> other) => this.plus(other);
              @override
              EmptyWriteableList<E> sublist(
                int start, [
                int? end = null,
              ]) {
                return EmptyWriteableList<E>();
              }
            
              @override
              EmptyWriteableList<E> getRange(
                int start,
                int end,
              ) {
                return EmptyWriteableList<E>();
              }
            
              @override
              Map<int, E> asMap() {
                throw UnsupportedError("Empty");
              }
            
              bool equals(Object? other) {
                return other is List<E> && (other as List<E>).isEmpty;
              }
            
              @override
              bool operator ==(Object? other) => this.equals(other);
              @override
              Iterator<E> get iterator {
                throw UnsupportedError("Empty");
              }
            
              @override
              Iterable<E> followedBy(Iterable<E> other) {
                return other;
              }
            
              @override
              EmptyWriteableList<T> map<T>(T Function(E) toElement) {
                return EmptyWriteableList<T>();
              }
            
              @override
              EmptyWriteableList<E> where(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyWriteableList<T> whereType<T>() {
                return EmptyWriteableList<T>();
              }
            
              @override
              EmptyWriteableList<T> expand<T>(Iterable<T> Function(E) toElements) {
                return EmptyWriteableList<T>();
              }
            
              @override
              bool contains(Object? element) {
                return false;
              }
            
              @override
              void forEach(void Function(E) action) {}
              @override
              E reduce(
                  E Function(
                E,
                E,
              )
                      combine) {
                throw UnsupportedError("Empty");
              }
            
              @override
              T fold<T>(
                T initialValue,
                T Function(
                  T,
                  E,
                )
                    combine,
              ) {
                throw UnsupportedError("Empty");
              }
            
              @override
              bool every(bool Function(E) test) {
                return false;
              }
            
              @override
              String join([String separator = ", "]) {
                return "";
              }
            
              @override
              bool any(bool Function(E) test) {
                return false;
              }
            
              @override
              EmptyWriteableList<E> toList({bool growable = true}) {
                return this;
              }
            
              @override
              bool get isEmpty {
                return true;
              }
            
              @override
              bool get isNotEmpty {
                return false;
              }
            
              @override
              EmptyWriteableList<E> take(int count) {
                return this;
              }
            
              @override
              EmptyWriteableList<E> takeWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyWriteableList<E> skip(int count) {
                return this;
              }
            
              @override
              EmptyWriteableList<E> skipWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              E first = throw UnsupportedError("Empty");
              @override
              E last = throw UnsupportedError("Empty");
              @override
              E get single {
                throw UnsupportedError("Empty");
              }
            
              @override
              E firstWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E lastWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E singleWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E elementAt(int index) {
                throw UnsupportedError("Empty");
              }
            
              @override
              Set<E> toSet() {
                throw UnsupportedError("Empty");
              }
            
              @override
              E set(
                int index,
                E value,
              ) {
                throw UnsupportedError("");
              }
            
              @override
              void operator []=(
                int index,
                E value,
              ) =>
                  this.set(index, value);
              @override
              void setAll(
                int index,
                Iterable<E> iterable,
              ) {}
              @override
              void setRange(
                int start,
                int end,
                Iterable<E> iterable, [
                int skipCount = 0,
              ]) {}
              @override
              void fillRange(
                int start,
                int end, [
                E? fillValue = null,
              ]) {}
              @override
              void sort(
                  [int Function(
                    E,
                    E,
                  )?
                      compare = null]) {}
              @override
              void shuffle([Random? random = null]) {}
              @override
              void insert(
                int index,
                E element,
              ) {
                throw UnsupportedError("Cannot add to a writeable list");
              }
            
              @override
              void insertAll(
                int index,
                Iterable<E> iterable,
              ) {
                throw UnsupportedError("Cannot add to a writeable list");
              }
            
              @override
              E removeAt(int index) {
                throw UnsupportedError("Cannot remove from a writeable list");
              }
            
              @override
              E removeLast() {
                throw UnsupportedError("Cannot remove from a writeable list");
              }
            
              @override
              void removeRange(
                int start,
                int end,
              ) {
                throw UnsupportedError("Cannot remove from a writeable list");
              }
            
              @override
              void replaceRange(
                int start,
                int end,
                Iterable<E> replacements,
              ) {
                throw UnsupportedError("Cannot remove from a writeable list");
              }
            
              @override
              void add(E value) {
                throw UnsupportedError("Cannot add to a writeable list");
              }
            
              @override
              void addAll(Iterable<E> iterable) {
                throw UnsupportedError("Cannot add to a writeable list");
              }
            
              @override
              void clear() {
                throw UnsupportedError("Cannot clear a writeable list");
              }
            
              @override
              bool remove(Object? value) {
                throw UnsupportedError("Cannot remove from a writeable list");
              }
            
              @override
              void removeWhere(bool Function(E) test) {
                throw UnsupportedError("Cannot remove from a writeable list");
              }
            
              @override
              void retainWhere(bool Function(E) test) {
                throw UnsupportedError("Cannot remove from a writeable list");
              }
            }
            """
        )
    }

    @Test
    fun `class implementing Array`() = assertCompile {
        kotlin(
            """
            import dart.math.Random

            class EmptyArray<E> : Array<E> {
                override val size: Int = 0
                override fun <R> cast() = EmptyArray<R>()
                override operator fun get(index: Int): E = throw UnsupportedError("Empty")
                override fun reversed() = this
                override fun indexOf(element: @UnsafeVariance E, start: Int): Int = -1
                override fun indexOfFirst(start: Int, predicate: (element: E) -> Boolean): Int = -1
                override fun indexOfLast(start: Int?, predicate: (element: E) -> Boolean): Int = -1
                override fun lastIndexOf(element: @UnsafeVariance E, start: Int?): Int = -1
                override operator fun plus(other: List<@UnsafeVariance E>) = other
                override fun subList(start: Int, end: Int?) = EmptyArray<E>()
                override fun slice(start: Int, end: Int) = EmptyArray<E>()
                override fun asMap(): Map<Int, E> = throw UnsupportedError("Empty")
                override fun equals(other: Any?) = other is List<E> && other.isEmpty()
                override fun iterator(): Iterator<E> = throw UnsupportedError("Empty")
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = elements
                override fun <T> map(transform: (element: E) -> T) = EmptyArray<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = EmptyArray<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = EmptyArray<T>()
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
            }
            """
        )

        dart(
            """
            import "dart:math";
            import "package:meta/meta.dart";
            
            @sealed
            class EmptyArray<E> implements List<E>, FixedSizeListMarker {
              @nonVirtual
              int _${"$"}lengthBackingField = 0;
              @override
              int get length {
                return this._${"$"}lengthBackingField;
              }
            
              @override
              void set length(int ${"$"}value) {
                throw UnsupportedError("Cannot change the size of a fixed-size list");
              }
            
              @override
              EmptyArray<R> cast<R>() {
                return EmptyArray<R>();
              }
            
              @override
              E get(int index) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E operator [](int index) => this.get(index);
              @override
              EmptyArray<E> get reversed {
                return this;
              }
            
              @override
              int indexOf(
                E element, [
                int start = 0,
              ]) {
                return -1;
              }
            
              @override
              int indexWhere(
                bool Function(E) test, [
                int start = 0,
              ]) {
                return -1;
              }
            
              @override
              int lastIndexWhere(
                bool Function(E) test, [
                int? start = 0,
              ]) {
                return -1;
              }
            
              @override
              int lastIndexOf(
                E element, [
                int? start = null,
              ]) {
                return -1;
              }
            
              @override
              List<E> plus(List<E> other) {
                return other;
              }
            
              @override
              List<E> operator +(List<E> other) => this.plus(other);
              @override
              EmptyArray<E> sublist(
                int start, [
                int? end = null,
              ]) {
                return EmptyArray<E>();
              }
            
              @override
              EmptyArray<E> getRange(
                int start,
                int end,
              ) {
                return EmptyArray<E>();
              }
            
              @override
              Map<int, E> asMap() {
                throw UnsupportedError("Empty");
              }
            
              bool equals(Object? other) {
                return other is List<E> && (other as List<E>).isEmpty;
              }
            
              @override
              bool operator ==(Object? other) => this.equals(other);
              @override
              Iterator<E> get iterator {
                throw UnsupportedError("Empty");
              }
            
              @override
              Iterable<E> followedBy(Iterable<E> other) {
                return other;
              }
            
              @override
              EmptyArray<T> map<T>(T Function(E) toElement) {
                return EmptyArray<T>();
              }
            
              @override
              EmptyArray<E> where(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyArray<T> whereType<T>() {
                return EmptyArray<T>();
              }
            
              @override
              EmptyArray<T> expand<T>(Iterable<T> Function(E) toElements) {
                return EmptyArray<T>();
              }
            
              @override
              bool contains(Object? element) {
                return false;
              }
            
              @override
              void forEach(void Function(E) action) {}
              @override
              E reduce(
                  E Function(
                E,
                E,
              )
                      combine) {
                throw UnsupportedError("Empty");
              }
            
              @override
              T fold<T>(
                T initialValue,
                T Function(
                  T,
                  E,
                )
                    combine,
              ) {
                throw UnsupportedError("Empty");
              }
            
              @override
              bool every(bool Function(E) test) {
                return false;
              }
            
              @override
              String join([String separator = ", "]) {
                return "";
              }
            
              @override
              bool any(bool Function(E) test) {
                return false;
              }
            
              @override
              EmptyArray<E> toList({bool growable = true}) {
                return this;
              }
            
              @override
              bool get isEmpty {
                return true;
              }
            
              @override
              bool get isNotEmpty {
                return false;
              }
            
              @override
              EmptyArray<E> take(int count) {
                return this;
              }
            
              @override
              EmptyArray<E> takeWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyArray<E> skip(int count) {
                return this;
              }
            
              @override
              EmptyArray<E> skipWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              E first = throw UnsupportedError("Empty");
              @override
              E last = throw UnsupportedError("Empty");
              @override
              E get single {
                throw UnsupportedError("Empty");
              }
            
              @override
              E firstWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E lastWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E singleWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E elementAt(int index) {
                throw UnsupportedError("Empty");
              }
            
              @override
              Set<E> toSet() {
                throw UnsupportedError("Empty");
              }
            
              @override
              E set(
                int index,
                E value,
              ) {
                throw UnsupportedError("");
              }
            
              @override
              void operator []=(
                int index,
                E value,
              ) =>
                  this.set(index, value);
              @override
              void setAll(
                int index,
                Iterable<E> iterable,
              ) {}
              @override
              void setRange(
                int start,
                int end,
                Iterable<E> iterable, [
                int skipCount = 0,
              ]) {}
              @override
              void fillRange(
                int start,
                int end, [
                E? fillValue = null,
              ]) {}
              @override
              void sort(
                  [int Function(
                    E,
                    E,
                  )?
                      compare = null]) {}
              @override
              void shuffle([Random? random = null]) {}
              @override
              void insert(
                int index,
                E element,
              ) {
                throw UnsupportedError("Cannot add to a fixed-size list");
              }
            
              @override
              void insertAll(
                int index,
                Iterable<E> iterable,
              ) {
                throw UnsupportedError("Cannot add to a fixed-size list");
              }
            
              @override
              E removeAt(int index) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              E removeLast() {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              void removeRange(
                int start,
                int end,
              ) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              void replaceRange(
                int start,
                int end,
                Iterable<E> replacements,
              ) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              void add(E value) {
                throw UnsupportedError("Cannot add to a fixed-size list");
              }
            
              @override
              void addAll(Iterable<E> iterable) {
                throw UnsupportedError("Cannot add to a fixed-size list");
              }
            
              @override
              void clear() {
                throw UnsupportedError("Cannot clear a fixed-size list");
              }
            
              @override
              bool remove(Object? value) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              void removeWhere(bool Function(E) test) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              void retainWhere(bool Function(E) test) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            }
            """
        )
    }

    @Test
    fun `class implementing MutableList`() = assertCompile {
        kotlin(
            """
            import dart.math.Random

            class EmptyMutableList<E> : MutableList<E> {
                override var size: Int = 0
                override fun <R> cast() = EmptyMutableList<R>()
                override operator fun get(index: Int): E = throw UnsupportedError("Empty")
                override fun reversed() = this
                override fun indexOf(element: @UnsafeVariance E, start: Int): Int = -1
                override fun indexOfFirst(start: Int, predicate: (element: E) -> Boolean): Int = -1
                override fun indexOfLast(start: Int?, predicate: (element: E) -> Boolean): Int = -1
                override fun lastIndexOf(element: @UnsafeVariance E, start: Int?): Int = -1
                override operator fun plus(other: List<@UnsafeVariance E>) = other
                override fun subList(start: Int, end: Int?) = EmptyMutableList<E>()
                override fun slice(start: Int, end: Int) = EmptyMutableList<E>()
                override fun asMap(): Map<Int, E> = throw UnsupportedError("Empty")
                override fun equals(other: Any?) = other is List<E> && other.isEmpty()
                override fun iterator(): MutableIterator<E> = throw UnsupportedError("Empty")
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = elements
                override fun <T> map(transform: (element: E) -> T) = EmptyMutableList<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = EmptyMutableList<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = EmptyMutableList<T>()
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
            import "dart:math";
            import "package:meta/meta.dart";
            
            @sealed
            class EmptyMutableList<E> implements List<E>, MutableListMarker {
              @override
              int length = 0;
              @override
              EmptyMutableList<R> cast<R>() {
                return EmptyMutableList<R>();
              }
            
              @override
              E get(int index) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E operator [](int index) => this.get(index);
              @override
              EmptyMutableList<E> get reversed {
                return this;
              }
            
              @override
              int indexOf(
                E element, [
                int start = 0,
              ]) {
                return -1;
              }
            
              @override
              int indexWhere(
                bool Function(E) test, [
                int start = 0,
              ]) {
                return -1;
              }
            
              @override
              int lastIndexWhere(
                bool Function(E) test, [
                int? start = 0,
              ]) {
                return -1;
              }
            
              @override
              int lastIndexOf(
                E element, [
                int? start = null,
              ]) {
                return -1;
              }
            
              @override
              List<E> plus(List<E> other) {
                return other;
              }
            
              @override
              List<E> operator +(List<E> other) => this.plus(other);
              @override
              EmptyMutableList<E> sublist(
                int start, [
                int? end = null,
              ]) {
                return EmptyMutableList<E>();
              }
            
              @override
              EmptyMutableList<E> getRange(
                int start,
                int end,
              ) {
                return EmptyMutableList<E>();
              }
            
              @override
              Map<int, E> asMap() {
                throw UnsupportedError("Empty");
              }
            
              bool equals(Object? other) {
                return other is List<E> && (other as List<E>).isEmpty;
              }
            
              @override
              bool operator ==(Object? other) => this.equals(other);
              @override
              MutableIterator<E> get iterator {
                throw UnsupportedError("Empty");
              }
            
              @override
              Iterable<E> followedBy(Iterable<E> other) {
                return other;
              }
            
              @override
              EmptyMutableList<T> map<T>(T Function(E) toElement) {
                return EmptyMutableList<T>();
              }
            
              @override
              EmptyMutableList<E> where(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyMutableList<T> whereType<T>() {
                return EmptyMutableList<T>();
              }
            
              @override
              EmptyMutableList<T> expand<T>(Iterable<T> Function(E) toElements) {
                return EmptyMutableList<T>();
              }
            
              @override
              bool contains(Object? element) {
                return false;
              }
            
              @override
              void forEach(void Function(E) action) {}
              @override
              E reduce(
                  E Function(
                E,
                E,
              )
                      combine) {
                throw UnsupportedError("Empty");
              }
            
              @override
              T fold<T>(
                T initialValue,
                T Function(
                  T,
                  E,
                )
                    combine,
              ) {
                throw UnsupportedError("Empty");
              }
            
              @override
              bool every(bool Function(E) test) {
                return false;
              }
            
              @override
              String join([String separator = ", "]) {
                return "";
              }
            
              @override
              bool any(bool Function(E) test) {
                return false;
              }
            
              @override
              EmptyMutableList<E> toList({bool growable = true}) {
                return this;
              }
            
              @override
              bool get isEmpty {
                return true;
              }
            
              @override
              bool get isNotEmpty {
                return false;
              }
            
              @override
              EmptyMutableList<E> take(int count) {
                return this;
              }
            
              @override
              EmptyMutableList<E> takeWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyMutableList<E> skip(int count) {
                return this;
              }
            
              @override
              EmptyMutableList<E> skipWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              E first = throw UnsupportedError("Empty");
              @override
              E last = throw UnsupportedError("Empty");
              @override
              E get single {
                throw UnsupportedError("Empty");
              }
            
              @override
              E firstWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E lastWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E singleWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E elementAt(int index) {
                throw UnsupportedError("Empty");
              }
            
              @override
              Set<E> toSet() {
                throw UnsupportedError("Empty");
              }
            
              @override
              E set(
                int index,
                E value,
              ) {
                throw UnsupportedError("");
              }
            
              @override
              void operator []=(
                int index,
                E value,
              ) =>
                  this.set(index, value);
              @override
              void setAll(
                int index,
                Iterable<E> iterable,
              ) {}
              @override
              void setRange(
                int start,
                int end,
                Iterable<E> iterable, [
                int skipCount = 0,
              ]) {}
              @override
              void fillRange(
                int start,
                int end, [
                E? fillValue = null,
              ]) {}
              @override
              void sort(
                  [int Function(
                    E,
                    E,
                  )?
                      compare = null]) {}
              @override
              void shuffle([Random? random = null]) {}
              @override
              void add(E value) {}
              @override
              void addAll(Iterable<E> iterable) {}
              @override
              void clear() {}
              @override
              bool remove(Object? value) {
                return false;
              }
            
              @override
              void removeWhere(bool Function(E) test) {}
              @override
              void retainWhere(bool Function(E) test) {}
              @override
              void insert(
                int index,
                E element,
              ) {}
              @override
              void insertAll(
                int index,
                Iterable<E> iterable,
              ) {}
              @override
              E removeAt(int index) {
                throw "no";
              }
            
              @override
              E removeLast() {
                throw "no";
              }
            
              @override
              void removeRange(
                int start,
                int end,
              ) {}
              @override
              void replaceRange(
                int start,
                int end,
                Iterable<E> replacements,
              ) {}
            }
            """
        )
    }

    @Test
    fun `class implementing Array but also has add method`() = assertCompile {
        kotlin(
            """
            import dart.math.Random

            class EmptyArray<E> : Array<E> {
                override val size: Int = 0
                override fun <R> cast() = EmptyArray<R>()
                override operator fun get(index: Int): E = throw UnsupportedError("Empty")
                override fun reversed() = this
                override fun indexOf(element: @UnsafeVariance E, start: Int): Int = -1
                override fun indexOfFirst(start: Int, predicate: (element: E) -> Boolean): Int = -1
                override fun indexOfLast(start: Int?, predicate: (element: E) -> Boolean): Int = -1
                override fun lastIndexOf(element: @UnsafeVariance E, start: Int?): Int = -1
                override operator fun plus(other: List<@UnsafeVariance E>) = other
                override fun subList(start: Int, end: Int?) = EmptyArray<E>()
                override fun slice(start: Int, end: Int) = EmptyArray<E>()
                override fun asMap(): Map<Int, E> = throw UnsupportedError("Empty")
                override fun equals(other: Any?) = other is List<E> && other.isEmpty()
                override fun iterator(): Iterator<E> = throw UnsupportedError("Empty")
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = elements
                override fun <T> map(transform: (element: E) -> T) = EmptyArray<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = EmptyArray<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = EmptyArray<T>()
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

                fun add(element: E) {}
            }
            """
        )

        dart(
            """
            import "dart:math";
            import "package:meta/meta.dart";
            
            @sealed
            class EmptyArray<E> implements List<E>, FixedSizeListMarker {
              @nonVirtual
              int _${"$"}lengthBackingField = 0;
              @override
              int get length {
                return this._${"$"}lengthBackingField;
              }
            
              @override
              void set length(int ${"$"}value) {
                throw UnsupportedError("Cannot change the size of a fixed-size list");
              }
            
              @override
              EmptyArray<R> cast<R>() {
                return EmptyArray<R>();
              }
            
              @override
              E get(int index) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E operator [](int index) => this.get(index);
              @override
              EmptyArray<E> get reversed {
                return this;
              }
            
              @override
              int indexOf(
                E element, [
                int start = 0,
              ]) {
                return -1;
              }
            
              @override
              int indexWhere(
                bool Function(E) test, [
                int start = 0,
              ]) {
                return -1;
              }
            
              @override
              int lastIndexWhere(
                bool Function(E) test, [
                int? start = 0,
              ]) {
                return -1;
              }
            
              @override
              int lastIndexOf(
                E element, [
                int? start = null,
              ]) {
                return -1;
              }
            
              @override
              List<E> plus(List<E> other) {
                return other;
              }
            
              @override
              List<E> operator +(List<E> other) => this.plus(other);
              @override
              EmptyArray<E> sublist(
                int start, [
                int? end = null,
              ]) {
                return EmptyArray<E>();
              }
            
              @override
              EmptyArray<E> getRange(
                int start,
                int end,
              ) {
                return EmptyArray<E>();
              }
            
              @override
              Map<int, E> asMap() {
                throw UnsupportedError("Empty");
              }
            
              bool equals(Object? other) {
                return other is List<E> && (other as List<E>).isEmpty;
              }
            
              @override
              bool operator ==(Object? other) => this.equals(other);
              @override
              Iterator<E> get iterator {
                throw UnsupportedError("Empty");
              }
            
              @override
              Iterable<E> followedBy(Iterable<E> other) {
                return other;
              }
            
              @override
              EmptyArray<T> map<T>(T Function(E) toElement) {
                return EmptyArray<T>();
              }
            
              @override
              EmptyArray<E> where(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyArray<T> whereType<T>() {
                return EmptyArray<T>();
              }
            
              @override
              EmptyArray<T> expand<T>(Iterable<T> Function(E) toElements) {
                return EmptyArray<T>();
              }
            
              @override
              bool contains(Object? element) {
                return false;
              }
            
              @override
              void forEach(void Function(E) action) {}
              @override
              E reduce(
                  E Function(
                E,
                E,
              )
                      combine) {
                throw UnsupportedError("Empty");
              }
            
              @override
              T fold<T>(
                T initialValue,
                T Function(
                  T,
                  E,
                )
                    combine,
              ) {
                throw UnsupportedError("Empty");
              }
            
              @override
              bool every(bool Function(E) test) {
                return false;
              }
            
              @override
              String join([String separator = ", "]) {
                return "";
              }
            
              @override
              bool any(bool Function(E) test) {
                return false;
              }
            
              @override
              EmptyArray<E> toList({bool growable = true}) {
                return this;
              }
            
              @override
              bool get isEmpty {
                return true;
              }
            
              @override
              bool get isNotEmpty {
                return false;
              }
            
              @override
              EmptyArray<E> take(int count) {
                return this;
              }
            
              @override
              EmptyArray<E> takeWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyArray<E> skip(int count) {
                return this;
              }
            
              @override
              EmptyArray<E> skipWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              E first = throw UnsupportedError("Empty");
              @override
              E last = throw UnsupportedError("Empty");
              @override
              E get single {
                throw UnsupportedError("Empty");
              }
            
              @override
              E firstWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E lastWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E singleWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError("Empty");
              }
            
              @override
              E elementAt(int index) {
                throw UnsupportedError("Empty");
              }
            
              @override
              Set<E> toSet() {
                throw UnsupportedError("Empty");
              }
            
              @override
              E set(
                int index,
                E value,
              ) {
                throw UnsupportedError("");
              }
            
              @override
              void operator []=(
                int index,
                E value,
              ) =>
                  this.set(index, value);
              @override
              void setAll(
                int index,
                Iterable<E> iterable,
              ) {}
              @override
              void setRange(
                int start,
                int end,
                Iterable<E> iterable, [
                int skipCount = 0,
              ]) {}
              @override
              void fillRange(
                int start,
                int end, [
                E? fillValue = null,
              ]) {}
              @override
              void sort(
                  [int Function(
                    E,
                    E,
                  )?
                      compare = null]) {}
              @override
              void shuffle([Random? random = null]) {}
              @nonVirtual
              void add(E element) {}
              @override
              void insert(
                int index,
                E element,
              ) {
                throw UnsupportedError("Cannot add to a fixed-size list");
              }
            
              @override
              void insertAll(
                int index,
                Iterable<E> iterable,
              ) {
                throw UnsupportedError("Cannot add to a fixed-size list");
              }
            
              @override
              E removeAt(int index) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              E removeLast() {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              void removeRange(
                int start,
                int end,
              ) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              void replaceRange(
                int start,
                int end,
                Iterable<E> replacements,
              ) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              void addAll(Iterable<E> iterable) {
                throw UnsupportedError("Cannot add to a fixed-size list");
              }
            
              @override
              void clear() {
                throw UnsupportedError("Cannot clear a fixed-size list");
              }
            
              @override
              bool remove(Object? value) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              void removeWhere(bool Function(E) test) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            
              @override
              void retainWhere(bool Function(E) test) {
                throw UnsupportedError("Cannot remove from a fixed-size list");
              }
            }
            """
        )
    }
}