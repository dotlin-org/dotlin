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

@DartName("UnmodifiableSetView")
external class ImmutableSetView<out E>(source: Set<E>) : ImmutableSet<E> {
    override val size: Int
    override fun <R> cast(): Set<R> // TODO: Wrap
    override fun equals(other: Any?): Boolean

    override fun lookup(element: Any?): E?
    override fun containsAll(other: Iterable<Any?>): Boolean
    override fun intersect(other: Set<Any?>): Set<E> // TODO: Wrap
    override fun union(other: Set<@UnsafeVariance E>): Set<E> // TODO: Wrap
    override fun subtract(other: Set<Any?>): Set<E> // TODO: Wrap

    override fun iterator(): Iterator<E>
    override fun plus(elements: Iterable<@UnsafeVariance E>): Iterable<E>
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
    override fun toList(growable: Boolean): Flex<AnyList<E>, List<E>>
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

    fun removeAll(other: Iterable<Any?>): Unit
    fun retainAll(other: Iterable<Any?>): Unit
}
