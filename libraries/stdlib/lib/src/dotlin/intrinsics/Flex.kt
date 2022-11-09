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

// TODO: Error if used in non-external declaration
// TODO: Error if L or U are Flex.
// TODO: Error if typealiased
/**
 * Represents a flexible type, with lower bound [L] and upper bound [U].
 *
 * This is used to make the types such as `(Immutable|Mutable)Set<E>` denotable.
 *
 * Only allowed to be used in `external` declarations.
 *
 * Cannot be typealiased.
 *
 * Should never be nullable. If you want a nullable type, make [L] and [U] nullable.
 *
 * @param [L] Lower bound. Cannot be a flexible type ([Flex]).
 * @param [U] Upper bound. Cannot be a flexible type ([Flex]).
 */
external interface Flex<L, U>

/**
 * Type representing any type of default (built-in) list kinds, wich are:
 * - Immutable lists ([ImmutableList])
 * - Fixed-size lists, a.k.a arrays ([FixedSizeList], [Array])
 * - Growable lists ([MutableList]).
 *
 * This type is only allowed to be used in external flexible types.
 *
 * @see Flex
 */
@DartName("List")
@DartLibrary("dart:core")
external interface AnyList<E> : ImmutableList<E>, FixedSizeList<E>, MutableList<E> {
    // Must be overridden to override return types.
    override fun <R> cast(): Flex<AnyList<R>, List<R>>
    override fun subList(start: Int, end: Int?): Flex<AnyList<E>, List<E>>
}

/**
 * Type representing any type of default (built-in) set kinds, wich are:
 * - Immutable set ([ImmutableSet])
 * - Mutable set ([MutableSet]).
 *
 * This type is only allowed to be used in external flexible types.
 *
 * @see Flex
 */
@DartName("Set")
@DartLibrary("dart:core")
external interface AnySet<E> : ImmutableSet<E>, MutableSet<E> {
    // Must be overridden to override return types.
    override fun <R> cast(): Flex<AnySet<R>, Set<R>>
}

/**
 * Type representing any type of default (built-in) map kinds, wich are:
 * - Immutable map ([ImmutableMap])
 * - Mutable map ([MutableMap]).
 *
 * This type is only allowed to be used in external flexible types.
 *
 * @see Flex
 */
@DartName("Map")
@DartLibrary("dart:core")
external interface AnyMap<K, V> : ImmutableMap<K, V>, MutableMap<K, V> {
    // Must be overridden to override return types.
    override fun <RK, RV> cast(): Flex<AnyMap<RK, RV>, Map<RK, RV>>
}

/**
 * Used to make type aliases to `dynamic`, since Kotlin doesn't support type aliases to `dynamic`
 * directly.
 */
// TODO: Error if used by non-stdlib (?)
// TODO: Error if used outside of typealias
external class Dynamic private constructor()

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