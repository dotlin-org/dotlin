/*
 * Copyright 2010-2019 JetBrains s.r.o.
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

import dart.math.Random
import dotlin.intrinsics.Flex

/**
 * A generic indexable ordered collection of elements. Methods in this interface support
 * only read-only access to the list; read/write access is supported through
 * the [WriteableList] or [MutableList] interface.
 *
 * Subtypes of this class implement different kinds of lists.
 * The most common kinds of lists are:
 *
 *  * **Immutable list**: [ImmutableList]
 *
 *    Does not support changing, adding or removing elements. Unlike with [List], [ImmutableList]
 *    makes the guaruantee that the list is immutable.
 *
 *  * **Fixed-size list**: [FixedSizeList] (= [Array])
 *
 *    Supports changing elements, but not adding or removing. A fized-size list, as
 *    the name implies, has a fixed size which cannot be changed.
 *
 *  * **Growable list**: [MutableList]
 *
 *    Supports changing, adding and removing elements.
 *
 * Lists are [Iterable]. Iteration occurs over values in index order. Changing
 * the values does not affect iteration, but changing the valid
 * indices&mdash;that is, changing the list's size&mdash;between iteration
 * steps causes a [ConcurrentModificationError]. This means that only growable
 * lists can throw ConcurrentModificationError. If the size changes
 * temporarily and is restored before continuing the iteration, the iterator
 * might not detect it.
 *
 * @param E the type of elements contained in the list. The list is covariant in its element type.
 */
@DartLibrary("dart:core")
external interface List<out E> : Collection<E> {
    override fun <R> cast(): List<R>

    operator fun get(index: Int): E

    @DartGetter
    fun reversed(): Iterable<E>

    @DartPositional
    fun indexOf(element: @UnsafeVariance E, start: Int = 0): Int

    @DartPositional
    @DartName("indexWhere")
    fun indexOfFirst(@DartIndex(1) start: Int = 0, @DartName("test") predicate: (element: E) -> Boolean): Int

    @DartPositional
    @DartName("lastIndexWhere")
    fun indexOfLast(@DartIndex(1) start: Int? = 0, @DartName("test") predicate: (element: E) -> Boolean): Int

    @DartPositional
    fun lastIndexOf(element: @UnsafeVariance E, start: Int? = null): Int

    operator fun plus(other: List<@UnsafeVariance E>): List<E>

    @DartPositional
    @DartName("sublist")
    fun subList(start: Int, end: Int? = null): List<E>

    @DartName("getRange")
    fun slice(start: Int, end: Int): Iterable<E>

    fun asMap(): Map<Int, E>

    // List Iterators
    /**
     * Returns a list iterator over the elements in this list (in proper sequence).
     */
    // TODO:
    //fun listIterator(): ListIterator<E>

    /**
     * Returns a list iterator over the elements in this list (in proper sequence), starting at the specified [index].
     */
    //TODO fun listIterator(index: Int): ListIterator<E>

    // TODO: Make `other` non-nullable
    override fun equals(other: Any?): Boolean
}

/**
 * A generic immutable ordered collection of elements. Methods in this interface support only
 * read-only access to the immutable list.
 *
 * Implementors of this interface take responsibility to be immutable.
 * Once constructed they must contain the same elements in the same order.
 *
 * @param E the type of elements contained in the list. The immutable list is covariant on its element type.
 */
@DartName("List")
@DartLibrary("dart:core")
interface ImmutableList<out E> : List<E>, ImmutableCollection<E>

/**
 * A generic ordered collection of elements that supports changing elements.
 */
@DartName("List")
@DartLibrary("dart:core")
external interface WriteableList<E> : List<E> {
    override var first: E

    override var last: E

    // Mutating but non-growing methods
    operator fun set(index: Int, value: E): E
    fun setAll(index: Int, @DartName("iterable") elements: Iterable<E>): Unit

    @DartPositional
    @DartName("setRange")
    fun setSlice(
        start: Int,
        end: Int,
        @DartName("iterable") elements: Iterable<E>,
        @DartName("skipCount") dropCount: Int = 0
    ): Unit

    @DartPositional
    @DartName("fillRange")
    fun fillSlice(start: Int, end: Int, @DartName("fillValue") fill: E? = null): Unit

    @DartPositional
    fun sort(@DartName("compare") selector: ((a: E, b: E) -> Int)? = null): Unit

    @DartPositional
    fun shuffle(random: Random? = null): Unit
}

