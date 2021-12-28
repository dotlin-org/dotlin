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
    "EXTENSION_PROPERTY_MUST_HAVE_ACCESSORS_OR_BE_ABSTRACT",
    "SEALED_INHERITOR_IN_DIFFERENT_PACKAGE"
)

package kotlin

import dart.core.StackTrace

open class Error(
    override val message: String?,
    override val cause: Throwable?,
    override val stackTrace: StackTrace? = StackTrace.CURRENT
) : Throwable(message, cause), dart.core.Error {
    @DartName("message")
    constructor(message: String?) : this(message, null)

    @DartName("cause")
    constructor(cause: Throwable?) : this(cause?.toString(), cause)

    @DartName("empty")
    constructor() : this(null, null)
}

open class Exception(override val message: String?, override val cause: Throwable?) : Throwable(message, cause),
    dart.core.Exception {
    @DartName("message")
    constructor(message: String?) : this(message, null)

    @DartName("cause")
    constructor(cause: Throwable?) : this(cause?.toString(), cause)

    @DartName("empty")
    constructor() : this(null, null)
}

open class RuntimeException(override val message: String?, override val cause: Throwable?) : Exception(message, cause) {
    @DartName("message")
    constructor(message: String?) : this(message, null)

    @DartName("cause")
    constructor(cause: Throwable?) : this(cause?.toString(), cause)

    @DartName("empty")
    constructor() : this(null, null)
}

@DartCatchAs<dart.core.ArgumentError>
open class IllegalArgumentException(
    override val message: String?,
    override val cause: Throwable?,
    override val stackTrace: StackTrace? = StackTrace.CURRENT,
    override val invalidValue: dynamic = null,
    override val name: String? = null,
) : RuntimeException(message, cause), dart.core.ArgumentError {
    @DartName("message")
    constructor(message: String?) : this(message = message, cause = null)

    @DartName("cause")
    constructor(cause: Throwable?) : this(cause?.toString(), cause)

    @DartName("empty")
    constructor() : this(message = null, cause = null)

    @DartName("from")
    constructor(error: dart.core.ArgumentError) : this(
        message = error.message,
        cause = null,
        stackTrace = error.stackTrace,
        invalidValue = error.invalidValue,
        name = error.name,
    )
}

@DartCatchAs<dart.core.StateError>
open class IllegalStateException(
    message: String?,
    override val cause: Throwable?,
    override val stackTrace: StackTrace? = StackTrace.CURRENT,
) : RuntimeException(message, cause), dart.core.StateError {
    @DartName("message")
    constructor(message: String?) : this(message, null)

    @DartName("cause")
    constructor(cause: Throwable?) : this(cause?.toString(), cause)

    @DartName("empty")
    constructor() : this(null, null)

    @DartName("from")
    constructor(error: dart.core.StateError) : this(
        message = error.message,
        cause = null,
        stackTrace = error.stackTrace,
    )

    override val message: String = message ?: messageFallback
}

@DartCatchAs<dart.core.IndexError>
open class IndexOutOfBoundsException(
    override val message: String?,
    override val stackTrace: StackTrace? = StackTrace.CURRENT,
    override val invalidValue: dynamic = null,
    override val name: String? = null,
    override val indexable: dynamic = null,
    override val length: Int = -1
) : RuntimeException(message, cause = null), dart.core.IndexError {
    @DartName("empty")
    constructor() : this(null)

    @DartName("from")
    constructor(error: dart.core.IndexError) : this(
        message = error.message?.toString(),
        stackTrace = error.stackTrace,
        invalidValue = error.invalidValue,
        name = error.name,
        indexable = error.indexable,
        length = error.length
    )
}

@DartCatchAs<dart.core.ConcurrentModificationError>
open class ConcurrentModificationException(
    override val message: String?,
    override val cause: Throwable?,
    override val stackTrace: StackTrace? = StackTrace.CURRENT,
    override val modifiedObject: Any? = null
) :
    RuntimeException(message, cause), dart.core.ConcurrentModificationError {
    @DartName("message")
    constructor(message: String?) : this(message, null)

    @DartName("cause")
    constructor(cause: Throwable?) : this(cause?.toString(), cause)

    @DartName("empty")
    constructor() : this(null, null)

    @DartName("from")
    constructor(error: dart.core.ConcurrentModificationError) : this(
        message = null,
        cause = null,
        stackTrace = error.stackTrace,
        modifiedObject = error.modifiedObject,
    )
}

