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

@file:Suppress(
    "NON_ABSTRACT_FUNCTION_WITH_NO_BODY",
    "MUST_BE_INITIALIZED_OR_BE_ABSTRACT",
    "UNUSED_PARAMETER"
)

package kotlin

/**
 * Represents an array (specifically, a growable Dart `List`),
 * an indexable collection of objects with a size.
 *
 * Array instances can be created using the [arrayOf], [arrayOfNulls] and [emptyArray]
 * standard library functions.
 *
 * Subclasses of this class implement different kinds of arrays.
 * The most common kinds of arrays are:
 *
 *  * Fixed-size array.
 * An error occurs when attempting to use operations
 * that can change the size of the array.
 *
 *  * Growable array. Full implementation of the API defined in this class.
 *
 * The default growable array, keeps an internal buffer, and grows that buffer
 * when necessary. This guarantees that a sequence of [add] operations will each
 * execute in amortized constant time. Setting the size directly may take time
 * proportional to the new size, and may change the internal capacity so that a
 * following add operation will need to immediately increase the buffer capacity.
 * Other array implementations may have different performance behavior.
 *
 * Arrays are [Iterable]. Iteration occurs over values in index order. Changing
 * the values does not affect iteration, but changing the valid
 * indices&mdash;that is, changing the array's size&mdash;between iteration
 * steps causes a [ConcurrentModificationError]. This means that only growable
 * arrays can throw ConcurrentModificationError. If the size changes
 * temporarily and is restored before continuing the iteration, the iterator
 * might not detect it.
 *
 * It is generally not allowed to modify the array's size (adding or removing
 * elements) while an operation on the array is being performed,
 * for example during a call to [forEach] or [sort].
 * Changing the array's size while it is being iterated, either by iterating it
 * directly or through iterating an [Iterable] that is backed by the array, will
 * break the iteration.
 */
@DartLibrary("dart:core", aliased = true)
@DartName("List")
external open class Array<T> {
    constructor(useAs: InterfaceOrMixin)

    /**
     * Creates a new array with the specified [size], where each element is calculated by calling the specified
     * [init] function.
     *
     * The function [init] is called for each array element sequentially starting from the first one.
     * It should return the value for an array element given its index.
     */
    @DartName("generate")
    constructor(size: Int, init: (Int) -> T)

    /**
     * Returns the array element at the specified [index]. This method can be called using the
     * index operator.
     * ```
     * value = arr[index]
     * ```
     *
     * If the [index] is out of bounds of this array, throws an [IndexOutOfBoundsException] except in Kotlin/JS
     * where the behavior is unspecified.
     */
    open operator fun get(index: Int): T

    /**
     * Sets the array element at the specified [index] to the specified [value]. This method can
     * be called using the index operator.
     * ```
     * arr[index] = value
     * ```
     *
     * If the [index] is out of bounds of this array, throws an [IndexOutOfBoundsException] except in Kotlin/JS
     * where the behavior is unspecified.
     */
    open operator fun set(index: Int, value: T): T

    /**
     * Returns the number of elements in the array.
     */
    open val size: Int

    /**
     * Creates an [Iterator] for iterating over the elements of the array.
     */
    @DartExtension
    /*inline*/ operator fun iterator(): kotlin.collections.Iterator<T>

    /**
     * Returns a new array containing the elements between [start] and [end].
     *
     * The new array is an `Array<E>` containing the elements of this array at
     * positions greater than or equal to [start] and less than [end] in the same
     * order as they occur in this array.
     *
     *
     * ```kotlin
     * val colors = arrayOf("red", "green", "blue", "orange", "pink")
     * println(colors.subarray(1, 3)); // [green, blue]
     * ```
     *
     * If [end] is omitted, it defaults to the [size] of this array.
     *
     * ```kotlin
     * println(colors.subarray(1)); // [green, blue, orange, pink]
     * ```
     *
     * The `start` and `end` positions must satisfy the relations
     * 0 ≤ `start` ≤ `end` ≤ [size]
     * If `end` is equal to `start`, then the returned array is empty.
     */
    @DartPositional
    @DartName("sublist")
    open fun subArray(start: Int, end: Int? = definedExternally): Array<T>
}
