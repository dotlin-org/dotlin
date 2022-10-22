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

package dart.typeddata

/**
 * A fixed-size array of 8-bit signed integers.
 * 
 * For long arrays, this implementation can be considerably
 * more space- and time-efficient than the default [Array] implementation.
 *
 * Integers stored in the array are truncated to their low eight bits,
 * interpreted as a signed 8-bit two's complement integer with values in the
 * range -128 to +127.
 */
@DartName("Int8List")
external interface Int8Array : Array<Int>(Interface), TypedData {
    /**
     * Returns a new array containing the elements between [start] and [end].
     *
     * The new array is an `Int8Array` containing the elements of this array at
     * positions greater than or equal to [start] and less than [end] in the same
     * order as they occur in this array.
     *
     * If [end] is omitted, it defaults to the [size] of this array.
     *
     * The `start` and `end` positions must satisfy the relations
     * 0 ≤ `start` ≤ `end` ≤ `this.size`
     * If `end` is equal to `start`, then the returned array is empty.
     */
    override fun subArray(start: Int, end: Int?): Int8Array

    companion object {
        // TODO: const
        external val bytesPerElement: Int = definedExternally
    }
}