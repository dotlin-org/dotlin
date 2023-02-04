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
    "INAPPLICABLE_OPERATOR_MODIFIER"
)

package kotlin

/**
 * Represents a double-precision 64-bit IEEE 754 floating point number.
 *
 * In Dart, values of this type are represented as values of the primitive type `double`.
 */
@DartLibrary("dart:core")
@DartName("double")
external class Double private constructor() : Number() {
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
    override fun toDouble(): Double = this

    // dart:core double members
    fun abs(): Double

    /**
     * The sign of the double's numerical value.
     *
     * Returns -1.0 if the value is less than zero,
     * +1.0 if the value is greater than zero,
     * and the value itself if it is -0.0, 0.0 or NaN.
     */
    val sign: Double

    /**
     * Returns the integer closest to this number.
     *
     * Rounds away from zero when there is no closest integer:
     * `(3.5).round() == 4` and `(-3.5).round() == -4`.
     *
     * Throws an [UnsupportedError] if this number is not finite
     * (NaN or an infinity).
     */
    fun round(): Int

    /**
     * Returns the greatest integer no greater than this number.
     *
     * Rounds the number towards negative infinity.
     *
     * Throws an [UnsupportedError] if this number is not finite
     * (NaN or infinity).
     */
    fun floor(): Int

    /**
     * Returns the least integer which is not smaller than this number.
     *
     * Rounds the number towards infinity.
     *
     * Throws an [UnsupportedError] if this number is not finite
     * (NaN or an infinity), .
     */
    fun ceil(): Int

    /**
     * Returns the integer obtained by discarding any fractional
     * part of this number.
     *
     * Rounds the number towards zero.
     *
     * Throws an [UnsupportedError] if this number is not finite
     * (NaN or an infinity).
     */
    fun truncate(): Int

    /**
     * Returns the integer double value closest to `this`.
     *
     * Rounds away from zero when there is no closest integer:
     * `(3.5).roundToDouble() == 4` and `(-3.5).roundToDouble() == -4`.
     *
     * If this is already an integer valued double, including `-0.0`, or it is not
     * a finite value, the value is returned unmodified.
     *
     * For the purpose of rounding, `-0.0` is considered to be below `0.0`,
     * and `-0.0` is therefore considered closer to negative numbers than `0.0`.
     * This means that for a value, `d` in the range `-0.5 < d < 0.0`,
     * the result is `-0.0`.
     */
    fun roundToDouble(): Double

    /**
     * Returns the greatest integer double value no greater than `this`.
     *
     * If this is already an integer valued double, including `-0.0`, or it is not
     * a finite value, the value is returned unmodified.
     *
     * For the purpose of rounding, `-0.0` is considered to be below `0.0`.
     * A number `d` in the range `0.0 < d < 1.0` will return `0.0`.
     */
    fun floorToDouble(): Double

    /**
     * Returns the least integer double value no smaller than `this`.
     *
     * If this is already an integer valued double, including `-0.0`, or it is not
     * a finite value, the value is returned unmodified.
     *
     * For the purpose of rounding, `-0.0` is considered to be below `0.0`.
     * A number `d` in the range `-1.0 < d < 0.0` will return `-0.0`.
     */
    fun ceilToDouble(): Double

    /**
     * Returns the integer double value obtained by discarding any fractional
     * digits from `this`.
     *
     * If this is already an integer valued double, including `-0.0`, or it is not
     * a finite value, the value is returned unmodified.
     *
     * For the purpose of rounding, `-0.0` is considered to be below `0.0`.
     * A number `d` in the range `-1.0 < d < 0.0` will return `-0.0`, and
     * in the range `0.0 < d < 1.0` it will return 0.0.
     */
    fun truncateToDouble(): Double
}