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

import kotlin.collections.Iterator as KtIterator

/**
 * An iterator over a mutable collection. Provides the ability to remove elements while iterating.
 * @see MutableCollection.iterator
 */
interface MutableIterator<out T> : Iterator<T>(Interface) {
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
interface BidirectionalIterator<out T> : Iterator<T>(Interface) {
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
