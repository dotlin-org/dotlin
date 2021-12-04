/*
 * Copyright 2010-2019 JetBrains s.r.o.
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

@file:Suppress("NON_ABSTRACT_FUNCTION_WITH_NO_BODY", "MUST_BE_INITIALIZED_OR_BE_ABSTRACT", "UNUSED_PARAMETER")

package kotlin

/**
 * Represents an array (specifically, a Dart `List`).
 * Array instances can be created using the [arrayOf], [arrayOfNulls] and [emptyArray]
 * standard library functions.
 * See [Kotlin language documentation](https://kotlinlang.org/docs/reference/basic-types.html#arrays)
 * for more information on arrays.
 */
public class Array<T> {
    /**
     * Creates a new array with the specified [size], where each element is calculated by calling the specified
     * [init] function.
     *
     * The function [init] is called for each array element sequentially starting from the first one.
     * It should return the value for an array element given its index.
     */
    public constructor(size: Int, init: (Int) -> T)

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
    public operator fun get(index: Int): T

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
    public operator fun set(index: Int, value: T): Unit

    /**
     * Returns the number of elements in the array.
     */
    public val size: Int

    /**
     * Creates an [Iterator] for iterating over the elements of the array.
     */
    public operator fun iterator(): Iterator<T>
}
