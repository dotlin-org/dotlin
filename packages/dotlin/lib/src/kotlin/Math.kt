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

package kotlin.math

//import kotlin.internal.InlineOnly
import dart.math.sin as dartSin
import dart.math.cos as dartCos
import dart.math.tan as dartTan
import dart.math.asin as dartAsin
import dart.math.acos as dartAcos
import dart.math.atan as dartAtan
import dart.math.atan2 as dartAtan2
import dart.math.sqrt as dartSqrt
import dart.math.min as dartMin
import dart.math.max as dartMax
import dart.math.pow as dartPow
import dart.math.exp as dartExp
import dart.math.log as dartLog

// region ================ Double Math ========================================

/* TODO: Unsupported by Dart.
/**
 * Computes the hyperbolic sine of the value [x].
 *
 * Special cases:
 *   - `sinh(NaN)` is `NaN`
 *   - `sinh(+Inf)` is `+Inf`
 *   - `sinh(-Inf)` is `-Inf`
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun sinh(x: Double): Double = dart.sinh(x)

/**
 * Computes the hyperbolic cosine of the value [x].
 *
 * Special cases:
 *   - `cosh(NaN)` is `NaN`
 *   - `cosh(+Inf|-Inf)` is `+Inf`
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun cosh(x: Double): Double = dart.cosh(x)

/**
 * Computes the hyperbolic tangent of the value [x].
 *
 * Special cases:
 *   - `tanh(NaN)` is `NaN`
 *   - `tanh(+Inf)` is `1.0`
 *   - `tanh(-Inf)` is `-1.0`
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun tanh(x: Double): Double = dart.tanh(x)

/**
 * Computes the inverse hyperbolic sine of the value [x].
 *
 * The returned value is `y` such that `sinh(y) == x`.
 *
 * Special cases:
 *   - `asinh(NaN)` is `NaN`
 *   - `asinh(+Inf)` is `+Inf`
 *   - `asinh(-Inf)` is `-Inf`
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun asinh(x: Double): Double = dart.asinh(x)

/**
 * Computes the inverse hyperbolic cosine of the value [x].
 *
 * The returned value is positive `y` such that `cosh(y) == x`.
 *
 * Special cases:
 *   - `acosh(NaN)` is `NaN`
 *   - `acosh(x)` is `NaN` when `x < 1`
 *   - `acosh(+Inf)` is `+Inf`
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun acosh(x: Double): Double = dart.acosh(x)

/**
 * Computes the inverse hyperbolic tangent of the value [x].
 *
 * The returned value is `y` such that `tanh(y) == x`.
 *
 * Special cases:
 *   - `tanh(NaN)` is `NaN`
 *   - `tanh(x)` is `NaN` when `x > 1` or `x < -1`
 *   - `tanh(1.0)` is `+Inf`
 *   - `tanh(-1.0)` is `-Inf`
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun atanh(x: Double): Double = dart.atanh(x)

/**
 * Computes `sqrt(x^2 + y^2)` without intermediate overflow or underflow.
 *
 * Special cases:
 *   - returns `+Inf` if any of arguments is infinite
 *   - returns `NaN` if any of arguments is `NaN` and the other is not infinite
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun hypot(x: Double, y: Double): Double = dart.hypot(x, y)
*/

/* TODO: Unsupported by Dart.
/**
 * Computes `exp(x) - 1`.
 *
 * This function can be implemented to produce more precise result for [x] near zero.
 *
 * Special cases:
 *   - `expm1(NaN)` is `NaN`
 *   - `expm1(+Inf)` is `+Inf`
 *   - `expm1(-Inf)` is `-1.0`
 *
 * @see [exp] function.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun expm1(x: Double): Double = dart.expm1(x)
*/
/**
 * Computes the logarithm of the value [x] to the given [base].
 *
 * Special cases:
 *   - `log(x, b)` is `NaN` if either `x` or `b` are `NaN`
 *   - `log(x, b)` is `NaN` when `x < 0` or `b <= 0` or `b == 1.0`
 *   - `log(+Inf, +Inf)` is `NaN`
 *   - `log(+Inf, b)` is `+Inf` for `b > 1` and `-Inf` for `b < 1`
 *   - `log(0.0, b)` is `-Inf` for `b > 1` and `+Inf` for `b > 1`
 *
 * See also logarithm functions for common fixed bases: [ln], [log10] and [log2].
 */
