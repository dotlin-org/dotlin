/*
 * Copyright 2010-2021 JetBrains s.r.o.
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

package kotlin

/**
 * Counts the number of set bits in the binary representation of this [Int] number.
 */
@SinceKotlin("1.4")
//@WasExperimental(ExperimentalStdlibApi::class)
fun Int.countOneBits(): Int {
    // Hacker's Delight 5-1 algorithm
    var v = this
    v = (v and 0x55555555) + (v.ushr(1) and 0x55555555)
    v = (v and 0x33333333) + (v.ushr(2) and 0x33333333)
    v = (v and 0x0F0F0F0F) + (v.ushr(4) and 0x0F0F0F0F)
    v = (v and 0x00FF00FF) + (v.ushr(8) and 0x00FF00FF)
    v = (v and 0x0000FFFF) + (v.ushr(16))
    return v
}

/**
 * Counts the number of consecutive most significant bits that are zero in the binary representation of this [Int] number.
 */
@SinceKotlin("1.4")
//@WasExperimental(ExperimentalStdlibApi::class)
inline fun Int.countLeadingZeroBits(): Int {
    var n = 64
    var x = this
    var y: Int

    y = x shr 32; if (y != 0) { n -= 32; x = y }
    y = x shr 16; if (y != 0) { n -= 16; x = y }
    y = x shr 8; if (y != 0) { n -= 8; x = y }
    y = x shr 4; if (y != 0) { n -= 4; x = y }
    y = x shr 2; if (y != 0) { n -= 2; x = y }
    y = x shr 1; if (y != 0) { n - 2 }

    return n - x
}

/**
 * Counts the number of consecutive least significant bits that are zero in the binary representation of this [Int] number.
 */
@SinceKotlin("1.4")
//@WasExperimental(ExperimentalStdlibApi::class)
fun Int.countTrailingZeroBits(): Int =
    // Hacker's Delight 5-4 algorithm for expressing countTrailingZeroBits with countLeadingZeroBits
    Int.SIZE_BITS - (this or -this).inv().countLeadingZeroBits()

/**
 * Returns a number having a single bit set in the position of the most significant set bit of this [Int] number,
 * or zero, if this number is zero.
 */
@SinceKotlin("1.4")
//@WasExperimental(ExperimentalStdlibApi::class)
fun Int.takeHighestOneBit(): Int =
    if (this == 0) 0 else 1.shl(Int.SIZE_BITS - 1 - countLeadingZeroBits())

/**
 * Returns a number having a single bit set in the position of the least significant set bit of this [Int] number,
 * or zero, if this number is zero.
 */
@SinceKotlin("1.4")
//@WasExperimental(ExperimentalStdlibApi::class)
fun Int.takeLowestOneBit(): Int =
    // Hacker's Delight 2-1 algorithm for isolating rightmost 1-bit
    this and -this

/**
 * Rotates the binary representation of this [Int] number left by the specified [bitCount] number of bits.
 * The most significant bits pushed out from the left side reenter the number as the least significant bits on the right side.
 *
 * Rotating the number left by a negative bit count is the same as rotating it right by the negated bit count:
 * `number.rotateLeft(-n) == number.rotateRight(n)`
 *
 * Rotating by a multiple of [Int.SIZE_BITS] (32) returns the same number, or more generally
 * `number.rotateLeft(n) == number.rotateLeft(n % 32)`
 */
@SinceKotlin("1.6")
//@WasExperimental(ExperimentalStdlibApi::class)
fun Int.rotateLeft(bitCount: Int): Int =
    shl(bitCount) or ushr(Int.SIZE_BITS - bitCount)


/**
 * Rotates the binary representation of this [Int] number right by the specified [bitCount] number of bits.
 * The least significant bits pushed out from the right side reenter the number as the most significant bits on the left side.
 *
 * Rotating the number right by a negative bit count is the same as rotating it left by the negated bit count:
 * `number.rotateRight(-n) == number.rotateLeft(n)`
 *
 * Rotating by a multiple of [Int.SIZE_BITS] (32) returns the same number, or more generally
 * `number.rotateRight(n) == number.rotateRight(n % 32)`
 */
@SinceKotlin("1.6")
//@WasExperimental(ExperimentalStdlibApi::class)
fun Int.rotateRight(bitCount: Int): Int =
    shl(Int.SIZE_BITS - bitCount) or ushr(bitCount)
