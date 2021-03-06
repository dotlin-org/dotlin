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

package dart.core

/**
 * An interface for getting items, one at a time, from an object.
 *
 * The for-in construct transparently uses `Iterator` to test for the end
 * of the iteration, and to get each item (or _element_).
 *
 * If the object iterated over is changed during the iteration, the
 * behavior is unspecified.
 *
 * The `Iterator` is initially positioned before the first element.
 * Before accessing the first element the iterator must thus be advanced using
 * [moveNext] to point to the first element.
 * If no element is left, then [moveNext] returns false,
 * and all further calls to [moveNext] will also return false.
 *
 * The [current] value must not be accessed before calling [moveNext]
 * or after a call to [moveNext] has returned false.
 */
@DartLibrary("dart:core", aliased = true)
internal external interface Iterator<out E> {
    /**
     * Advances the iterator to the next element of the iteration.
     *
     * Should be called before reading [current].
     * If the call to `moveNext` returns `true`,
     * then [current] will contain the next element of the iteration
     * until `moveNext` is called again.
     * If the call returns `false`, there are no further elements
     * and [current] should not be used any more.
     *
     * It is safe to call [moveNext] after it has already returned `false`,
     * but it must keep returning `false` and not have any other effect.
     *
     * A call to `moveNext` may throw for various reasons,
     * including a concurrent change to an underlying collection.
     * If that happens, the iterator may be in an inconsistent
     * state, and any further behavior of the iterator is unspecified,
     * including the effect of reading [current].
     */
    fun moveNext(): Boolean

    /**
     * The current element.
     *
     * If the iterator has not yet been moved to the first element
     * ([moveNext] has not been called yet),
     * or if the iterator has been moved past the last element of the [Iterable]
     * ([moveNext] has returned false),
     * then [current] is unspecified.
     * An [Iterator] may either throw or return an iterator specific default value
     * in that case.
     *
     * The `current` getter should keep its value until the next call to
     * [moveNext], even if an underlying collection changes.
     * After a successful call to `moveNext`, the user doesn't need to cache
     * the current value, but can keep reading it from the iterator.
     */
    val current: E
}

/**
 * An [Iterator] that allows moving backwards as well as forwards.
 */
@DartLibrary("dart:core", aliased = true)
internal external interface BidirectionalIterator<out E> : Iterator<E> {
    /**
     * Move back to the previous element.
     *
     * Returns true and updates [current] if successful. Returns false
     * and updates [current] to an implementation defined state if there is no
     * previous element
     */
    fun movePrevious(): Boolean
}