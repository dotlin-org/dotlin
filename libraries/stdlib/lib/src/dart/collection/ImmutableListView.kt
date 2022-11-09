/*
 * Copyright 2021-2022 Wilko Manger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dart.collection

import dotlin.intrinsics.*

// Stub: Rest will be generated later by dtgen.

@DartName("UnmodifiableListView")
external class ImmutableListView<out E>(source: Iterable<E>) : ImmutableList<E> {
    override val size: Int
    override fun <R> cast(): ImmutableList<R>
    override operator fun get(index: Int): E
    override fun reversed(): Iterable<E>
    override fun indexOf(element: @UnsafeVariance E, start: Int): Int
    override fun indexOfFirst(start: Int, predicate: (element: E) -> Boolean): Int
    override fun indexOfLast(start: Int?, predicate: (element: E) -> Boolean): Int
    override fun lastIndexOf(element: @UnsafeVariance E, start: Int?): Int
    override operator fun plus(other: List<@UnsafeVariance E>): ImmutableList<E> // TODO: Wrap
    override fun slice(start: Int, end: Int): Iterable<E>
    override fun asMap(): Map<Int, E>
    override fun subList(start: Int, end: Int?): ImmutableList<E> // TODO: Wrap
    override fun equals(other: Any?): Boolean

    override fun iterator(): Iterator<E>
    override fun plus(elements: Iterable<@UnsafeVariance E>): ImmutableList<E>
    override fun <T> map(transform: (element: E) -> T): Iterable<T>
    override fun filter(predicate: (element: E) -> Boolean): Iterable<E>
    override fun <T> filterIsInstance(): Iterable<T>
    override fun <T> flatMap(transform: (element: E) -> Iterable<T>): Iterable<T>
    override fun contains(element: Any?): Boolean
    override fun forEach(action: (element: E) -> Unit)
    override fun reduce(operator: (acc: E, element: E) -> @UnsafeVariance E): E
    override fun <T> fold(
        initial: T,
        operation: (acc: T, element: E) -> T
    ): T
    override fun all(predicate: (element: E) -> Boolean): Boolean
    override fun joinToString(separator: String): String
    override fun any(predicate: (element: E) -> Boolean): Boolean
    override fun toList(growable: Boolean): Flex<AnyList<@UnsafeVariance E>, List<@UnsafeVariance E>>
    override fun isEmpty(): Boolean
    override fun isNotEmpty(): Boolean
    override fun take(n: Int): Iterable<E>
    override fun takeWhile(predicate: (value: E) -> Boolean): Iterable<E>
    override fun drop(n: Int): Iterable<E>
    override fun dropWhile(predicate: (value: E) -> Boolean): Iterable<E>
    override val first: E
    override val last: E
    override fun single(): E
    override fun first(
        orElse: (() -> @UnsafeVariance E)?,
        predicate: (element: E) -> Boolean
    ): E
    override fun last(
        orElse: (() -> @UnsafeVariance E)?,
        predicate: (element: E) -> Boolean
    ): E
    override fun single(
        orElse: (() -> @UnsafeVariance E)?,
        predicate: (element: E) -> Boolean
    ): E
    override fun elementAt(index: Int): E
    override fun toSet(): Flex<AnySet<E>, Set<E>>
}
