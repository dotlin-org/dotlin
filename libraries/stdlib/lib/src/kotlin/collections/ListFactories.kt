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

package kotlin.collections

import dart.collection.ImmutableListView

/**
 * Returns a new immutable list of given elements.
 */
external const inline fun <T> listOf(vararg elements: T): ImmutableList<T>

/**
 * Returns an empty immutable list.
 */
external const inline fun <T> emptyList(): ImmutableList<T>

/**
 * Returns a new [MutableList] with the given elements.
 */
external inline fun <T> mutableListOf(vararg elements: T): MutableList<T>

/**
 * Returns a new [MutableList], initialized with the given amount of null values.
 *
 * The list is modifiable and growable.
 */
inline fun <T> mutableListOfNulls(size: Int): MutableList<T?> = MutableList.filled<T?>(size, fill = null)

/**
 * Creates a new immutable list with the specified [size], where each element is calculated by calling the specified
 * [init] function.
 *
 * The function [init] is called for each list element sequentially starting from the first one.
 * It should return the value for a list element given its index.
 *
 * @sample samples.collections.Collections.Lists.readOnlyListFromInitializer
 */
@SinceKotlin("1.1")
inline fun <T> List(size: Int, init: (index: Int) -> T): ImmutableListView<T> =
    ImmutableListView<T>(MutableList(size, init))

/**
 * Creates a new mutable list with the specified [size], where each element is calculated by calling the specified
 * [init] function.
 *
 * The function [init] is called for each list element sequentially starting from the first one.
 * It should return the value for a list element given its index.
 *
 * @sample samples.collections.Collections.Lists.mutableListFromInitializer
 */
@SinceKotlin("1.1")
inline fun <T> MutableList(size: Int, init: (index: Int) -> T): MutableList<T> =
    MutableList.generate<T>(size, init, growable = true)