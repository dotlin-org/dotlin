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

@file:DartLibrary("dart:math")

package dart.math

/**
 * Converts [radians] to a [Double] and returns the sine of the value.
 *
 * If [radians] is not a finite number, the result is NaN.
 */
external fun sin(radians: Number): Double

/**
 * Converts [radians] to a [Double] and returns the cosine of the value.
 *
 * If [radians] is not a finite number, the result is NaN.
 */
external fun cos(radians: Number): Double

/**
 * Converts [radians] to a [Double] and returns the tangent of the value.
 *
 * The tangent function is equivalent to `sin(radians)/cos(radians)` and may be
 * infinite (positive or negative) when `cos(radians)` is equal to zero.
 * If [radians] is not a finite number, the result is NaN.
 */
external fun tan(radians: Number): Double

/**
 * Converts [x] to a [Double] and returns its arc sine in radians.
 *
 * Returns a value in the range -PI/2..PI/2, or NaN if [x] is outside
 * the range -1..1.
 */
external fun asin(x: Number): Double

/**
 * Converts [x] to a [Double] and returns its arc cosine in radians.
 *
 * Returns a value in the range 0..PI, or NaN if [x] is outside
 * the range -1..1.
 */
external fun acos(x: Number): Double

/**
 * Converts [x] to a [Double] and returns its arc tangent in radians.
 *
 * Returns a value in the range -PI/2..PI/2, or NaN if [x] is NaN.
 */
external fun atan(x: Number): Double

/**
 * A variant of [atan].
 *
 * Converts both arguments to [Double]s.
 *
 * Returns the angle in radians between the positive x-axis
 * and the vector ([b],[a]).
 * The result is in the range -PI..PI.
 *
 * If [b] is positive, this is the same as `atan(a/b)`.
 *
 * The result is negative when [a] is negative (including when [a] is the
 * double -0.0).
 *
 * If [a] is equal to zero, the vector ([b],[a]) is considered parallel to
 * the x-axis, even if [b] is also equal to zero. The sign of [b] determines
 * the direction of the vector along the x-axis.
 *
 * Returns NaN if either argument is NaN.
 */
external fun atan2(a: Number, b: Number): Double

/**
 * Converts [x] to a [Double] and returns the positive square root of the
 * value.
 *
 * Returns -0.0 if [x] is -0.0, and NaN if [x] is otherwise negative or NaN.
 */
external fun sqrt(x: Number): Double

/**
 * Converts [x] to a [Double] and returns the natural exponent, [e],
 * to the power [x].
 *
 * Returns NaN if [x] is NaN.
 */
external fun exp(x: Number): Double

/**
 * Converts [x] to a [Double] and returns the natural logarithm of the value.
 *
 * Returns negative infinity if [x] is equal to zero.
 * Returns NaN if [x] is NaN or less than zero.
 */
external fun log(x: Double): Double;

/**
 * Returns the lesser of two numbers.
 *
 * Returns NaN if either argument is NaN.
 * The lesser of `-0.0` and `0.0` is `-0.0`.
 * If the arguments are otherwise equal (including int and doubles with the
 * same mathematical value) then it is unspecified which of the two arguments
 * is returned.
 */
@PublishedApi
internal external fun <T : Number> min(a: T, b: T): T

/**
 * Returns the larger of two numbers.
 *
 * Returns NaN if either argument is NaN.
 * The larger of `-0.0` and `0.0` is `0.0`. If the arguments are
 * otherwise equal (including int and doubles with the same mathematical value)
 * then it is unspecified which of the two arguments is returned.
 */
@PublishedApi
internal external fun <T : Number> max(a: T, b: T): T

/**
 * Returns [x] to the power of [exponent].
 *
 * If [x] is an [int] and [exponent] is a non-negative [int], the result is
 * an [int], otherwise both arguments are converted to doubles first, and the
 * result is a [Double].
 *
 * For integers, the power is always equal to the mathematical result of `x` to
 * the power `exponent`, only limited by the available memory.
 *
 * For doubles, `pow(x, y)` handles edge cases as follows:
 *
 * - if `y` is zero (0.0 or -0.0), the result is always 1.0.
 * - if `x` is 1.0, the result is always 1.0.
 * - otherwise, if either `x` or `y` is NaN then the result is NaN.
 * - if `x` is negative (but not -0.0) and `y` is a finite non-integer, the
 * result is NaN.
 * - if `x` is Infinity and `y` is negative, the result is 0.0.
 * - if `x` is Infinity and `y` is positive, the result is Infinity.
 * - if `x` is 0.0 and `y` is negative, the result is Infinity.
 * - if `x` is 0.0 and `y` is positive, the result is 0.0.
 * - if `x` is -Infinity or -0.0 and `y` is an odd integer, then the result is
 * `-pow(-x ,y)`.
 * - if `x` is -Infinity or -0.0 and `y` is not an odd integer, then the result
 * is the same as `pow(-x , y)`.
 * - if `y` is Infinity and the absolute value of `x` is less than 1, the
 * result is 0.0.
 * - if `y` is Infinity and `x` is -1, the result is 1.0.
 * - if `y` is Infinity and the absolute value of `x` is greater than 1,
 * the result is Infinity.
 * - if `y` is -Infinity, the result is `1/pow(x, Infinity)`.
 *
 * This corresponds to the `pow` function defined in the IEEE Standard
 * 754-2008.
 *
 * Notice that the result may overflow. If integers are represented as 64-bit
 * numbers, an integer result may be truncated, and a double result may
 * overflow to [Double.POSITIVE_INFINITY] or [Double.NEGATIVE_INFINITY].
 */
external fun pow(x: Number, exponent: Number): Number