@SinceKotlin("1.2")
/*actual*/ fun log(x: Double, base: Double): Double {
    if (base <= 0.0 || base == 1.0) return Double.NaN
    return dartLog(x) / dartLog(base)
}

/* TODO: Unsupported by Dart.
/**
 * Computes the natural logarithm (base `E`) of the value [x].
 *
 * Special cases:
 *   - `ln(NaN)` is `NaN`
 *   - `ln(x)` is `NaN` when `x < 0.0`
 *   - `ln(+Inf)` is `+Inf`
 *   - `ln(0.0)` is `-Inf`
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun ln(x: Double): Double = dartLog(x)

/**
 * Computes the common logarithm (base 10) of the value [x].
 *
 * @see [ln] function for special cases.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun log10(x: Double): Double = dartLog10(x)

/**
 * Computes the binary logarithm (base 2) of the value [x].
 *
 * @see [ln] function for special cases.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun log2(x: Double): Double = dartLog2(x)

/**
 * Computes `ln(x + 1)`.
 *
 * This function can be implemented to produce more precise result for [x] near zero.
 *
 * Special cases:
 *   - `ln1p(NaN)` is `NaN`
 *   - `ln1p(x)` is `NaN` where `x < -1.0`
 *   - `ln1p(-1.0)` is `-Inf`
 *   - `ln1p(+Inf)` is `+Inf`
 *
 * @see [ln] function
 * @see [expm1] function
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun ln1p(x: Double): Double = dartLog1p(x)
*/
/**
 * Rounds the given value [x] to an integer towards positive infinity.

 * @return the smallest double value that is greater than or equal to the given value [x] and is a mathematical integer.
 *
 * Special cases:
 *   - `ceil(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun ceil(x: Double): Double = x.ceilToDouble()

/**
 * Rounds the given value [x] to an integer towards negative infinity.

 * @return the largest double value that is smaller than or equal to the given value [x] and is a mathematical integer.
 *
 * Special cases:
 *   - `floor(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun floor(x: Double): Double = x.floorToDouble()

/**
 * Rounds the given value [x] to an integer towards zero.
 *
 * @return the value [x] having its fractional part truncated.
 *
 * Special cases:
 *   - `truncate(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun truncate(x: Double): Double = x.truncateToDouble()

/**
 * Rounds the given value [x] towards the closest integer with ties rounded towards even integer.
 *
 * Special cases:
 *   - `round(x)` is `x` where `x` is `NaN` or `+Inf` or `-Inf` or already a mathematical integer.
 */
@SinceKotlin("1.2")
/*actual*/ fun round(x: Double): Double {
    if (x % 0.5 != 0.0) {
        return x.roundToDouble()
    }
    val floored = floor(x)
    return if (floored % 2 == 0.0) floored else ceil(x)
}

/**
 * Returns the absolute value of the given value [x].
 *
 * Special cases:
 *   - `abs(NaN)` is `NaN`
 *
 * @see absoluteValue extension property for [Double]
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun abs(x: Double): Double = x.abs()

/**
 * Returns the sign of the given value [x]:
 *   - `-1.0` if the value is negative,
 *   - zero if the value is zero,
 *   - `1.0` if the value is positive
 *
 * Special case:
 *   - `sign(NaN)` is `NaN`
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun sign(x: Double): Double = x.sign


/**
 * Returns the smaller of two values.
 *
 * If either value is `NaN`, then the result is `NaN`.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun min(a: Double, b: Double): Double = dartMin(a, b)

/**
 * Returns the greater of two values.
 *
 * If either value is `NaN`, then the result is `NaN`.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun max(a: Double, b: Double): Double = dartMax(a, b)

// extensions

/**
 * Raises this value to the power [x].
 *
 * Special cases:
 *   - `b.pow(0.0)` is `1.0`
 *   - `b.pow(1.0) == b`
 *   - `b.pow(NaN)` is `NaN`
 *   - `NaN.pow(x)` is `NaN` for `x != 0.0`
 *   - `b.pow(Inf)` is `NaN` for `abs(b) == 1.0`
 *   - `b.pow(x)` is `NaN` for `b < 0` and `x` is finite and not an integer
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun Double.pow(x: Double): Double = dartPow(this, x).toDouble()

/**
 * Raises this value to the integer power [n].
 *
 * See the other overload of [pow] for details.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun Double.pow(n: Int): Double = dartPow(this, n.toDouble()).toDouble()

/**
 * Returns the absolute value of this value.
 *
 * Special cases:
 *   - `NaN.absoluteValue` is `NaN`
 *
 * @see abs function
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline val Double.absoluteValue: Double get() = abs()

/**
 * Returns this value with the sign bit same as of the [sign] value.
 *
 * If [sign] is `NaN` the sign of the result is undefined.
 */