// TODO
//@DartName("List")
/*external*/ typealias FixedSizeList<E> = Array<E>

/**
 * A generic ordered collection of elements that supports changing, adding and removing elements.
 *
 * The default growable list, as created by `mutableListOf()`, keeps
 * an internal buffer, and grows that buffer when necessary. This guarantees
 * that a sequence of [add] operations will each execute in amortized constant
 * time. Setting the size directly may take time proportional to the new
 * size, and may change the internal capacity so that a following add
 * operation will need to immediately increase the buffer capacity.
 * Other list implementations may have different performance behavior.
 *
 * It is generally not allowed to modify the list's length (adding or removing
 * elements) while an operation on the list is being performed,
 * for example during a call to [forEach] or [sort].
 * Changing the list's length while it is being iterated, either by iterating it
 * directly or through iterating an [Iterable] that is backed by the list, will
 * break the iteration.
 *
 * @param E the type of elements contained in the list. The mutable list is invariant in its element type.
 */
@DartName("List")
@DartLibrary("dart:core")
external interface MutableList<E> : WriteableList<E>, MutableCollection<E> {
    // List Iterators
    // TODO override fun listIterator(): MutableListIterator<E>

    // TODO override fun listIterator(index: Int): MutableListIterator<E>

    //#region List overrides
    override fun <R> cast(): MutableList<R>
    override fun subList(start: Int, end: Int?): MutableList<E>
    //#endregion

    //#region Mutating methods
    /**
     * Inserts an element into the list at the specified [index].
     */
    @DartName("insert")
    fun add(index: Int, element: E): Unit

    /**
     * Adds all of the elements of the specified collection to the end of this list.
     *
     * The elements are appended in the order they appear in the [elements] collection.
     */
    @DartName("insertAll")
    fun addAll(index: Int, @DartName("iterable") elements: Iterable<E>): Unit

    /**
     * Removes an element at the specified [index] from the list.
     *
     * @return the element that has been removed.
     */
    fun removeAt(index: Int): E

    fun removeLast(): E

    @DartName("removeRange")
    fun removeSlice(start: Int, end: Int): Unit

    @DartName("replaceRange")
    fun replaceSlice(start: Int, end: Int, @DartName("replacements") elements: Iterable<E>): Unit
    //#endregion

    // Other list variants (like Array) are also created through this companion object, but that's okay, because
    // in Dart they're all List.
    @DartName("MutableList\$Companion")
    companion object {
        @PublishedApi
        @DartConstructor
        internal external fun <T> filled(length: Int, fill: T, growable: Boolean = false): MutableList<T>

        @PublishedApi
        @DartConstructor
        internal external fun <T> empty(growable: Boolean = false): MutableList<T>

        @PublishedApi
        @DartConstructor
        internal external fun <T> generate(
            length: Int,
            generator: (Int) -> T,
            growable: Boolean = true,
        ): MutableList<T>
    }
}

// TODO: Move to internal
// Dart List factory constructor and static method declarations. Can be copied over to the relevant companion objects.
internal external object ListFactoriesAndStatics {
    @PublishedApi
    // @DartConstructor
    internal external fun <T> filled(length: Int, fill: T, growable: Boolean = false): List<T>

    @PublishedApi
    // @DartConstructor
    internal external fun <T> empty(growable: Boolean = false): List<T>

    @PublishedApi
    // @DartConstructor
    internal external fun <T> from(elements: Iterable<*>, growable: Boolean = true): List<T>

    @PublishedApi
    // @DartConstructor
    internal external fun <T> of(elements: Iterable<T>, growable: Boolean = true): List<T>

    @PublishedApi
    // @DartConstructor
    internal external fun <T> generate(length: Int, generator: (Int) -> T, growable: Boolean = true): List<T>

    @PublishedApi
    // @DartConstructor
    internal external fun <T> unmodifiable(elements: Iterable<*>): List<T>

    @PublishedApi
    internal external fun <S, T> castFrom(source: MutableList<S>): List<T>

    @PublishedApi
    internal external fun <T> copyRange(
        target: MutableList<T>,
        at: Int,
        source: MutableList<T>,
        start: Int? = null,
        end: Int? = null
    ): Unit

    internal external fun <T> writeIterable(target: MutableList<T>, at: Int, source: Iterable<T>): Unit
}