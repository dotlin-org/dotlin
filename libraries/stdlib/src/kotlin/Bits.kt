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

import dart.typeddata.ByteData

/**
 * Returns a bit representation of the specified floating-point value as [Int]
 * according to the IEEE 754 floating-point "double format" bit layout.
 */
@SinceKotlin("1.2")
fun Double.toBits(): Int =
    doubleToRawBits(if (this.isNaN()) Double.NaN else this)

/**
 * Returns a bit representation of the specified floating-point value as [Int]
 * according to the IEEE 754 floating-point "double format" bit layout,
 * preserving `NaN` values exact layout.
 */
@SinceKotlin("1.2")
fun Double.toRawBits(): Int =
    doubleToRawBits(this)

/**
 * Returns the [Double] value corresponding to a given bit representation.
 */
@SinceKotlin("1.2")
/*inline*/ fun Double.Companion.fromBits(bits: Int): Double =
    doubleFromBits(bits)

private val buffer = ByteData(8)

private fun doubleToRawBits(value: Double): Int {
    buffer.setFloat64(0, value)
    return buffer.getInt64(0)
}

private fun doubleFromBits(value: Int): Double {
    buffer.setInt64(0, value)
    return buffer.getFloat64(0)
}
