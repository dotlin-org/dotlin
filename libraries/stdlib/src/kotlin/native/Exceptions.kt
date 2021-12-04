/*
 * Copyright 2010-2020 JetBrains s.r.o.
 * Copyright 2021 Wilko Manger
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
    "NON_MEMBER_FUNCTION_NO_BODY",
    "MUST_BE_INITIALIZED_OR_BE_ABSTRACT",
    "UNUSED_PARAMETER",
    "PRIMARY_CONSTRUCTOR_DELEGATION_CALL_EXPECTED",
    "EXTENSION_PROPERTY_MUST_HAVE_ACCESSORS_OR_BE_ABSTRACT"
)

package kotlin

public open class Error : Throwable {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

public open class Exception : Throwable {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

public open class RuntimeException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

public open class IllegalArgumentException : RuntimeException {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

public open class IllegalStateException : RuntimeException {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

public open class IndexOutOfBoundsException : RuntimeException {
    constructor()
    constructor(message: String?)
}

public open class ConcurrentModificationException : RuntimeException {
    constructor()
    constructor(message: String?)
    @Deprecated("The constructor is not supported on all platforms and will be removed from kotlin-stdlib-common soon.", level = DeprecationLevel.ERROR)
    constructor(message: String?, cause: Throwable?)
    @Deprecated("The constructor is not supported on all platforms and will be removed from kotlin-stdlib-common soon.", level = DeprecationLevel.ERROR)
    constructor(cause: Throwable?)
}

public open class UnsupportedOperationException : RuntimeException {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}

public open class NumberFormatException : IllegalArgumentException {
    constructor()
    constructor(message: String?)
}

public open class NullPointerException : RuntimeException {
    constructor()
    constructor(message: String?)
}

public open class ClassCastException : RuntimeException {
    constructor()
    constructor(message: String?)
}

public open class AssertionError : Error {
    constructor()
    constructor(message: Any?)
}

public open class NoSuchElementException : RuntimeException {
    constructor()
    constructor(message: String?)
}

@SinceKotlin("1.3")
public open class ArithmeticException : RuntimeException {
    constructor()
    constructor(message: String?)
}

/**
 * Returns the detailed description of this throwable with its stack trace.
 *
 * The detailed description includes:
 * - the short description (see [Throwable.toString]) of this throwable;
 * - the complete stack trace;
 * - detailed descriptions of the exceptions that were [suppressed][suppressedExceptions] in order to deliver this exception;
 * - the detailed description of each throwable in the [Throwable.cause] chain.
 */
@SinceKotlin("1.4")
public fun Throwable.stackTraceToString(): String

/**
 * Prints the [detailed description][Throwable.stackTraceToString] of this throwable to the standard output or standard error output.
 */
@SinceKotlin("1.4")
public fun Throwable.printStackTrace(): Unit

/**
 * When supported by the platform, adds the specified exception to the list of exceptions that were
 * suppressed in order to deliver this exception.
 */
@SinceKotlin("1.4")
public fun Throwable.addSuppressed(exception: Throwable)

/**
 * Returns a list of all exceptions that were suppressed in order to deliver this exception.
 *
 * The list can be empty:
 * - if no exceptions were suppressed;
 * - if the platform doesn't support suppressed exceptions;
 * - if this [Throwable] instance has disabled the suppression.
 */
//@SinceKotlin("1.4")
//public val Throwable.suppressedExceptions: List<Throwable>
// TODO