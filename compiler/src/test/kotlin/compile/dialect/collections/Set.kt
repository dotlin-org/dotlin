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

@DisplayName("Compile: Dialect: Collections: Set")
class Set : BaseTest {
    @Test
    fun `setOf call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val set = setOf(0, 1, 2)
            }
            """
        )

        dart(
            """
            import "dart:collection" show UnmodifiableSetView;
            import "package:meta/meta.dart";

            void main() {
              final Set<int> set = UnmodifiableSetView<int>(<int>{0, 1, 2});
            }
            """
        )
    }

    @Test
    fun `setOf const call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val set = @const setOf(0, 1, 2)
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Set<int> set = const <int>{0, 1, 2};
            }
            """
        )
    }

    @Test
    fun `setOf const call in const val`() = assertCompile {
        kotlin(
            """
            fun main() {
                const val set = setOf(0, 1, 2)
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              const Set<int> set = <int>{0, 1, 2};
            }
            """
        )
    }

    @Test
    fun `emptySet call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val set = emptySet<String>()
            }
            """
        )

        dart(
            """
            import "dart:collection" show UnmodifiableSetView;
            import "package:meta/meta.dart";

            void main() {
              final Set<String> set = UnmodifiableSetView<String>(<String>{});
            }
            """
        )
    }

    @Test
    fun `emptySet const call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val set = @const emptySet<String>()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Set<String> set = const <String>{};
            }
            """
        )
    }

    @Test
    fun `emptySet const call in const val`() = assertCompile {
        kotlin(
            """
            fun main() {
                const val set = emptySet<String>()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              const Set<String> set = <String>{};
            }
            """
        )
    }

    @Test
    fun `mutableSetOf call`() = assertCompile {
        kotlin(
            """
            fun main() {
                val set = mutableSetOf(0, 1, 2)
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Set<int> set = <int>{0, 1, 2};
            }
            """
        )
    }


    @Test
    fun `use Kotlin ImmutableSet as Dart Set in Kotlin external function`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                test(setOf())
            }

            @DartLibrary("test.dart")
            external fun test(set: Flex<AnySet<Int>, Set<Int>>)
            """
        )

        dart(
            """
            void test(Set<int> set) {}
            """,
            Path("lib/test.dart"),
            assert = false,
        )

        dart(
            """
            import "dart:collection" show UnmodifiableSetView;
            import "test.dart" show test;
            import "package:meta/meta.dart";

            void main() {
              test(UnmodifiableSetView<int>(<int>{}));
            }
            """
        )
    }

    @Test
    fun `use Kotlin MutableSet as Dart Set in external function`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                test(mutableSetOf())
            }

            @DartLibrary("test.dart")
            external fun test(set: Flex<AnySet<Int>, Set<Int>>)
            """
        )

        dart(
            """
            void test(Set<int> set) {}
            """,
            Path("lib/test.dart"),
            assert = false,
        )

        dart(
            """
            import "test.dart" show test;
            import "package:meta/meta.dart";

            void main() {
              test(<int>{});
            }
            """
        )
    }

    @Test
    fun `assign Dart Set subtype to Kotlin MutableSet`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val mySet: MutableSet<Int> = MySet<Int>()
            }

            @DartLibrary("my_set.dart")
            external class MySet<E> : Flex<AnySet<E>, Set<E>> {
                override var size: Int
                override fun iterator(): MutableIterator<E>
                override fun add(value: E): Boolean
                override fun addAll(elements: Iterable<E>): Unit
                override fun clear(): Unit
                override fun remove(value: Any?): Boolean
                override fun removeIf(predicate: (element: E) -> Boolean): Unit
                override fun retainIf(predicate: (element: E) -> Boolean): Unit
                override fun equals(other: Any?): Boolean
                override fun <R> cast(): MutableSet<R>
                
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = elements
                override fun <T> map(transform: (element: E) -> T) = MySet<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = MySet<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = MySet<T>()
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
                override fun toList(growable: Boolean): List<E>
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
                
                override fun lookup(element: Any?): E?
                override fun containsAll(other: Iterable<Any?>): Boolean
                override fun intersect(other: Set<Any?>): MutableSet<E>
                override fun union(other: Set<E>): MutableSet<E>
                override fun subtract(other: Set<Any?>): MutableSet<E>
                
                override fun removeAll(other: Iterable<Any?>): Unit
                override fun retainAll(other: Iterable<Any?>): Unit
            }
            """
        )

        dart(
            """
            class MySet<E> implements Set<E> {
              dynamic noSuchMethod(Invocation invocation) {}
            }
            """,
            Path("lib/my_set.dart"),
            assert = false
        )

        dart(
            """
            import "my_set.dart" show MySet;
            import "package:meta/meta.dart";

            void main() {
              final Set<int> mySet = MySet<int>();
            }
            """
        )
    }

    @Test
    fun `assign Dart Set to Kotlin ImmutableSet`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val myList: ImmutableSet<Int> = calculate()
            }

            @DartLibrary("calc.dart")
            external fun calculate(): Flex<AnySet<Int>, Set<Int>>
            """
        )

        dart(
            """
            Set<int> calculate() => {};
            """,
            Path("lib/calc.dart"),
            assert = false,
        )

        dart(
            """
            import "calc.dart" show calculate;
            import "package:meta/meta.dart";

            void main() {
              final Set<int> myList = calculate();
            }
            """
        )
    }

    @Test
    fun `assign Dart Set subtype to Kotlin ImmutableSet`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val mySet: ImmutableSet<Int> = MySet<Int>()
            }

            @DartLibrary("my_set.dart")
            external class MySet<E> : Flex<AnySet<E>, Set<E>> {
                override var size: Int
                override fun iterator(): MutableIterator<E>
                override fun add(value: E): Boolean
                override fun addAll(elements: Iterable<E>): Unit
                override fun clear(): Unit
                override fun remove(value: Any?): Boolean
                override fun removeIf(predicate: (element: E) -> Boolean): Unit
                override fun retainIf(predicate: (element: E) -> Boolean): Unit
                override fun equals(other: Any?): Boolean
                override fun <R> cast(): MutableSet<R>
                
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = elements
                override fun <T> map(transform: (element: E) -> T) = MySet<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = MySet<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = MySet<T>()
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
                override fun toList(growable: Boolean): List<E>
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
                
                override fun lookup(element: Any?): E?
                override fun containsAll(other: Iterable<Any?>): Boolean
                override fun intersect(other: Set<Any?>): MutableSet<E>
                override fun union(other: Set<E>): MutableSet<E>
                override fun subtract(other: Set<Any?>): MutableSet<E>
                
                override fun removeAll(other: Iterable<Any?>): Unit
                override fun retainAll(other: Iterable<Any?>): Unit
            }
            """
        )

        dart(
            """
            class MySet<E> implements Set<E> {
              dynamic noSuchMethod(Invocation invocation) {}
            }
            """,
            Path("lib/my_set.dart"),
            assert = false
        )

        dart(
            """
            import "my_set.dart" show MySet;
            import "package:meta/meta.dart";

            void main() {
              final Set<int> mySet = MySet<int>();
            }
            """
        )
    }

    @Test
    fun `is Set`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj is Set<Int>) {
                    obj.size
                }
            }

            fun calculate(): Any? = null
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object? obj = calculate();
              if (obj is Set<int>) {
                (obj as Set<int>).length;
              }
            }

            Object? calculate() {
              return null;
            }
            """
        )
    }

    @Test
    fun `!is Set`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj !is Set<Int>) {

                }
            }

            fun calculate(): Any? = null
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final Object? obj = calculate();
              if (obj is! Set<int>) {}
            }

            Object? calculate() {
              return null;
            }
            """
        )
    }

    @Test
    fun `is ImmutableSet`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj is ImmutableSet<Int>) {
                    obj.size
                }
            }

            fun calculate(): Any? = null
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show isImmutableSet;
            import "package:meta/meta.dart";

            void main() {
              final Object? obj = calculate();
              if (isImmutableSet<int>(obj)) {
                (obj as Set<int>).length;
              }
            }

            Object? calculate() {
              return null;
            }
            """
        )
    }

    @Test
    fun `!is ImmutableSet`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj !is ImmutableSet<Int>) {
                    
                }
            }

            fun calculate(): Any? = null
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show isImmutableSet;
            import "package:meta/meta.dart";

            void main() {
              final Object? obj = calculate();
              if (!isImmutableSet<int>(obj)) {}
            }

            Object? calculate() {
              return null;
            }
            """
        )
    }

    @Test
    fun `is MutableSet`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj is MutableSet<Int>) {
                    obj.size
                }
            }

            fun calculate(): Any? = null
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show isMutableSet;
            import "package:meta/meta.dart";

            void main() {
              final Object? obj = calculate();
              if (isMutableSet<int>(obj)) {
                (obj as Set<int>).length;
              }
            }

            Object? calculate() {
              return null;
            }
            """
        )
    }

    @Test
    fun `!is MutableSet`() = assertCompile {
        kotlin(
            """
            fun main() {
                val obj = calculate()
                if (obj !is MutableSet<Int>) {}
            }

            fun calculate(): Any? = null
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/collection_type_checks.dt.g.dart"
                show isMutableSet;
            import "package:meta/meta.dart";

            void main() {
              final Object? obj = calculate();
              if (!isMutableSet<int>(obj)) {}
            }

            Object? calculate() {
              return null;
            }
            """
        )
    }

    @Test
    fun `class implementing ImmutableSet`() = assertCompile {
        kotlin(
            """
            class EmptyImmutableSet<out E> : ImmutableSet<E> {
                override val size: Int = 0
                override fun <R> cast() = EmptyImmutableSet<R>()
                override fun equals(other: Any?) = other is List<E> && other.isEmpty()
                override fun iterator(): Iterator<E> = throw UnsupportedError("Empty")
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = throw "no"
                override fun <T> map(transform: (element: E) -> T) = EmptyImmutableSet<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = EmptyImmutableSet<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = EmptyImmutableSet<T>()
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
                override fun toList(growable: Boolean) = throw ""
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

                override fun lookup(element: Any?): E? = throw "no"
            
                override fun containsAll(other: Iterable<Any?>): Boolean = throw "no"
                
                override fun intersect(other: Set<Any?>): Set<E> = throw "no"
            
                override fun union(other: Set<@UnsafeVariance E>): Set<E> = throw "no"
                
                override fun subtract(other: Set<Any?>): Set<E> = throw "no"
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/collection_markers.dt.g.dart"
                show ImmutableSetMarker;
            import "package:dotlin/src/kotlin/collections/collection.dt.g.dart"
                show MutableCollection;
            import "package:meta/meta.dart";

            @sealed
            class EmptyImmutableSet<E> implements Set<E>, ImmutableSetMarker {
              @nonVirtual
              int _${'$'}lengthBackingField = 0;
              @override
              int get length {
                return this._${'$'}lengthBackingField;
              }
            
              @override
              void set length(int ${'$'}value) {
                throw UnsupportedError("Cannot change the size of an immutable set");
              }
            
              @override
              EmptyImmutableSet<R> cast<R>() {
                return EmptyImmutableSet<R>();
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
                throw "no";
              }
            
              @override
              EmptyImmutableSet<T> map<T>(T Function(E) toElement) {
                return EmptyImmutableSet<T>();
              }
            
              @override
              EmptyImmutableSet<E> where(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyImmutableSet<T> whereType<T>() {
                return EmptyImmutableSet<T>();
              }
            
              @override
              EmptyImmutableSet<T> expand<T>(Iterable<T> Function(E) toElements) {
                return EmptyImmutableSet<T>();
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
              Never toList({bool growable = true}) {
                throw "";
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
              EmptyImmutableSet<E> take(int count) {
                return this;
              }
            
              @override
              EmptyImmutableSet<E> takeWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyImmutableSet<E> skip(int count) {
                return this;
              }
            
              @override
              EmptyImmutableSet<E> skipWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              final E first = throw UnsupportedError("Empty");
              @override
              final E last = throw UnsupportedError("Empty");
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
              E? lookup(Object? object) {
                throw "no";
              }
            
              @override
              bool containsAll(Iterable<Object?> other) {
                throw "no";
              }
            
              @override
              Set<E> intersection(Set<Object?> other) {
                throw "no";
              }
            
              @override
              Set<E> union(Set<E> other) {
                throw "no";
              }
            
              @override
              Set<E> difference(Set<Object?> other) {
                throw "no";
              }
            
              @override
              bool add(E value) {
                throw UnsupportedError("Cannot add to an immutable set");
              }
            
              @override
              void removeAll(Iterable<Object?> other) {
                throw UnsupportedError("Cannot remove from an immutable set");
              }
            
              @override
              void retainAll(Iterable<Object?> other) {
                throw UnsupportedError("Cannot remove from an immutable set");
              }
            
              @override
              void addAll(Iterable<E> iterable) {
                throw UnsupportedError("Cannot add to an immutable set");
              }
            
              @override
              void clear() {
                throw UnsupportedError("Cannot clear an immutable set");
              }
            
              @override
              bool remove(Object? value) {
                throw UnsupportedError("Cannot remove from an immutable set");
              }
            
              @override
              void removeWhere(bool Function(E) test) {
                throw UnsupportedError("Cannot remove from an immutable set");
              }
            
              @override
              void retainWhere(bool Function(E) test) {
                throw UnsupportedError("Cannot remove from an immutable set");
              }
            }
            """
        )
    }

    @Test
    fun `class implementing MutableSet`() = assertCompile {
        kotlin(
            """
            class EmptyMutableSet<E> : MutableSet<E> {
                override var size: Int = 0
                override fun <R> cast() = EmptyMutableSet<R>()
                override fun equals(other: Any?) = other is List<E> && other.isEmpty()
                override fun iterator(): MutableIterator<E> = throw UnsupportedError("Empty")
                override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E> = throw "no"
                override fun <T> map(transform: (element: E) -> T) = EmptyMutableSet<T>()
                override fun filter(predicate: (element: E) -> Boolean) = this
                override fun <T> filterIsInstance() = EmptyMutableSet<T>()
                override fun <T> flatMap(transform: (element: E) -> Iterable<T>) = EmptyMutableSet<T>()
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
                override fun toList(growable: Boolean) = throw ""
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
                override fun toSet(): MutableSet<E> = throw UnsupportedError("Empty")
                override fun lookup(element: Any?): E? = throw "no"
                override fun containsAll(other: Iterable<Any?>): Boolean = throw "no"
                override fun intersect(other: Set<Any?>): MutableSet<E> = throw "no"
                override fun union(other: Set<@UnsafeVariance E>): MutableSet<E> = throw "no"
                override fun subtract(other: Set<Any?>): MutableSet<E> = throw "no"

                override fun add(element: E): Boolean = false
                override fun addAll(elements: Iterable<E>) {}
                override fun clear() {}
                override fun remove(value: Any?) = false
                override fun removeAll(other: Iterable<Any?>) {}
                override fun retainAll(other: Iterable<Any?>) {}
                override fun removeIf(predicate: (element: E) -> Boolean) {}
                override fun retainIf(predicate: (element: E) -> Boolean) {}
            }
            """
        )

        dart(
            """
            import "package:dotlin/src/dotlin/intrinsics/collection_markers.dt.g.dart"
                show MutableSetMarker;
            import "package:dotlin/src/kotlin/collections/iterator.dt.g.dart"
                show MutableIterator;
            import "package:meta/meta.dart";

            @sealed
            class EmptyMutableSet<E> implements Set<E>, MutableSetMarker {
              @override
              int length = 0;
              @override
              EmptyMutableSet<R> cast<R>() {
                return EmptyMutableSet<R>();
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
                throw "no";
              }
            
              @override
              EmptyMutableSet<T> map<T>(T Function(E) toElement) {
                return EmptyMutableSet<T>();
              }
            
              @override
              EmptyMutableSet<E> where(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyMutableSet<T> whereType<T>() {
                return EmptyMutableSet<T>();
              }
            
              @override
              EmptyMutableSet<T> expand<T>(Iterable<T> Function(E) toElements) {
                return EmptyMutableSet<T>();
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
              Never toList({bool growable = true}) {
                throw "";
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
              EmptyMutableSet<E> take(int count) {
                return this;
              }
            
              @override
              EmptyMutableSet<E> takeWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              EmptyMutableSet<E> skip(int count) {
                return this;
              }
            
              @override
              EmptyMutableSet<E> skipWhile(bool Function(E) test) {
                return this;
              }
            
              @override
              final E first = throw UnsupportedError("Empty");
              @override
              final E last = throw UnsupportedError("Empty");
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
              E? lookup(Object? object) {
                throw "no";
              }
            
              @override
              bool containsAll(Iterable<Object?> other) {
                throw "no";
              }
            
              @override
              Set<E> intersection(Set<Object?> other) {
                throw "no";
              }
            
              @override
              Set<E> union(Set<E> other) {
                throw "no";
              }
            
              @override
              Set<E> difference(Set<Object?> other) {
                throw "no";
              }
            
              @override
              bool add(E value) {
                return false;
              }
            
              @override
              void addAll(Iterable<E> iterable) {}
              @override
              void clear() {}
              @override
              bool remove(Object? value) {
                return false;
              }
            
              @override
              void removeAll(Iterable<Object?> other) {}
              @override
              void retainAll(Iterable<Object?> other) {}
              @override
              void removeWhere(bool Function(E) test) {}
              @override
              void retainWhere(bool Function(E) test) {}
            }
            """
        )
    }
 }