@SinceKotlin("1.2")
/*actual*/ fun Double.withSign(sign: Double): Double {
    return if (this.sign == sign.sign) this else -this
}

/**
 * Returns this value with the sign bit same as of the [sign] value.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun Double.withSign(sign: Int): Double = this.withSign(sign.toDouble())

/**
 * Returns the ulp (unit in the last place) of this value.
 *
 * An ulp is a positive distance between this value and the next nearest [Double] value larger in magnitude.
 *
 * Special Cases:
 *   - `NaN.ulp` is `NaN`
 *   - `x.ulp` is `+Inf` when `x` is `+Inf` or `-Inf`
 *   - `0.0.ulp` is `Double.MIN_VALUE`
 */
@SinceKotlin("1.2")
/*actual*/ val Double.ulp: Double get() = when {
    this < 0 -> (-this).ulp
    this.isNaN || this == Double.POSITIVE_INFINITY -> this
    this == Double.MAX_VALUE -> this - this.nextDown()
    else -> this.nextUp() - this
}

/**
 * Returns the [Double] value nearest to this value in direction of positive infinity.
 */
@SinceKotlin("1.2")
/*actual*/ fun Double.nextUp(): Double = when {
    this.isNaN || this == Double.POSITIVE_INFINITY -> this
    this == 0.0 -> Double.MIN_VALUE
    else -> Double.fromBits(this.toRawBits() + if (this > 0) 1 else -1)
}

/**
 * Returns the [Double] value nearest to this value in direction of negative infinity.
 */
@SinceKotlin("1.2")
/*actual*/ fun Double.nextDown(): Double = when {
    this.isNaN || this == Double.NEGATIVE_INFINITY -> this
    this == 0.0 -> -Double.MIN_VALUE
    else -> Double.fromBits(this.toRawBits() + if (this > 0) -1 else 1)
}


/**
 * Returns the [Double] value nearest to this value in direction from this value towards the value [to].
 *
 * Special cases:
 *   - `x.nextTowards(y)` is `NaN` if either `x` or `y` are `NaN`
 *   - `x.nextTowards(x) == x`
 *
 */
@SinceKotlin("1.2")
/*actual*/ fun Double.nextTowards(to: Double): Double = when {
    this.isNaN || to.isNaN -> Double.NaN
    to == this -> to
    to > this -> this.nextUp()
    else /* to < this */ -> this.nextDown()
}

// endregion

// region ================ Integer Math ========================================


/**
 * Returns the absolute value of the given value [n].
 *
 * Special cases:
 *   - `abs(Int.MIN_VALUE)` is `Int.MIN_VALUE` due to an overflow
 *
 * @see absoluteValue extension property for [Int]
 */
@SinceKotlin("1.2")
/*actual*/ fun abs(n: Int): Int = n.abs()

/**
 * Returns the smaller of two values.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun min(a: Int, b: Int): Int = dartMin(a, b)

/**
 * Returns the greater of two values.
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline fun max(a: Int, b: Int): Int = dartMax(a, b)

/**
 * Returns the absolute value of this value.
 *
 * Special cases:
 *   - `Int.MIN_VALUE.absoluteValue` is `Int.MIN_VALUE` due to an overflow
 *
 * @see abs function
 */
@SinceKotlin("1.2")
//@InlineOnly
/*actual*/ inline val Int.absoluteValue: Int get() = abs(this)

/**
 * Returns the sign of this value:
 *   - `-1` if the value is negative,
 *   - `0` if the value is zero,
 *   - `1` if the value is positive
 */
@SinceKotlin("1.2")
/*actual*/ val Int.sign: Int get() = when {
    this < 0 -> -1
    this > 0 -> 1
    else -> 0
}