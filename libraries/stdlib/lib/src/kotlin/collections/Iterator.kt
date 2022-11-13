/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

@file:Suppress("INAPPLICABLE_OPERATOR_MODIFIER", "WRONG_MODIFIER_CONTAINING_DECLARATION")

package kotlin.collections

/**
 * An interface for getting items, one at a time, from an object.
 *
 * The for-in construct transparently uses `Iterator` to test for the end
 * of the iteration, and to get each item (or _element_).
 *
 * If the object iterated over is changed during the iteration, the
 * behavior is unspecified.
 *
 * The `Iterator` is initially positioned before the first element.
 * Before accessing the first element the iterator must thus be advanced using
 * [moveNext] to point to the first element.
 * If no element is left, then [moveNext] returns false,
 * and all further calls to [moveNext] will also return false.
 *
 * The [current] value must not be accessed before calling [moveNext]
 * or after a call to [moveNext] has returned false.
 */
@DartLibrary("dart:core")
external interface Iterator<out E> {
    /**
     * Advances the iterator to the next element of the iteration.
     *
     * Should be called before reading [current].
     * If the call to `moveNext` returns `true`,
     * then [current] will contain the next element of the iteration
     * until `moveNext` is called again.
     * If the call returns `false`, there are no further elements
     * and [current] should not be used any more.
     *
     * It is safe to call [moveNext] after it has already returned `false`,
     * but it must keep returning `false` and not have any other effect.
     *
     * A call to `moveNext` may throw for various reasons,
     * including a concurrent change to an underlying collection.
     * If that happens, the iterator may be in an inconsistent
     * state, and any further behavior of the iterator is unspecified,
     * including the effect of reading [current].
     */
    open fun moveNext(): Boolean

    /**
     * The current element.
     *
     * If the iterator has not yet been moved to the first element
     * ([moveNext] has not been called yet),
     * or if the iterator has been moved past the last element of the [Iterable]
     * ([moveNext] has returned false),
     * then [current] is unspecified.
     * An [Iterator] may either throw or return an iterator specific default value
     * in that case.
     *
     * The `current` getter should keep its value until the next call to
     * [moveNext], even if an underlying collection changes.
     * After a successful call to `moveNext`, the user doesn't need to cache
     * the current value, but can keep reading it from the iterator.
     */
    open val current: E

    // Kotlin iterator overrides. Only here to make the Kotlin compiler happy.

    /**
     * Do not use. Use `moveNext` and `current` instead.
     */
    final operator fun hasNext(): Boolean = throw UnsupportedError("Use moveNext and current")

    /**
     * Do not use. Use `moveNext` and `current` instead.
     */
    final operator fun next(): E = throw UnsupportedError("Use moveNext and current")
}

/**
 * An iterator over a mutable collection. Provides the ability to remove elements while iterating.
 * @see MutableCollection.iterator
 */
interface MutableIterator<out T> : Iterator<T> {
    /**
     * Removes from the underlying collection the [current] element.
     *
     * The [current] won't change, unless [moveNext] (or in the case of [BidirectionalIterator], [movePrevious]) is
     * called.
     */
    fun remove(): Unit
}

/**
 * An iterator over a collection that supports back-and-forth sequential access to elements.
 */
interface BidirectionalIterator<out T> : Iterator<T> {
    /**
     * Move back to the previous element.
     *
     * Returns true and updates [current] if successful. Returns false
     * and updates [current] to an implementation defined state if there is no
     * previous element.
     */
    fun movePrevious(): Boolean
    override fun moveNext(): Boolean
    override val current: T
}

/**
 * An iterator over a collection that supports indexed access.
 *
 * @see List.listIterator
 */
interface ListIterator<out T> : BidirectionalIterator<T> {
    override fun movePrevious(): Boolean
    override fun moveNext(): Boolean
    override val current: T

    /**
     * T index of the [current] element.
     */
    val currentIndex: Int
}

/**
 * An iterator over a mutable collection that supports indexed access. Provides the ability
 * to add, modify and remove elements while iterating.
 */
interface MutableListIterator<T> : ListIterator<T>, MutableIterator<T> {
    override fun movePrevious(): Boolean
    override fun moveNext(): Boolean
    override val current: T

    override fun remove(): Unit

    /**
     * Replaces the [current] element with the specified [element].
     *
     * The [current] element won't change, only after a call to [moveNext] or [movePrevious] will the value
     * be up to date.
     */
    fun set(element: T): Unit

    /**
     * Adds the specified [element] into the underlying collection immediately before the [current] element, if any,
     * (If the collection contains no elements, the new element becomes the sole element in the collection.)
     */
    fun add(element: T): Unit
}
