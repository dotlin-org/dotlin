/*
 * Copyright 2010-2020 JetBrains s.r.o.
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

import kotlin.collections.Iterator as KtIterator

import kotlin.internal.PlatformDependent

/**
 * A generic unordered collection of elements that does not support duplicate elements.
 *
 * That is, for each object of the element type, the object is either considered
 * to be in the set, or to _not_ be in the set.
 *
 * Methods in this interface support only read-only access to the set;
 * read/write access is supported through the [MutableSet] interface.
 *
 * Set implementations may consider some elements indistinguishable. These
 * elements are treated as being the same for any operation on the set.
 *
 * The default [Set] implementation, [LinkedHashSet], considers objects
 * indistinguishable if they are equal with regard to [Any.==] and
 * [Any.hashCode].
 *
 * Iterating over elements of a set may be either unordered
 * or ordered in some way. Examples:
 *
 *  * A [HashSet] is unordered, which means that its iteration order is
 * unspecified,
 *  * [LinkedHashSet] iterates in the insertion order of its elements, and
 *  * a sorted set like [SplayTreeSet] iterates the elements in sorted order.
 *
 * @param E the type of elements contained in the set. The set is covariant in its element type.
 */
@DartLibrary("dart:core")
external interface Set<out E> : Collection<E> {
    override fun <R> cast(): Set<R>

    fun lookup(@DartName("object") element: Any?): E?

    fun containsAll(other: Iterable<Any?>): Boolean

    @DartName("intersection")
    fun intersect(other: Set<Any?>): Set<E>

    fun union(other: Set<@UnsafeVariance E>): Set<E>

    // TODO?: Map Any? to E
    @DartName("difference")
    fun subtract(other: Set<Any?>): Set<E>
}

/**
 * A generic immutable unordered collection of elements that does not support duplicate elements.
 * Methods in this interface support only read-only access to the immutable set.
 *
 * Implementors of this interface take responsibility to be immutable.
 * Once constructed they must contain the same elements in the same order.
 *
 * @param E the type of elements contained in the set. The set is covariant on its element type.
 */
@DartName("Set")
@DartLibrary("dart:core")
external interface ImmutableSet<out E> : Set<E>, ImmutableCollection<E> {
    // TODO: Wrap return types
}

/**
 * A generic unordered collection of elements that does not support duplicate elements, and supports
 * adding and removing elements.
 *
 * It is generally not allowed to modify the set (add or remove elements) while
 * an operation on the set is being performed, for example during a call to
 * [forEach] or [containsAll]. Nor is it allowed to modify the set while
 * iterating either the set itself or any [Iterable] that is backed by the set,
 * such as the ones returned by methods like [filter] and [map].
 *
 * It is generally not allowed to modify the equality of elements (and thus not
 * their hashcode) while they are in the set. Some specialized subtypes may be
 * more permissive, in which case they should document this behavior.
 *
 * @param E the type of elements contained in the set. The mutable set is invariant in its element type.
 */
@DartName("Set")
@DartLibrary("dart:core")
external interface MutableSet<E> : Set<E>, MutableCollection<E> {
    override fun <R> cast(): MutableSet<R>

    @Suppress("RETURN_TYPE_MISMATCH_ON_OVERRIDE")
    override fun add(@DartName("value") element: E): Boolean

    override fun intersect(other: Set<Any?>): MutableSet<E>

    override fun union(other: Set<E>): MutableSet<E>

    override fun subtract(other: Set<Any?>): MutableSet<E>

    fun removeAll(other: Iterable<Any?>): Unit
    fun retainAll(other: Iterable<Any?>): Unit
}