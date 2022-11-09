/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package kotlin

/**
 * Returns an empty array of the specified type T.
 *
 * Arrays are writeable and have a fixed size.
 *
 * @see Array
 * @see WriteableList
 */
external fun <T> arrayOf(): Array<T>

/**
 * Returns an array containing the specified elements.
 *
 * Arrays are writeable and have a fixed size.
 *
 * @see Array
 * @see WriteableList
 */
external fun <T> arrayOf(vararg elements: T): Array<T>

/**
 * Returns an array of objects of the given type with the given size, initialized with null values.
 *
 * Arrays are writeable and have a fixed size.
 *
 * @see Array
 * @see WriteableList
 */
fun <T> arrayOfNulls(size: Int): Array<T?> = Array.filled<T?>(size, fill = null)

/**
 * Returns an empty array of the specified type T.
 *
 * Arrays are writeable and have a fixed size.
 *
 * @see Array
 * @see WriteableList
 */
external fun <T> emptyArray(): Array<T>

/**
 * Creates a new array of the specified [size], where each element is calculated by calling the specified
 * [init] function.
 *
 * The function [init] is called for each array element sequentially starting from the first one.
 * It should return the value for an array element given its index.
 *
 * Arrays are writeable and have a fixed size.
 */
inline fun <T> Array(size: Int, init: (index: Int) -> T): Array<T> =
    Array.generate<T>(size, init, growable = false)