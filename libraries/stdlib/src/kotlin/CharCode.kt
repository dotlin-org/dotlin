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
    "PRIMARY_CONSTRUCTOR_DELEGATION_CALL_EXPECTED"
)

package kotlin

/**
 * Creates a Char with the specified [code], or throws an exception if the [code] is out of `Char.MIN_VALUE.code..Char.MAX_VALUE.code`.
 *
 * If the program that calls this function is written in a way that only valid [code] is passed as the argument,
 * using the overload that takes a [UShort] argument is preferable (`Char(intValue.toUShort())`).
 * That overload doesn't check validity of the argument, and may improve program performance when the function is called routinely inside a loop.
 *
 * @sample samples.text.Chars.charFromCode
 */
@SinceKotlin("1.5")
//@kotlin.internal.InlineOnly
inline fun Char(code: Int): Char {
    if (code < Char.MIN_VALUE.code || code > Char.MAX_VALUE.code) {
        throw IllegalArgumentException("Invalid Char code: $code")
    }
    return code.toChar()
}

/**
 * Creates a Char with the specified [code].
 *
 * @sample samples.text.Chars.charFromCode
 */
//@SinceKotlin("1.5")
//TODO fun Char(code: UShort): Char

/**
 * Returns the code of this Char.
 *
 * Code of a Char is the value it was constructed with, and the UTF-16 code unit corresponding to this Char.
 *
 * @sample samples.text.Chars.code
 */
@SinceKotlin("1.5")
//@kotlin.internal.InlineOnly
@Suppress("DEPRECATION")
inline val Char.code: Int get() = this.toInt()