@DartCatchAs<dart.core.UnsupportedError>
open class UnsupportedOperationException(
    override val message: String?,
    override val cause: Throwable?,
    override val stackTrace: StackTrace? = StackTrace.CURRENT,
) :
    RuntimeException(message, cause), dart.core.UnsupportedError {
    @DartName("message")
    constructor(message: String?) : this(message, null)

    @DartName("cause")
    constructor(cause: Throwable?) : this(cause?.toString(), cause)

    @DartName("empty")
    constructor() : this(null, null)

    @DartName("from")
    constructor(error: dart.core.UnsupportedError) : this(
        message = error.message,
        cause = null,
        stackTrace = error.stackTrace
    )
}

@DartCatchAs<dart.core.FormatException>
open class NumberFormatException(
    message: String?,
    override val source: dynamic = null,
    override val offset: Int? = null,
    override val stackTrace: StackTrace? = StackTrace.CURRENT,
) : IllegalArgumentException(message, cause = null), dart.core.FormatException {
    @DartName("empty")
    constructor() : this(null)

    @DartName("from")
    constructor(error: dart.core.FormatException) : this(
        message = error.message,
        source = error.source,
        offset = error.offset
    )

    override val message: String = message ?: messageFallback
}

@DartCatchAs<dart.core.TypeError>
open class NullPointerException(
    override val message: String?,
    override val stackTrace: StackTrace? = StackTrace.CURRENT,
) : RuntimeException(message, cause = null),
    dart.core.TypeError {
    @DartName("empty")
    constructor() : this(null)

    @DartName("from")
    constructor(error: dart.core.TypeError) : this(
        message = null,
        stackTrace = error.stackTrace,
    )
}

@DartCatchAs<dart.core.TypeError>
open class ClassCastException(
    override val message: String?,
    override val stackTrace: StackTrace? = StackTrace.CURRENT,
) : RuntimeException(message, cause = null), dart.core.TypeError {
    @DartName("empty")
    constructor() : this(null)

    @DartName("from")
    constructor(error: dart.core.TypeError) : this(
        message = null,
        stackTrace = error.stackTrace,
    )
}

@DartCatchAs<dart.core.AssertionError>
open class AssertionError(
    override val message: String?,
    override val stackTrace: StackTrace? = StackTrace.CURRENT,
) : Error(message, cause = null), dart.core.AssertionError {
    @DartName("message")
    constructor(message: Any?) : this(message?.toString())

    @DartName("empty")
    constructor() : this(null)

    @DartName("from")
    constructor(error: dart.core.AssertionError) : this(
        message = error.message?.toString(),
        stackTrace = error.stackTrace,
    )
}

open class NoSuchElementException(override val message: String?) : RuntimeException(message, cause = null) {
    @DartName("empty")
    constructor() : this(null)
}

//@DartCatchAs<dart.core.IntegerDivisionByZeroException> TODO
@SinceKotlin("1.3")
open class ArithmeticException(message: String?) : RuntimeException(message, cause = null),
    dart.core.IntegerDivisionByZeroException {
    @DartName("empty")
    constructor() : this(null)

    override val message = message ?: super<dart.core.IntegerDivisionByZeroException>.message
}

private val messageFallback = "<unspecified>"

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
fun Throwable.stackTraceToString(): String = "" // TODO

/**
 * Prints the [detailed description][Throwable.stackTraceToString] of this throwable to the standard output or standard error output.
 */
@SinceKotlin("1.4")
fun Throwable.printStackTrace(): Unit {} // TODO

/**
 * When supported by the platform, adds the specified exception to the list of exceptions that were
 * suppressed in order to deliver this exception.
 */
@SinceKotlin("1.4")
fun Throwable.addSuppressed(exception: Throwable) {}

/**
 * Returns a list of all exceptions that were suppressed in order to deliver this exception.
 *
 * The list can be empty:
 * - if no exceptions were suppressed;
 * - if the platform doesn't support suppressed exceptions;
 * - if this [Throwable] instance has disabled the suppression.
 */
@SinceKotlin("1.4")
val Throwable.suppressedExceptions: List<Throwable>
    get() = dart("<Throwable>[]") // TODO?