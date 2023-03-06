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

package dotlin.intrinsics

// TODO: @DartPublic

/**
 * Type used as the lower bound of the flexible type `(Immutable|Mutable|FixedSize)List<E>`.
 */
@DartName("List")
@DartLibrary("dart:core")
external /*internal*/ interface AnyList<E> : ImmutableList<E>, FixedSizeList<E>, MutableList<E> {
    // Must be overridden to override return types.
    override fun <R> cast(): AnyList<R>
    override fun subList(start: Int, end: Int?): AnyList<E>
}

/**
 * Type used as the lower bound of the flexible type `(Immutable|Mutable)Set<E>`.
 */
@DartName("Set")
@DartLibrary("dart:core")
external /*internal*/ interface AnySet<E> : ImmutableSet<E>, MutableSet<E> {
    // Must be overridden to override return types.
    override fun <R> cast(): AnySet<R>
}

/**
 * Type used as the lower bound of the flexible type `(Immutable|Mutable)Map<E>`.
 */
@DartName("Map")
@DartLibrary("dart:core")
external /*internal*/ interface AnyMap<K, V> : ImmutableMap<K, V>, MutableMap<K, V> {
    // Must be overridden to override return types.
    override fun <RK, RV> cast(): AnyMap<RK, RV>
}

// TODO: alias to `runtime.Collection<E>`.
// TODO: @DartPublic
/**
 * This typealias represents the following type in Dart: `Collection<T> | List<T> | Set<T>`.
 *
 * Because [Collection] does not exist in Dart, but Dart `List`s and `Set`s are `Collection`s, we have to replace
 * [Collection] types for variables, parameters, etc. to the more lenient type as mentioned. However, that type is
 * not denotable in either language, so we end up with `dynamic`.
 */
/*internal*/ typealias AnyCollection<E> = Dynamic