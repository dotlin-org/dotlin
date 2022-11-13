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

import dotlin.intrinsics.*

/**
 * Classes that inherit from this interface can be represented as a sequence of elements that can
 * be iterated over.
 * @param T the type of element being iterated over. The iterator is covariant in its element type.
 */
@DartLibrary("dart:core")
external abstract class Iterable<out E> const constructor() {
    /* @DartInheritance */
    constructor(implement: Interface)

    @DartGetter
    abstract operator fun iterator(): Iterator<E>

    open fun <R> cast(): Iterable<R>

    @DartName("followedBy")
    /*TODO operator*/ open fun plus(@DartName("other") elements: Iterable<@UnsafeVariance E>): Iterable<E>

    // TODO: Rename T to R?
    open fun <T> map(@DartName("toElement") transform: (/*@DartName("e")*/ element: E) -> T): Iterable<T>

    @DartName("where")
    open fun filter(@DartName("test") predicate: (element: E) -> Boolean): Iterable<E>

    @DartName("whereType")
    open fun <T> filterIsInstance(): Iterable<T>

    @DartName("expand")
    open fun <T> flatMap(@DartName("toElements") transform: (element: E) -> Iterable<T>): Iterable<T>

    /*TODO operator*/ open fun contains(element: Any?): Boolean

    open fun forEach(action: (element: E) -> Unit): Unit

    open fun reduce(@DartName("combine") operator: (/*@DartName("value")*/ acc: E, element: E) -> @UnsafeVariance E): E

    // TODO: Rename T to R?
    open fun <T> fold(
        @DartName("initialValue") initial: T,
        @DartName("combine") operation: (/*@DartName("previousValue*/ acc: T, element: E) -> T
    ): T

    @DartName("every")
    open fun all(@DartName("test") predicate: (element: E) -> Boolean): Boolean

    @DartName("join")
    @DartPositional
    open fun joinToString(@DartDifferentDefaultValue separator: String = ", "): String

    open fun any(@DartName("test") predicate: (element: E) -> Boolean): Boolean

    // TODO: Split up `growable: false` and `growable: true`
    open fun toList(growable: Boolean = true): Flex<AnyList<E>, List<E>>

    open fun toSet(): Flex<AnySet<E>, Set<E>>

    //@DartGetter
    //open fun count(): Int
    // TODO: MERGE: Should be called "count" and a function. It must then merge with "size" in Collection.
    @DartName("length")
    open val size: Int

    @DartGetter
    open fun isEmpty(): Boolean

    @DartGetter
    open fun isNotEmpty(): Boolean

    open fun take(@DartName("count") n: Int): Iterable<E>

    open fun takeWhile(@DartName("test") predicate: (value: E) -> Boolean): Iterable<E>

    @DartName("skip")
    open fun drop(@DartName("count") n: Int): Iterable<E>

    @DartName("skipWhile")
    open fun dropWhile(@DartName("test") predicate: (value: E) -> Boolean): Iterable<E>

    //@DartGetter
    // TODO: MERGE: Should be a function. It must then merge with var "first" in WriteableList
    // or the var must be generated.
    //open fun first(): E

    open val first: E

    //@DartGetter
    // TODO: MERGE: Should be a function. It must then merge with var "last" in WriteableList
    // or the var must be generated.
    //open fun last(): E

    open val last: E

    @DartGetter
    open fun single(): E

    @DartName("firstWhere")
    open fun first(
        orElse: (() -> @UnsafeVariance E)? = null,
        @DartIndex(0) @DartName("test") predicate: (element: E) -> Boolean
    ): E

    @DartName("lastWhere")
    open fun last(
        orElse: (() -> @UnsafeVariance E)? = null,
        @DartIndex(0) @DartName("test") predicate: (element: E) -> Boolean
    ): E

    @DartName("singleWhere")
    open fun single(
        orElse: (() -> @UnsafeVariance E)? = null,
        @DartIndex(0) @DartName("test") predicate: (element: E) -> Boolean
    ): E

    open fun elementAt(index: Int): E

    override fun toString(): String
}


/**
 * Classes that inherit from this interface can be represented as a sequence of elements that can
 * be iterated over and that supports removing elements during iteration.
 * @param T the type of element being iterated over. The mutable iterator is invariant in its element type.
 */
interface MutableIterable<out T> : Iterable<T>(Interface) {
    /**
     * Returns an iterator over the elements of this sequence that supports removing elements during iteration.
     */
    override fun iterator(): MutableIterator<T>
}