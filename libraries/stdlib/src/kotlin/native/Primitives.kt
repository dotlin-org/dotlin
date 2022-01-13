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

/**
 * Represents a 32-bit signed integer.
 *
 * In Dart, values of this type are represented as values of the primitive type `int`.
 */
@DartName("int")
external class Int private constructor() : Number(), Comparable<Int> {
    @DartName("Int\$Companion")
    companion object {
        /**
         * A constant holding the minimum value an instance of Int can have.
         */
        const val MIN_VALUE: Int = -9223372036854775807 - 1

        /**
         * A constant holding the maximum value an instance of Int can have.
         */
        const val MAX_VALUE: Int = 9223372036854775807

        /**
         * The number of bytes used to represent an instance of Int in a binary form.
         */
        @SinceKotlin("1.3")
        const val SIZE_BYTES: Int = 8

        /**
         * The number of bits used to represent an instance of Int in a binary form.
         */
        @SinceKotlin("1.3")
        const val SIZE_BITS: Int = 64
    }

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    override operator fun compareTo(other: Int): Int

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    internal operator fun compareTo(other: Long): Int

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    operator fun compareTo(other: Double): Int

    /** Adds the other value to this value. */
    operator fun plus(other: Int): Int
    /** Adds the other value to this value. */
    internal operator fun plus(other: Long): Int
    /** Adds the other value to this value. */
    operator fun plus(other: Double): Double

    /** Subtracts the other value from this value. */
    operator fun minus(other: Int): Int
    /** Subtracts the other value from this value. */
    internal operator fun minus(other: Long): Int
    /** Subtracts the other value from this value. */
    operator fun minus(other: Double): Double

    /** Multiplies this value by the other value. */
    operator fun times(other: Int): Int
    /** Multiplies this value by the other value. */
    internal operator fun times(other: Long): Int
    /** Multiplies this value by the other value. */
    operator fun times(other: Double): Double

    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    operator fun div(other: Int): Int
    /** Divides this value by the other value, truncating the result to an integer that is closer to zero. */
    internal operator fun div(other: Long): Int
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
    internal operator fun rem(other: Long): Int
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
    operator fun rangeTo(other: Int): IntRange
     /** Creates a range from this value to the specified [other] value. */
     internal operator fun rangeTo(other: Long): IntRange

    /**
     * Shifts this value left by the [bitCount] number of bits.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    infix fun shl(bitCount: Int): Int

    /**
     * Shifts this value right by the [bitCount] number of bits, filling the leftmost bits with copies of the sign bit.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    infix fun shr(bitCount: Int): Int

    /**
     * Shifts this value right by the [bitCount] number of bits, filling the leftmost bits with zeros.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    infix fun ushr(bitCount: Int): Int

    /** Performs a bitwise AND operation between the two values. */
    infix fun and(other: Int): Int
    /** Performs a bitwise OR operation between the two values. */
    infix fun or(other: Int): Int
    /** Performs a bitwise XOR operation between the two values. */
    infix fun xor(other: Int): Int
    /** Inverts the bits in this value. */
    fun inv(): Int

    /** Returns this value. */
    override fun toInt(): Int

    /**
     * Converts this [Int] value to [Double].
     *
     * The resulting `Double` value represents the same numerical value as this `Int`.
     */
    override fun toDouble(): Double
}

/**
 * Represents a double-precision 64-bit IEEE 754 floating point number.
 *
 * In Dart, values of this type are represented as values of the primitive type `double`.
 */
@DartName("double")
external class Double private constructor() : Number(), Comparable<Double> {
    @DartName("Double\$Companion")
    companion object {
        /**
         * A constant holding the smallest *positive* nonzero value of Double.
         */
        const val MIN_VALUE: Double = 4.9E-324

        /**
         * A constant holding the largest positive finite value of Double.
         */
        const val MAX_VALUE: Double = 1.7976931348623157E308

        /**
         * A constant holding the positive infinity value of Double.
         */
        const val POSITIVE_INFINITY: Double = 1.0/0.0

        /**
         * A constant holding the negative infinity value of Double.
         */
        const val NEGATIVE_INFINITY: Double = -1.0/0.0

        /**
         * A constant holding the "not a number" value of Double.
         */
        const val NaN: Double = -(0.0/0.0)

        /**
         * The number of bytes used to represent an instance of Double in a binary form.
         */
        @SinceKotlin("1.4")
        const val SIZE_BYTES: Int = 8

        /**
         * The number of bits used to represent an instance of Double in a binary form.
         */
        @SinceKotlin("1.4")
        const val SIZE_BITS: Int = 64
    }

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
    internal operator fun compareTo(other: Long): Int

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    override operator fun compareTo(other: Double): Int

    /** Adds the other value to this value. */
    operator fun plus(other: Int): Double
    /** Adds the other value to this value. */
    internal operator fun plus(other: Long): Double
    /** Adds the other value to this value. */
    operator fun plus(other: Double): Double

    /** Subtracts the other value from this value. */
    operator fun minus(other: Int): Double
    /** Subtracts the other value from this value. */
    internal operator fun minus(other: Long): Double
    /** Subtracts the other value from this value. */
    operator fun minus(other: Double): Double

    /** Multiplies this value by the other value. */
    operator fun times(other: Int): Double
    /** Multiplies this value by the other value. */
    internal operator fun times(other: Long): Double
    /** Multiplies this value by the other value. */
    operator fun times(other: Double): Double

    /** Divides this value by the other value. */
    operator fun div(other: Int): Double
    /** Divides this value by the other value. */
    internal operator fun div(other: Long): Double
    /** Divides this value by the other value. */
    operator fun div(other: Double): Double

    /**
     * Calculates the remainder of truncating division of this value by the other value.
     * 
     * The result is either zero or has the same sign as the _dividend_ and has the absolute value less than the absolute value of the divisor.
     */
    @SinceKotlin("1.1")
    operator fun rem(other: Int): Double
    /**
     * Calculates the remainder of truncating division of this value by the other value.
     * 
     * The result is either zero or has the same sign as the _dividend_ and has the absolute value less than the absolute value of the divisor.
     */
    @SinceKotlin("1.1")
    internal operator fun rem(other: Long): Double
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
    operator fun inc(): Double

    /**
     * Returns this value decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    operator fun dec(): Double

    /** Returns this value. */
    operator fun unaryPlus(): Double
    /** Returns the negative of this value. */
    operator fun unaryMinus(): Double

    /**
     * Converts this [Double] value to [Int].
     *
     * The fractional part, if any, is rounded down towards zero.
     * Returns zero if this `Double` value is `NaN`, [Int.MIN_VALUE] if it's less than `Int.MIN_VALUE`,
     * [Int.MAX_VALUE] if it's bigger than `Int.MAX_VALUE`.
     */
    // TODO: Fix documentation, won't return zero if NaN
    override fun toInt(): Int

    /** Returns this value. */
    @DartExtension
    override fun toDouble(): Double = this
}