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

package kotlin.collections

/**
 * An iterator over a collection or another entity that can be represented as a sequence of elements.
 * Allows to sequentially access the elements.
 *
 * Equivalent to `dart:core`'s `Iterator`. Any subtypes of [kotlin.Iterator] will implement
 * `dart:core`'s `Iterator` automatically.
 *
 * In Dart, `current` is updated everytime [next] (and in [BidirectionalIterator], [previous]) returns a value.
 */
interface Iterator<out T> {
    /**
     * Returns the next element in the iteration.
     */
    operator fun next(): T

    /**
     * Returns `true` if the iteration has more elements.
     */
    operator fun hasNext(): Boolean
}

/**
 * An iterator over a mutable collection. Provides the ability to remove elements while iterating.
 * @see MutableCollection.iterator
 */
interface MutableIterator<out T> : Iterator<T> {
    /**
     * Removes from the underlying collection the last element returned by this iterator.
     */
    fun remove(): Unit
}

/**
 * An iterator over a collection that supports back-and-forth sequential access to elements.
 *
 * Any subtypes of [kotlin.BidirectionalIterator] will implement `dart:core`'s `BidirectionalIterator` automatically.
 */
interface BidirectionalIterator<out T> : Iterator<T> {
    override fun next(): T
    override fun hasNext(): Boolean

    /**
     * Returns `true` if there are elements in the iteration before the current element.
     */
    fun hasPrevious(): Boolean

    /**
     * Returns the previous element in the iteration and moves the cursor position backwards.
     */
    fun previous(): T
}

/**
 * An iterator over a collection that supports indexed access.
 *
 * @see List.listIterator
 */
interface ListIterator<out T> : BidirectionalIterator<T> {
    // Query Operations
    override fun next(): T
    override fun hasNext(): Boolean

    override fun hasPrevious(): Boolean
    override fun previous(): T

    /**
     * Returns the index of the element that would be returned by a subsequent call to [next].
     */
    fun nextIndex(): Int

    /**
     * Returns the index of the element that would be returned by a subsequent call to [previous].
     */
    fun previousIndex(): Int
}

/**
 * An iterator over a mutable collection that supports indexed access. Provides the ability
 * to add, modify and remove elements while iterating.
 */
interface MutableListIterator<T> : ListIterator<T>, MutableIterator<T> {
    // Query Operations
    override fun next(): T
    override fun hasNext(): Boolean

    // Modification Operations
    override fun remove(): Unit

    /**
     * Replaces the last element returned by [next] or [previous] with the specified element [element].
     */
    fun set(element: T): Unit

    /**
     * Adds the specified element [element] into the underlying collection immediately before the element that would be
     * returned by [next], if any, and after the element that would be returned by [previous], if any.
     * (If the collection contains no elements, the new element becomes the sole element in the collection.)
     * The new element is inserted before the implicit cursor: a subsequent call to [next] would be unaffected,
     * and a subsequent call to [previous] would return the new element. (This call increases by one the value \
     * that would be returned by a call to [nextIndex] or [previousIndex].)
     */
    fun add(element: T): Unit
}
