/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package kotlin

/**
 * Superclass for all platform classes representing numeric values.
 */
@DartLibrary("dart:core")
@DartName("num")
external abstract class Number : Comparable<Number> {
    /**
     * Returns the value of this number as a [Double], which may involve rounding.
     */
    abstract fun toDouble(): Double

    /**
     * Returns the value of this number as an [Int], which may involve rounding or truncation.
     */
    abstract fun toInt(): Int

    /**
     * Compares this to `other`.
     *
     * Returns a negative number if `this` is less than `other`, zero if they are
     * equal, and a positive number if `this` is greater than `other`.
     *
     * The ordering represented by this method is a total ordering of [Number]
     * values. All distinct doubles are non-equal, as are all distinct integers,
     * but integers are equal to doubles if they have the same numerical
     * value.
     *
     * For [Double]s, the `compareTo` operation is different from the partial
     * ordering given by [equals]. For example,
     * IEEE doubles impose that `0.0 == -0.0` and all comparison operations on
     * NaN return false.
     *
     * This function imposes a complete ordering for doubles. When using
     * `compareTo` the following properties hold:
     *
     * - All NaN values are considered equal, and greater than any numeric value.
     * - -0.0 is less than 0.0 (and the integer 0), but greater than any non-zero
     * negative value.
     * - Negative infinity is less than all other values and positive infinity is
     * greater than all non-NaN values.
     * - All other values are compared using their numeric value.
     *
     * Examples:
     * ```kotlin
     * println(1.compareTo(2)) // => -1
     * println(2.compareTo(1)) // => 1
     * println(1.compareTo(1)) // => 0
     *
     * // The following comparisons yield different results than the
     * // corresponding comparison operators.
     * println((-0.0).compareTo(0.0))  // => -1
     * println(Double.NaN.compareTo(Double.NaN))  // => 0
     * println(Double.POSITIVE_INFINITY.compareTo(Double.NaN)) // => -1
     *
     * // -0.0, and NaN comparison operators have rules imposed by the IEEE
     * // standard.
     * println(-0.0 == 0.0); // => true
     * println(Double.NaN == Double.NaN)  // => false
     * println(Double.POSITIVE_INFINITY < Double.NaN)  // => false
     * println(Double.NaN < Double.POSITIVE_INFINITY)  // => false
     * println(Double.NaN == Double.POSITIVE_INFINITY)  // => false
    ```
     */
    override fun compareTo(other: Number): Int

    /**
     * Whether this number is a Not-a-Number value.
     *
     * Is `true` if this number is the [Double.NaN] value
     * or any other of the possible [double] NaN values.
     * Is `false` if this number is an integer,
     * a finite double or an infinite double ([double.infinity]
     * or [double.negativeInfinity]).
     *
     * All numbers satisfy exactly one of [isInfinite], [isFinite]
     * and `isNaN`.
     */
    val isNaN: Boolean

    /**
     * Whether this number is positive infinity or negative infinity.
     *
     * Only satisfied by [Double.infinity] and [Double.negativeInfinity].
     *
     * All numbers satisfy exactly one of `isInfinite`, [isFinite]
     * and [isNaN].
     */
    val isInfinite: Boolean

    /**
     * Whether this number is finite.
     *
     * The only non-finite numbers are NaN values, positive infinity, and
     * negative infinity. All integers are finite.
     *
     * All numbers satisfy exactly one of [isInfinite], `isFinite`
     * and [isNaN].
     */
    val isFinite: Boolean
}

