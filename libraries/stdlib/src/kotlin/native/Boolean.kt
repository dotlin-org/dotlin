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

@file:Suppress(
    "NON_ABSTRACT_FUNCTION_WITH_NO_BODY",
    "MUST_BE_INITIALIZED_OR_BE_ABSTRACT",
    "UNUSED_PARAMETER",
    "WRONG_EXTERNAL_DECLARATION" // TODO: Fix in analyzer
)

package kotlin

/**
 * Represents a value which is either `true` or `false`.
 *
 * In Dart, values of this type are represented as values of type `bool`.
 */
@DartLibrary("dart:core")
@DartName("bool")
external class Boolean private constructor() : Comparable<Boolean> {
    /**
     * Returns the inverse of this boolean.
     */
    operator fun not(): Boolean

    /**
     * Performs a logical `and` operation between this Boolean and the [other] one. Unlike the `&&` operator,
     * this function does not perform short-circuit evaluation. Both `this` and [other] will always be evaluated.
     */
    infix fun and(other: Boolean): Boolean

    /**
     * Performs a logical `or` operation between this Boolean and the [other] one. Unlike the `||` operator,
     * this function does not perform short-circuit evaluation. Both `this` and [other] will always be evaluated.
     */
    infix fun or(other: Boolean): Boolean

    /**
     * Performs a logical `xor` operation between this Boolean and the [other] one.
     */
    infix fun xor(other: Boolean): Boolean

    override fun compareTo(other: Boolean): Int

    @DartName("Boolean\$Companion")
    @SinceKotlin("1.3")
    companion object {}
}
