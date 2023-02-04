/*
 * Copyright 2010-2019 JetBrains s.r.o.
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
 * A generic ordered _fixed-length_ collection of elements that supports changing elements.
 *
 * @see List
 * @see ChangeableList
 * @see MutableList
 */
@DartName("List")
@DartLibrary("dart:core")
external interface Array<E> : WriteableList<E> {
    //#region List overrides
    override fun <R> cast(): Array<R> // TODO: Make non-growable OR return MutableList<E>
    override fun subList(start: Int, end: Int?): Array<E> // TODO: Make non-growable OR return MutableList<E>
    //#endregion

    @DartName("Array\$Companion")
    companion object {
        @PublishedApi
        @DartConstructor
        internal external fun <T> filled(length: Int, fill: T, growable: Boolean = false): Array<T>

        @PublishedApi
        @DartConstructor
        internal external fun <T> generate(length: Int, generator: (Int) -> T, growable: Boolean = true): Array<T>

        // Used by lowerings.
        @PublishedApi
        @DartConstructor
        internal external fun <T> of(elements: Iterable<T>, growable: Boolean = true): Array<T>

        // Used by lowerings.
        @PublishedApi
        @DartConstructor
        internal external fun <T> empty(growable: Boolean = false): Array<T>

    }
}