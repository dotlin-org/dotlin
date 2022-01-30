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

package dart.core

/**
 * An indexable collection of objects with a size.
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
external interface IArray<E> : Iterable<E> {
    /**
     * The object at the given [index] in the array.
     *
     * The [index] must be a valid index of this array,
     * which means that `index` must be non-negative and
     * less than [size].
     */
    operator fun get(index: Int): E

    /**
     * Sets the value at the given [index] in the array to [value].
     *
     * The [index] must be a valid index of this array,
     * which means that `index` must be non-negative and
     * less than [size].
     */
    operator fun set(index: Int, value: E): E

    /**
     * The number of objects in this array.
     *
     * The valid indices for an array are `0` through `size - 1`.
     */
    @DartName("length")
    val size: Int

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
    @Suppress("WRONG_DEFAULT_VALUE_FOR_EXTERNAL_FUN_PARAMETER") // TODO: Fix in analyzer?
    @DartPositional
    @DartName("sublist")
    fun subArray(start: Int, end: Int? = null): IArray<E>
}