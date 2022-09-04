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
    "NON_ABSTRACT_MEMBER_OF_EXTERNAL_INTERFACE", // TODO: Fix in analyzer
    "NESTED_CLASS_IN_EXTERNAL_INTERFACE", // TODO: Fix in analyzer
    "WRONG_INITIALIZER_OF_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
    "WRONG_BODY_OF_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
    "NESTED_EXTERNAL_DECLARATION", // TODO: Fix in analyzer
    "WRONG_DEFAULT_VALUE_FOR_EXTERNAL_FUN_PARAMETER", // TODO: Fix in analyzer
    "EXTERNAL_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER", // TODO: Fix in analyzer
    "EXTERNAL_DELEGATED_CONSTRUCTOR_CALL" // TODO: Fix in analyzer
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
@DartLibrary("dart:core")
external open class Error {
    /**
     * The stack trace at the point where this error was first thrown.
     */
    val stackTrace: StackTrace? = definedExternally
}

/**
 * Error thrown by the runtime system when an assert statement fails.
 *
 * @constructor Creates an assertion error with the provided [message].
 */
@DartLibrary("dart:core")
external class AssertionError @DartPositional constructor(
    /**
     * Message describing the assertion error.
     */
    val message: Any? = null
) : Error

/**
 * Error thrown by the runtime system when a dynamic type error happens.
 */
external class TypeError : Error

/**
 * Error thrown when a function is passed an unacceptable argument.
 */
external open class ArgumentError @DartPositional constructor(
    /**
     * Message describing the problem.
     */
    val message: dynamic = null,
    /**
     * Name of the invalid argument, if available.
     */
    val name: String? = null
) : Error {
    /**
     * The invalid value.
     */
    val invalidValue: dynamic

    /**
     * Creates error containing the invalid [value].
     *
     * A message is built by suffixing the [message] argument with
     * the [name] argument (if provided) and the value. Example:
     * ```plaintext
     * Invalid argument (foo): null
     * ```
     * The `name` should match the argument name of the function, but if
     * the function is a method implementing an interface, and its argument
     * names differ from the interface, it might be more useful to use the
     * interface method's argument name (or just rename arguments to match).
     */
    @DartName("value")
    @DartPositional
    constructor(invalidValue: dynamic, name: String? = null, message: dynamic = null)
}

/**
 * Error thrown due to a value being outside a valid range.
 */
external open class RangeError(
    // TODO: Add all properties
    /**
     * The minimum value that [value] is allowed to assume.
     */
    open val start: Number?,

    /**
     * The maximum value that [value] is allowed to assume.
     */
    open val end: Number?
) : ArgumentError(invalidValue = null, name = "null", message = null)

external open class IndexError @DartPositional constructor(
    // TODO: Add all propreties
    val indexable: dynamic,
    val length: Int? = null
) : RangeError {
    override val start: Int = definedExternally
    override val end: Int = definedExternally
}

external open class UnsupportedError(val message: String?) : Error

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
external open class StateError(val message: String) : Error

/**
 * Error occurring when a collection is modified during iteration.
 *
 * Some modifications may be allowed for some collections, so each collection
 * ([Iterable] or similar collection of values) should declare which operations
 * are allowed during an iteration.
 */
external open class ConcurrentModificationError @DartPositional constructor(
    /**
     * The object that was modified in an incompatible way.
     */
    val modifiedObject: Any? = null
) : Error

/**
 * Thrown by operations that have not been implemented yet.
 *
 * This [Error] is thrown by unfinished code that hasn't yet implemented
 * all the features it needs.
 *
 * If the class does not intend to implement the feature, it should throw
 * an [UnsupportedError] instead. This error is only intended for
 * use during development.
 */
external open class UnimplementedError @DartPositional constructor(val message: String? = null) :
    Error