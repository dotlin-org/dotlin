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
            import 'dart:collection';
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'dart:collection';
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

            void main() {
              final Set<int> set = <int>{0, 1, 2};
            }
            """
        )
    }


    @Test
    fun `use Kotlin ImmutableSet as Dart Set in external function`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                test(setOf())
            }

            external fun test(list: Flex<AnySet<Int>, Set<Int>>)
            """
        )

        dart(
            """
            import 'dart:collection';
            import 'package:meta/meta.dart';

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

            external fun test(list: Flex<AnySet<Int>, Set<Int>>)
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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

            external fun calculate(): Flex<AnySet<Int>, Set<Int>>
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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

            external fun calculate(): Any
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final Object obj = calculate();
              if (obj is Set<int>) {
                (obj as Set<int>).length;
              }
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

            external fun calculate(): Any
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final Object obj = calculate();
              if (obj is! Set<int>) {
                {}
              }
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

            external fun calculate(): Any
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final Object obj = calculate();
              if (obj.isImmutableSet<int>()) {
                (obj as Set<int>).length;
              }
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

            external fun calculate(): Any
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final Object obj = calculate();
              if (!obj.isImmutableSet<int>()) {
                {}
              }
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

            external fun calculate(): Any
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final Object obj = calculate();
              if (obj.isMutableSet<int>()) {
                (obj as Set<int>).length;
              }
            }
            """
        )
    }

    @Test
    fun `!is MutableSet`() = assertCompile {
        kotlin(
            """
            import dotlin.intrinsics.*

            fun main() {
                val obj = calculate()
                if (obj !is MutableSet<Int>) {
                    
                }
            }

            external fun calculate(): Any
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void main() {
              final Object obj = calculate();
              if (!obj.isMutableSet<int>()) {
                {}
              }
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
            import 'package:meta/meta.dart';

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
                throw UnsupportedError('Cannot change the size of an immutable set');
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
                throw UnsupportedError('Empty');
              }
            
              @override
              Iterable<E> followedBy(Iterable<E> other) {
                throw 'no';
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
                throw UnsupportedError('Empty');
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
                throw UnsupportedError('Empty');
              }
            
              @override
              bool every(bool Function(E) test) {
                return false;
              }
            
              @override
              String join([String separator = ', ']) {
                return '';
              }
            
              @override
              bool any(bool Function(E) test) {
                return false;
              }
            
              @override
              Never toList({bool growable = true}) {
                throw '';
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
              final E first = throw UnsupportedError('Empty');
              @override
              final E last = throw UnsupportedError('Empty');
              @override
              E get single {
                throw UnsupportedError('Empty');
              }
            
              @override
              E firstWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError('Empty');
              }
            
              @override
              E lastWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError('Empty');
              }
            
              @override
              E singleWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError('Empty');
              }
            
              @override
              E elementAt(int index) {
                throw UnsupportedError('Empty');
              }
            
              @override
              Set<E> toSet() {
                throw UnsupportedError('Empty');
              }
            
              @override
              E? lookup(Object? object) {
                throw 'no';
              }
            
              @override
              bool containsAll(Iterable<Object?> other) {
                throw 'no';
              }
            
              @override
              Set<E> intersection(Set<Object?> other) {
                throw 'no';
              }
            
              @override
              Set<E> union(Set<E> other) {
                throw 'no';
              }
            
              @override
              Set<E> difference(Set<Object?> other) {
                throw 'no';
              }
            
              @override
              bool add(E value) {
                throw UnsupportedError('Cannot add to an immutable set');
              }
            
              @override
              void removeAll(Iterable<Object?> other) {
                throw UnsupportedError('Cannot remove from an immutable set');
              }
            
              @override
              void retainAll(Iterable<Object?> other) {
                throw UnsupportedError('Cannot remove from an immutable set');
              }
            
              @override
              void addAll(Iterable<E> iterable) {
                throw UnsupportedError('Cannot add to an immutable set');
              }
            
              @override
              void clear() {
                throw UnsupportedError('Cannot clear an immutable set');
              }
            
              @override
              bool remove(Object? value) {
                throw UnsupportedError('Cannot remove from an immutable set');
              }
            
              @override
              void removeWhere(bool Function(E) test) {
                throw UnsupportedError('Cannot remove from an immutable set');
              }
            
              @override
              void retainWhere(bool Function(E) test) {
                throw UnsupportedError('Cannot remove from an immutable set');
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
            import 'dart:math';
            import 'package:meta/meta.dart';
            
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
                throw UnsupportedError('Empty');
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
                throw UnsupportedError('Empty');
              }
            
              bool equals(Object? other) {
                return other is List<E> && (other as List<E>).isEmpty;
              }
            
              @override
              bool operator ==(Object? other) => this.equals(other);
              @override
              MutableIterator<E> get iterator {
                throw UnsupportedError('Empty');
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
                throw UnsupportedError('Empty');
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
                throw UnsupportedError('Empty');
              }
            
              @override
              bool every(bool Function(E) test) {
                return false;
              }
            
              @override
              String join([String separator = ', ']) {
                return '';
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
              E first = throw UnsupportedError('Empty');
              @override
              E last = throw UnsupportedError('Empty');
              @override
              E get single {
                throw UnsupportedError('Empty');
              }
            
              @override
              E firstWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError('Empty');
              }
            
              @override
              E lastWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError('Empty');
              }
            
              @override
              E singleWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError('Empty');
              }
            
              @override
              E elementAt(int index) {
                throw UnsupportedError('Empty');
              }
            
              @override
              Set<E> toSet() {
                throw UnsupportedError('Empty');
              }
            
              @override
              E set(
                int index,
                E value,
              ) {
                throw UnsupportedError('');
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
                throw 'no';
              }
            
              @override
              E removeLast() {
                throw 'no';
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
            import 'dart:math';
            import 'package:meta/meta.dart';
            
            @sealed
            class EmptyArray<E> implements List<E>, FixedSizeListMarker {
              @nonVirtual
              int _${'$'}lengthBackingField = 0;
              @override
              int get length {
                return this._${'$'}lengthBackingField;
              }
            
              @override
              void set length(int ${'$'}value) {
                throw UnsupportedError('Cannot change the size of a fixed-size list');
              }
            
              @override
              EmptyArray<R> cast<R>() {
                return EmptyArray<R>();
              }
            
              @override
              E get(int index) {
                throw UnsupportedError('Empty');
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
                throw UnsupportedError('Empty');
              }
            
              bool equals(Object? other) {
                return other is List<E> && (other as List<E>).isEmpty;
              }
            
              @override
              bool operator ==(Object? other) => this.equals(other);
              @override
              Iterator<E> get iterator {
                throw UnsupportedError('Empty');
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
                throw UnsupportedError('Empty');
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
                throw UnsupportedError('Empty');
              }
            
              @override
              bool every(bool Function(E) test) {
                return false;
              }
            
              @override
              String join([String separator = ', ']) {
                return '';
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
              E first = throw UnsupportedError('Empty');
              @override
              E last = throw UnsupportedError('Empty');
              @override
              E get single {
                throw UnsupportedError('Empty');
              }
            
              @override
              E firstWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError('Empty');
              }
            
              @override
              E lastWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError('Empty');
              }
            
              @override
              E singleWhere(
                bool Function(E) test, {
                E Function()? orElse = null,
              }) {
                throw UnsupportedError('Empty');
              }
            
              @override
              E elementAt(int index) {
                throw UnsupportedError('Empty');
              }
            
              @override
              Set<E> toSet() {
                throw UnsupportedError('Empty');
              }
            
              @override
              E set(
                int index,
                E value,
              ) {
                throw UnsupportedError('');
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
                throw UnsupportedError('Cannot add to a fixed-size list');
              }
            
              @override
              void insertAll(
                int index,
                Iterable<E> iterable,
              ) {
                throw UnsupportedError('Cannot add to a fixed-size list');
              }
            
              @override
              E removeAt(int index) {
                throw UnsupportedError('Cannot remove from a fixed-size list');
              }
            
              @override
              E removeLast() {
                throw UnsupportedError('Cannot remove from a fixed-size list');
              }
            
              @override
              void removeRange(
                int start,
                int end,
              ) {
                throw UnsupportedError('Cannot remove from a fixed-size list');
              }
            
              @override
              void replaceRange(
                int start,
                int end,
                Iterable<E> replacements,
              ) {
                throw UnsupportedError('Cannot remove from a fixed-size list');
              }
            
              @override
              void addAll(Iterable<E> iterable) {
                throw UnsupportedError('Cannot add to a fixed-size list');
              }
            
              @override
              void clear() {
                throw UnsupportedError('Cannot clear a fixed-size list');
              }
            
              @override
              bool remove(Object? value) {
                throw UnsupportedError('Cannot remove from a fixed-size list');
              }
            
              @override
              void removeWhere(bool Function(E) test) {
                throw UnsupportedError('Cannot remove from a fixed-size list');
              }
            
              @override
              void retainWhere(bool Function(E) test) {
                throw UnsupportedError('Cannot remove from a fixed-size list');
              }
            }
            """
        )
    }
 }