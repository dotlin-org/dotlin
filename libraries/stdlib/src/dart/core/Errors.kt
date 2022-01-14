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

@file:Suppress(
    "WRONG_BODY_OF_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
    "NON_ABSTRACT_MEMBER_OF_EXTERNAL_INTERFACE" // TODO: Fix in analyzer
)

package dart.core

/**
 * Error objects thrown in the case of a program failure.
 *
 * An `Error` object represents a program failure that the programmer
 * should have avoided.
 *
 * Examples include calling a function with invalid arguments,
 * or even with the wrong number of arguments,
 * or calling it at a time when it is not allowed.
 *
 * These are not errors that a caller should expect or catch -
 * if they occur, the program is erroneous,
 * and terminating the program may be the safest response.
 *
 * When deciding that a function throws an error,
 * the conditions where it happens should be clearly described,
 * and they should be detectable and predictable,
 * so the programmer using the function can avoid triggering the error.
 *
 * Such descriptions often uses words like
 * "must" or "must not" to describe the condition,
 * and if you see words like that in a function's documentation,
 * then not satisfying the requirement
 * is very likely to cause an error to be thrown.
 *
 * Example (from [String.contains]):
 * ```plaintext
 * `startIndex` must not be negative or greater than `length`.
 * ```
 * In this case, an error will be thrown if `startIndex` is negative
 * or too large.
 *
 * If the conditions are not detectable before calling a function,
 * the called function should not throw an `Error`.
 * It may still throw,
 * but the caller will have to catch the thrown value,
 * effectively making it an alternative result rather than an error.
 * The thrown object can choose to implement [Exception]
 * to document that it represents an exceptional, but not erroneous,
 * occurrence, but being an [Exception] has no other effect
 * than documentation.
 */
@DartLibrary("dart:core", aliased = true)
external sealed interface Error {
    /**
     * The stack trace at the point where this error was first thrown.
     */
    val stackTrace: StackTrace?
}

/**
 * Error thrown by the runtime system when an assert statement fails.
 */
@DartLibrary("dart:core", aliased = true)
external sealed interface AssertionError : Error {
    /**
     * Message describing the assertion error.
     */
    val message: Any?
}

/**
 * Error thrown by the runtime system when a dynamic type error happens.
 */
external sealed interface TypeError : Error

/**
 * Error thrown when a function is passed an unacceptable argument.
 */
external sealed interface ArgumentError : Error {
    /**
     * The invalid value.
     */
    val invalidValue: dynamic

    /**
     * Name of the invalid argument, if available.
     */
    val name: String?

    /**
     * Message describing the problem.
     */
    val message: dynamic
}

/**
 * Error thrown due to a value being outside a valid range.
 */
external sealed interface RangeError : ArgumentError {
    /**
     * The minimum value that [value] is allowed to assume.
     */
    val start: Number?

    /**
     * The maximum value that [value] is allowed to assume.
     */
    val end: Number?
}

external sealed interface IndexError : RangeError {
    val indexable: dynamic

    val length: Int

    override val start: Int
        get() = 0

    override val end: Int
        get() = length - 1
}

external sealed interface UnsupportedError : Error {
    val message: String?
}

/**
 * The operation was not allowed by the current state of the object.
 *
 * Should be used when this particular object is currently in a state
 * which doesn't support the requested operation, but other similar
 * objects might, or the object might change its state to one which
 * supports the operation.
 *
 * If the operation is never supported, consider using
 * [UnsupportedError] instead.
 *
 * This is a generic error used for a variety of different erroneous
 * actions. The message should be descriptive.
 */
external sealed interface StateError : Error {
    val message: String
}

/**
 * Error occurring when a collection is modified during iteration.
 *
 * Some modifications may be allowed for some collections, so each collection
 * ([Iterable] or similar collection of values) should declare which operations
 * are allowed during an iteration.
 */
external sealed interface ConcurrentModificationError : Error {
    /**
     * The object that was modified in an incompatible way.
     */
    val modifiedObject: Any?
}