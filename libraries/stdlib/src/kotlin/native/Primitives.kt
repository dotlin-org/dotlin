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
 * Represents a 32-bit signed integer.
 *
 * In Dart, values of this type are represented as values of the primitive type `int`.
 */
@DartLibrary("dart:core")
@DartName("int")
external class Int private constructor() : Number() {
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
    operator fun rangeTo(other: Int): IntRange
     /** Creates a range from this value to the specified [other] value. */
    operator fun rangeTo(other: Long): IntRange

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

    // dart:core int members
    /**
     * Returns this integer to the power of [exponent] modulo [modulus].
     *
     * The [exponent] must be non-negative and [modulus] must be
     * positive.
     * int modPow(int exponent, int modulus);
     *
     * Returns the modular multiplicative inverse of this integer
     * modulo [modulus].
     *
     * The [modulus] must be positive.
     *
     * It is an error if no modular inverse exists.
     */
    fun modInverse(modulus: Int): Int

    /**
     * Returns the greatest common divisor of this integer and [other].
     *
     * If either number is non-zero, the result is the numerically greatest
     * integer dividing both `this` and `other`.
     *
     * The greatest common divisor is independent of the order,
     * so `x.gcd(y)` is  always the same as `y.gcd(x)`.
     *
     * For any integer `x`, `x.gcd(x)` is `x.abs()`.
     *
     * If both `this` and `other` is zero, the result is also zero.
     */
    fun gcd(other: Int): Int

    /**
     * Returns true if and only if this integer is even.
     */
    val isEven: Int

    /**
     * Returns true if and only if this integer is odd.
     */
    val isOdd: Int

    /**
     * Returns the minimum number of bits required to store this integer.
     *
     * The number of bits excludes the sign bit, which gives the natural length
     * for non-negative (unsigned) values.  Negative values are complemented to
     * return the bit position of the first bit that differs from the sign bit.
     *
     * To find the number of bits needed to store the value as a signed value,
     * add one, i.e. use `x.bitLength + 1`.
     * ```dart
     * x.bitLength == (-x-1).bitLength;
     *
     * 3.bitLength == 2;     // 00000011
     * 2.bitLength == 2;     // 00000010
     * 1.bitLength == 1;     // 00000001
     * 0.bitLength == 0;     // 00000000
     * (-1).bitLength == 0;  // 11111111
     * (-2).bitLength == 1;  // 11111110
     * (-3).bitLength == 2;  // 11111101
     * (-4).bitLength == 2;  // 11111100
     * ```
     */
    val bitLength: Int

    /**
     * Returns the least significant [width] bits of this integer as a
     * non-negative number (i.e. unsigned representation).  The returned value has
     * zeros in all bit positions higher than [width].
     * ```dart
     * (-1).toUnsigned(5) == 31   // 11111111  ->  00011111
     * ```
     * This operation can be used to simulate arithmetic from low level languages.
     * For example, to increment an 8 bit quantity:
     * ```dart
     * q = (q + 1).toUnsigned(8);
     * ```
     * `q` will count from `0` up to `255` and then wrap around to `0`.
     *
     * If the input fits in [width] bits without truncation, the result is the
     * same as the input.  The minimum width needed to avoid truncation of `x` is
     * given by `x.bitLength`, i.e.
     * ```dart
     * x == x.toUnsigned(x.bitLength);
     * ```
     */
    fun toUnsigned(width: Int): Int

    /**
     * Returns the least significant [width] bits of this integer, extending the
     * highest retained bit to the sign.  This is the same as truncating the value
     * to fit in [width] bits using an signed 2-s complement representation.  The
     * returned value has the same bit value in all positions higher than [width].
     *
     * ```dart
     * //     V--sign bit-V
     * 16.toSigned(5) == -16;   //  00010000 -> 11110000
     * 239.toSigned(5) == 15;   //  11101111 -> 00001111
     * //     ^           ^
     * ```
     * This operation can be used to simulate arithmetic from low level languages.
     * For example, to increment an 8 bit signed quantity:
     * ```dart
     * q = (q + 1).toSigned(8);
     * ```
     * `q` will count from `0` up to `127`, wrap to `-128` and count back up to
     * `127`.
     *
     * If the input value fits in [width] bits without truncation, the result is
     * the same as the input.  The minimum width needed to avoid truncation of `x`
     * is `x.bitLength + 1`, i.e.
     * ```dart
     * x == x.toSigned(x.bitLength + 1);
     * ```
     */
    fun toSigned(width: Int): Int

    /**
     * Returns the absolute value of this integer.
     *
     * For any integer `value`,
     * the result is the same as `value < 0 ? -value : value`.
     *
     * Integer overflow may cause the result of `-value` to stay negative.
     */
    fun abs(): Int

    /**
     * Returns the sign of this integer.
     *
     * Returns 0 for zero, -1 for values less than zero and
     * +1 for values greater than zero.
     */
    val sign: Int;

    /**
     * Returns `this`.
     */
    fun round(): Int

    /**
     * Returns `this`.
     */
    fun floor(): Int

    /**
     * Returns `this`.
     */
    fun ceil(): Int

    /**
     * Returns `this`.
     */
    fun truncate(): Int

    /**
     * Returns `this.toDouble()`.
     */
    fun roundToDouble(): Double

    /**
     * Returns `this.toDouble()`.
     */
    fun floorToDouble(): Double

    /**
     * Returns `this.toDouble()`.
     */
    fun ceilToDouble(): Double

    /**
     * Returns `this.toDouble()`.
     */
    fun truncateToDouble(): Double

    /**
     * Converts [this] to a string representation in the given [radix].
     *
     * In the string representation, lower-case letters are used for digits above
     * '9', with 'a' being 10 an 'z' being 35.
     *
     * The [radix] argument must be an integer in the range 2 to 36.
     */
    fun toRadixString(radix: Int): String
}

