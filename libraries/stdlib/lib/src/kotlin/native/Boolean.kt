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
    companion object {
        /**
         * Returns the boolean value of the environment declaration [name].
         *
         * The boolean value of the declaration is `true` if the declared value is
         * the string `"true"`, and `false` if the value is `"false"`.
         *
         * In all other cases, including when there is no declaration for `name`,
         * the result is the [defaultValue].
         *
         * The result is the same as would be returned by:
         * ```kotlin
         * when (@const String.fromEnvironment(name)) {
         *     "true" -> true
         *     "false" -> false
         *     else -> defaultValue
         * }
         * ```
         * Example:
         * ```kotlin
         * const val loggingFlag = @const Boolean.fromEnvironment("logging");
         * ```
         * If you want to use a different truth-string than `"true"`, you can use the
         * [String.fromEnvironment] constructor directly:
         * ```dart
         * const val isLoggingOn = (@const String.fromEnvironment("logging") == "on");
         * ```
         *
         * The string value, or lack of a value, associated with a [name]
         * must be consistent across all calls to [String.fromEnvironment],
         * [int.fromEnvironment], `bool.fromEnvironment` and [bool.hasEnvironment]
         * in a single program.
         *
         * This constructor is only guaranteed to work when invoked as `const`.
         * It may work as a non-constant invocation on some platforms which
         * have access to compiler options at run-time, but most ahead-of-time
         * compiled platforms will not have this information.
         */
        @DartConstructor
        external const fun fromEnvironment(name: String, defaultValue: Boolean = false): Boolean
    }
}
