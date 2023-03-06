/*
 * Copyright 2023 Wilko Manger
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

@file:DartExtensionName("IterableExt")

package dotlin

inline fun <T> Iterable<T>.toMutableList(): MutableList<T> = toList(growable = true) as MutableList<T>
inline fun <T> Iterable<T>.toArray(): Array<T> = toList(growable = false) as Array<T>
inline fun <T> Iterable<T>.toFixedSizeList(): FixedSizeList<T> = toArray()