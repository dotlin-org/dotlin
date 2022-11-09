/*
 * Copyright 2022 Wilko Manger
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

//@DartName("Iterable")
//@DartLibrary("dart:core", aliased = true)
/*external abstract class Sequence<E> const constructor() {
    companion object {
        @DartConstructor
        external fun <E> generate(count: Int, generator: ((index: Int) -> E)?): Sequence<E>

        @DartConstructor
        external const fun <E> empty(): Sequence<E>

        external fun <S, T> castFrom(source: Sequence<S>): Sequence<T>
    }

    @DartGetter
    abstract operator fun iterator(): Iterator<E>

    open fun <R> cast(): Sequence<R>

    @DartName("followedBy")
    open /*TODO operator*/ fun plus(@DartName("other") elements: Sequence<E>): Sequence<E>

    // TODO: Rename T to R?
    open fun <T> map(@DartName("toElement") transform: (/*@DartName("e")*/ element: E) -> T): Sequence<T>

    @DartName("where")
    open fun filter(@DartName("test") predicate: (element: E) -> Boolean): Sequence<E>

    @DartName("whereType")
    open fun <T> filterIsInstance(): Sequence<T>

    @DartName("expand")
    open fun <T> flatMap(@DartName("toElements") transform: (element: E) -> Sequence<T>): Sequence<T>

    open /*TODO operator*/ fun contains(element: Any?): Boolean

    open fun forEach(action: (element: E) -> Unit): Unit

    open fun reduce(@DartName("combine") operator: (/*@DartName("value")*/ acc: E, element: E) -> E): E

    // TODO: Rename T to R?
    open fun <T> fold(
        @DartName("initialValue") initial: T,
        @DartName("combine") operation: (/*@DartName("previousValue*/ acc: T, element: E) -> T
    ): T

    @DartName("every")
    open fun all(@DartName("test") predicate: (element: E) -> Boolean): Boolean

    @DartName("join")
    @DartPositional
    open fun joinToString(@DartDifferentDefaultValue seperator: String = ", "): String

    open fun any(@DartName("test") predicate: (element: E) -> Boolean): Boolean

    @DartName("toList")
    open fun toArray(growable: Boolean = true): Array<E>

    @DartName("toSet")
    open fun toUniqueArray(): UniqueArray<E>

    @DartGetter
    @DartName("length")
    open fun count(): Int

    @DartGetter
    open fun isEmpty(): Boolean

    @DartGetter
    open fun isNotEmpty(): Boolean

    open fun take(@DartName("count") n: Int): Sequence<E>

    open fun takeWhile(@DartName("test") predicate: (value: E) -> Boolean): Sequence<E>

    @DartName("skip")
    open fun drop(@DartName("count") n: Int): Sequence<E>

    @DartName("skipWhile")
    open fun dropWhile(@DartName("test") predicate: (value: E) -> Boolean): Boolean

    @DartGetter
    open fun first(): E

    @DartGetter
    open fun last(): E

    @DartGetter
    open fun single(): E

    @DartName("firstWhere")
    open fun first(orElse: (() -> E)? = null, @DartIndex(0) @DartName("test") predicate: (element: E) -> Boolean): E

    @DartName("lastWhere")
    open fun last(orElse: (() -> E)? = null, @DartIndex(0) @DartName("test") predicate: (element: E) -> Boolean): E

    @DartName("singleWhere")
    open fun single(orElse: (() -> E)? = null, @DartIndex(0) @DartName("test") predicate: (element: E) -> Boolean): E

    open fun elementAt(index: Int): E

    override fun toString(): String
}
*/