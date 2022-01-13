/*
 * Copyright 2010-2016 JetBrains s.r.o.
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
    "NON_MEMBER_FUNCTION_NO_BODY"
)

package kotlin

import kotlin.internal.PureReifiable

/**
 * Returns a string representation of the object. Can be called with a null receiver, in which case
 * it returns the string "null".
 */
@DartName("safeToString")
fun Any?.toString(): String {
    if (this == null) return "null"
    return toString()
}

/**
 * Concatenates this string with the string representation of the given [other] object. If either the receiver
 * or the [other] object are null, they are represented as the string "null".
 */
operator fun String?.plus(other: Any?): String = toString() + other.toString()

/**
 * Returns an array of objects of the given type with the given [size], initialized with null values.
 */
inline fun <reified @PureReifiable T> arrayOfNulls(size: Int): Array<T?>

/**
 * Returns an array containing the specified elements.
 */
inline fun <reified @PureReifiable T> arrayOf(vararg elements: T): Array<T> = dart("<T>[...elements]")

/**
 * Returns an empty array of the specified type [T].
 */
inline fun <reified @PureReifiable T> emptyArray(): Array<T> = dart("<T>[]")

/**
 * Returns an array containing enum T entries.
 */
@SinceKotlin("1.1")
inline fun <reified T : Enum<T>> enumValues(): Array<T>

/**
 * Returns an enum entry with specified name.
 */
@SinceKotlin("1.1")
inline fun <reified T : Enum<T>> enumValueOf(name: String): T