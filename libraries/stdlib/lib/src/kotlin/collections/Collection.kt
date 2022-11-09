/*
 * Copyright 2010-2020 JetBrains s.r.o.
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

import kotlin.internal.PlatformDependent

/**
 * A generic collection of elements. Methods in this interface support only read-only access to the collection;
 * read/write access is supported through the [MutableCollection] interface.
 * @param E the type of elements contained in the collection. The collection is covariant in its element type.
 */
interface Collection<out E> : Iterable<E>(Interface) {
    // Dart methods/properties
    /**
     * Returns the size of the collection.
     */
    override val size: Int

    override fun <R> cast(): Collection<R>

    // Bulk Operations
    /**
     * Checks if all elements in the specified collection are contained in this collection.
     */
    // TODO: Should be extension
    //  fun containsAll(elements: Collection<@UnsafeVariance E>): Boolean
}

/**
 * A generic immutable collection of elements. Methods in this
 * interface support only read-only access to the collection.
 *
 * Implementors of this interface take responsibility to be immutable.
 * Once constructed they must contain the same elements in the same order.
 *
 * @param E the type of elements contained in the collection. The immutable collection is covariant on its element type.
 */
interface ImmutableCollection<out E> : Collection<E>

/**
 * A generic collection of elements that supports adding and removing elements.
 *
 * @param E the type of elements contained in the collection. The mutable collection is invariant in its element type.
 */
interface MutableCollection<E> : Collection<E>, MutableIterable<E> {
    override var size: Int

    // Query Operations
    override fun iterator(): MutableIterator<E>

    // Dart methods
    override fun <R> cast(): MutableCollection<R>

    /**
     * Adds the specified element to the collection.
     *
     * @return `true` if the element has been added, `false` if the collection does not support duplicates
     * and the element is already contained in the collection.
     */
    fun add(value: E): Unit

    fun addAll(@DartName("iterable") elements: Iterable<E>): Unit

    fun clear(): Unit

    fun remove(value: Any?): Boolean

    @DartName("removeWhere")
    fun removeIf(@DartName("test") predicate: (element: E) -> Boolean): Unit

    @DartName("retainWhere")
    fun retainIf(@DartName("test") predicate: (element: E) -> Boolean): Unit
}