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
    "NON_ABSTRACT_FUNCTION_WITH_NO_BODY",
    "MUST_BE_INITIALIZED_OR_BE_ABSTRACT"
)

package dart.core

/**
 * An interface implemented by all stack trace objects.
 *
 * A [StackTrace] is intended to convey information to the user about the call
 * sequence that triggered an exception.
 *
 * These objects are created by the runtime, it is not possible to create
 * them programmatically.
 */
external interface StackTrace {
    companion object {
        /**
         * A stack trace object with no information.
         *
         * This stack trace is used as the default in situations where
         * a stack trace is required, but the user has not supplied one.
         */
        @DartStatic
        @DartName("empty")
        external val EMPTY: StackTrace = definedExternally

        /**
         * Returns a representation of the current stack trace.
         */
        @DartStatic
        @DartName("current")
        external val CURRENT: StackTrace = definedExternally
    }

    /**
     * Returns a [String] representation of the stack trace.
     *
     * The string represents the full stack trace starting from
     * the point where a throw occurred to the top of the current call sequence.
     *
     * The exact format of the string representation is not final.
     */
    override fun toString(): String
}