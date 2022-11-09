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

@file:Suppress(
    "ABSTRACT_MEMBER_NOT_IMPLEMENTED" // TODO: Fix in analyzer
)

package dart.typed_data

/**
 * A fixed-length, random-access sequence of bytes that also provides random
 * and unaligned access to the fixed-width integers and floating point
 * numbers represented by those bytes.
 *
 * `ByteData` may be used to pack and unpack data from external sources
 * (such as networks or files systems), and to process large quantities
 * of numerical data more efficiently than would be possible
 * with ordinary [Array] implementations.
 * `ByteData` can save space, by eliminating the need for object headers,
 * and time, by eliminating the need for data copies.
 *
 * If data comes in as bytes, they can be converted to `ByteData` by
 * sharing the same buffer.
 * ```kotlin
 * val bytes: Uint8Array = ...;
 * val blob = ByteData.sublistView(bytes);
 * if (blob.getUint32(0, Endian.little) == 0x04034b50) { // Zip file marker
 * ...
 * }
 *
 * ```
 *
 * Finally, `ByteData` may be used to intentionally reinterpret the bytes
 * representing one arithmetic type as another.
 * For example this code fragment determine what 32-bit signed integer
 * is represented by the bytes of a 32-bit floating point number
 * (both stored as big endian):
 * ```kotlin
 * val bdata = ByteData(8);
 * bdata.setFloat32(0, 3.04);
 * val huh: Int = bdata.getInt32(0); // 0x40428f5c
 * ```
 */
external class ByteData(length: Int) : TypedData {
    /**
     * Returns the (possibly negative) integer represented by the eight bytes at
     * the specified [byteOffset] in this object, in two's complement binary
     * form.
     *
     * The return value will be between -2<sup>63</sup> and 2<sup>63</sup> - 1,
     * inclusive.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 8` must be less than or equal to the length of this object.
     */
    @DartPositional
    fun getInt64(byteOffset: Int, endian: Endian = Endian.BIG): Int

    /**
     * Sets the eight bytes starting at the specified [byteOffset] in this
     * object to the two's complement binary representation of the specified
     * [value], which must fit in eight bytes.
     *
     * In other words, [value] must lie
     * between -2<sup>63</sup> and 2<sup>63</sup> - 1, inclusive.
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 8` must be less than or equal to the length of this object.
     */
    @DartPositional
    fun setInt64(byteOffset: Int, value: Int, endian: Endian = Endian.BIG)

    /**
     * Returns the floating point number represented by the eight bytes at
     * the specified [byteOffset] in this object, in IEEE 754
     * double-precision binary floating-point format (binary64).
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 8` must be less than or equal to the length of this object.
     */
    @DartPositional
    fun getFloat64(byteOffset: Int, endian: Endian = Endian.BIG): Double

    /**
     * Sets the eight bytes starting at the specified [byteOffset] in this
     * object to the IEEE 754 double-precision binary floating-point
     * (binary64) representation of the specified [value].
     *
     * The [byteOffset] must be non-negative, and
     * `byteOffset + 8` must be less than or equal to the length of this object.
     */
    @DartPositional
    fun setFloat64(byteOffset: Int, value: Double, endian: Endian = Endian.BIG)
}