/**
 * Represents [Int] _literals_ outside of the range `-2147483648..2147483647`.
 *
 * Note that [Int] already represents 64-bit signed integers, and is the only integer class that should be used.
 * [Long] is fully assignable to [Int], without conversion.
 *
 * Referencing the [Long] type directly is an error.
 */
@DartLibrary("dart:core")
@DartName("int")
external class Long private constructor() : Number() {
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
    operator fun rangeTo(other: Int): IntRange
    /** Creates a range from this value to the specified [other] value. */
    operator fun rangeTo(other: Long): IntRange

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
    fun inv(): Int

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

    // dart:core int members
    /**
     * Returns this integer to the power of [exponent] modulo [modulus].
     *
     * The [exponent] must be non-negative and [modulus] must be
     * positive.
     * int modPow(int exponent, int modulus);
     *
     * Returns the modular multiplicative inverse of this integer
     * modulo [modulus].
     *
     * The [modulus] must be positive.
     *
     * It is an error if no modular inverse exists.
     */
    fun modInverse(modulus: Int): Int

    /**
     * Returns the greatest common divisor of this integer and [other].
     *
     * If either number is non-zero, the result is the numerically greatest
     * integer dividing both `this` and `other`.
     *
     * The greatest common divisor is independent of the order,
     * so `x.gcd(y)` is  always the same as `y.gcd(x)`.
     *
     * For any integer `x`, `x.gcd(x)` is `x.abs()`.
     *
     * If both `this` and `other` is zero, the result is also zero.
     */
    fun gcd(other: Int): Int

    /**
     * Returns true if and only if this integer is even.
     */
    val isEven: Int

    /**
     * Returns true if and only if this integer is odd.
     */
    val isOdd: Int

    /**
     * Returns the minimum number of bits required to store this integer.
     *
     * The number of bits excludes the sign bit, which gives the natural length
     * for non-negative (unsigned) values.  Negative values are complemented to
     * return the bit position of the first bit that differs from the sign bit.
     *
     * To find the number of bits needed to store the value as a signed value,
     * add one, i.e. use `x.bitLength + 1`.
     * ```dart
     * x.bitLength == (-x-1).bitLength;
     *
     * 3.bitLength == 2;     // 00000011
     * 2.bitLength == 2;     // 00000010
     * 1.bitLength == 1;     // 00000001
     * 0.bitLength == 0;     // 00000000
     * (-1).bitLength == 0;  // 11111111
     * (-2).bitLength == 1;  // 11111110
     * (-3).bitLength == 2;  // 11111101
     * (-4).bitLength == 2;  // 11111100
     * ```
     */
    val bitLength: Int

    /**
     * Returns the least significant [width] bits of this integer as a
     * non-negative number (i.e. unsigned representation).  The returned value has
     * zeros in all bit positions higher than [width].
     * ```dart
     * (-1).toUnsigned(5) == 31   // 11111111  ->  00011111
     * ```
     * This operation can be used to simulate arithmetic from low level languages.
     * For example, to increment an 8 bit quantity:
     * ```dart
     * q = (q + 1).toUnsigned(8);
     * ```
     * `q` will count from `0` up to `255` and then wrap around to `0`.
     *
     * If the input fits in [width] bits without truncation, the result is the
     * same as the input.  The minimum width needed to avoid truncation of `x` is
     * given by `x.bitLength`, i.e.
     * ```dart
     * x == x.toUnsigned(x.bitLength);
     * ```
     */
    fun toUnsigned(width: Int): Int

    /**
     * Returns the least significant [width] bits of this integer, extending the
     * highest retained bit to the sign.  This is the same as truncating the value
     * to fit in [width] bits using an signed 2-s complement representation.  The
     * returned value has the same bit value in all positions higher than [width].
     *
     * ```dart
     * //     V--sign bit-V
     * 16.toSigned(5) == -16;   //  00010000 -> 11110000
     * 239.toSigned(5) == 15;   //  11101111 -> 00001111
     * //     ^           ^
     * ```
     * This operation can be used to simulate arithmetic from low level languages.
     * For example, to increment an 8 bit signed quantity:
     * ```dart
     * q = (q + 1).toSigned(8);
     * ```
     * `q` will count from `0` up to `127`, wrap to `-128` and count back up to
     * `127`.
     *
     * If the input value fits in [width] bits without truncation, the result is
     * the same as the input.  The minimum width needed to avoid truncation of `x`
     * is `x.bitLength + 1`, i.e.
     * ```dart
     * x == x.toSigned(x.bitLength + 1);
     * ```
     */
    fun toSigned(width: Int): Int

    /**
     * Returns the absolute value of this integer.
     *
     * For any integer `value`,
     * the result is the same as `value < 0 ? -value : value`.
     *
     * Integer overflow may cause the result of `-value` to stay negative.
     */
    fun abs(): Int

    /**
     * Returns the sign of this integer.
     *
     * Returns 0 for zero, -1 for values less than zero and
     * +1 for values greater than zero.
     */
    val sign: Int;

    /**
     * Returns `this`.
     */
    fun round(): Int

    /**
     * Returns `this`.
     */
    fun floor(): Int

    /**
     * Returns `this`.
     */
    fun ceil(): Int

    /**
     * Returns `this`.
     */
    fun truncate(): Int

    /**
     * Returns `this.toDouble()`.
     */
    fun roundToDouble(): Double

    /**
     * Returns `this.toDouble()`.
     */
    fun floorToDouble(): Double

    /**
     * Returns `this.toDouble()`.
     */
    fun ceilToDouble(): Double

    /**
     * Returns `this.toDouble()`.
     */
    fun truncateToDouble(): Double

    /**
     * Converts [this] to a string representation in the given [radix].
     *
     * In the string representation, lower-case letters are used for digits above
     * '9', with 'a' being 10 an 'z' being 35.
     *
     * The [radix] argument must be an integer in the range 2 to 36.
     */
    fun toRadixString(radix: Int): String
}

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