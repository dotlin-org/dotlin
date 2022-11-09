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

/**
 * Returns a new immutable set with the given elements. Elements of the set are
 * iterated in the order they were specified.
 */
external const inline fun <T> setOf(vararg elements: T): ImmutableSet<T>

/**
 * Returns an empty immutable set.
 */
external const inline fun <T> emptySet(): ImmutableSet<T>

/**
 * Returns a new [MutableSet] with the given elements. Elements of the set are
 * iterated in the order they were specified.
 */
external inline fun <T> mutableSetOf(vararg elements: T): MutableSet<T>