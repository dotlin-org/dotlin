/*
 * Copyright 2010-2021 JetBrains s.r.o.
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

@file:Suppress(
    "NON_ABSTRACT_FUNCTION_WITH_NO_BODY",
    "MUST_BE_INITIALIZED_OR_BE_ABSTRACT",
    "UNUSED_PARAMETER",
    "DIVISION_BY_ZERO",
    "WRONG_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
    "WRONG_INITIALIZER_OF_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
    "EXTERNAL_DELEGATED_CONSTRUCTOR_CALL", // TODO: Fix in analyzer
    "WRONG_BODY_OF_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
    "INAPPLICABLE_OPERATOR_MODIFIER"
)

package kotlin

external internal abstract class Byte private constructor() : Number(), Comparable<Byte>

external internal abstract class Short private constructor() : Number(), Comparable<Short>

// Only has its members so they can be called on Long literals.
external internal class Long private constructor() : Number(), Comparable<Long> {
    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    operator fun compareTo(other: Int): Int

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    override operator fun compareTo(other: Long): Int

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    operator fun compareTo(other: Double): Int

    /** Adds the other value to this value. */
    operator fun plus(other: Int): Int
    /** Adds the other value to this value. */
    operator fun plus(other: Long): Int
    /** Adds the other value to this value. */
    operator fun plus(other: Double): Double

    /** Subtracts the other value from this value. */
    operator fun minus(other: Int): Int
    /** Subtracts the other value from this value. */
    operator fun minus(other: Long): Int
    /** Subtracts the other value from this value. */
    operator fun minus(other: Double): Double

    /** Multiplies this value by the other value. */
    operator fun times(other: Int): Int
    /** Multiplies this value by the other value. */
    operator fun times(other: Long): Int
    /** Multiplies this value by the other value. */
    operator fun times(other: Double): Double

    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    operator fun div(other: Int): Int
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    operator fun div(other: Long): Int
    /** Divides this value by the other value. */
    operator fun div(other: Double): Double

    /**
     * Calculates the remainder of truncating division of this value by the other value.
     * 
     * The result is either zero or has the same sign as the _dividend_ and has the absolute value less than the absolute value of the divisor.
     */
    @SinceKotlin("1.1")
    operator fun rem(other: Int): Int
    /**
     * Calculates the remainder of truncating division of this value by the other value.
     * 
     * The result is either zero or has the same sign as the _dividend_ and has the absolute value less than the absolute value of the divisor.
     */
    @SinceKotlin("1.1")
    operator fun rem(other: Long): Int
    /**
     * Calculates the remainder of truncating division of this value by the other value.
     * 
     * The result is either zero or has the same sign as the _dividend_ and has the absolute value less than the absolute value of the divisor.
     */
    @SinceKotlin("1.1")
    operator fun rem(other: Double): Double

    /**
     * Returns this value incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    operator fun inc(): Int

    /**
     * Returns this value decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    operator fun dec(): Int

    /** Returns this value. */
    operator fun unaryPlus(): Int
    /** Returns the negative of this value. */
    operator fun unaryMinus(): Int

     /** Creates a range from this value to the specified [other] value. */
    operator fun rangeTo(other: Int): LongRange
     /** Creates a range from this value to the specified [other] value. */
    operator fun rangeTo(other: Long): LongRange

    /**
     * Shifts this value left by the [bitCount] number of bits.
     *
     * Note that only the six lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..63`.
     */
    infix fun shl(bitCount: Int): Int

    /**
     * Shifts this value right by the [bitCount] number of bits, filling the leftmost bits with copies of the sign bit.
     *
     * Note that only the six lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..63`.
     */
    infix fun shr(bitCount: Int): Int

    /**
     * Shifts this value right by the [bitCount] number of bits, filling the leftmost bits with zeros.
     *
     * Note that only the six lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..63`.
     */
    infix fun ushr(bitCount: Int): Int

    /** Performs a bitwise AND operation between the two values. */
    infix fun and(other: Long): Int
    /** Performs a bitwise OR operation between the two values. */
    infix fun or(other: Long): Int
    /** Performs a bitwise XOR operation between the two values. */
    infix fun xor(other: Long): Int
    /** Inverts the bits in this value. */
    fun inv(): Long

    /**
     * Converts this [Long] value to [Int].
     *
     * If this value is in [Int.MIN_VALUE]..[Int.MAX_VALUE], the resulting `Int` value represents
     * the same numerical value as this `Long`.
     *
     * The resulting `Int` value is represented by the least significant 32 bits of this `Long` value.
     */
    override fun toInt(): Int

    /**
     * Converts this [Long] value to [Double].
     *
     * The resulting value is the closest `Double` to this `Long` value.
     * In case when this `Long` value is exactly between two `Double`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    override fun toDouble(): Double
}

external internal abstract class Float private constructor() : Number(), Comparable<Float>