/*
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

@file:Suppress("WRONG_BODY_OF_EXTERNAL_DECLARATION") // TODO: Fix in analyzer

package dart.core

@DartImportAlias("dart:core")
external sealed interface Exception

/**
 * Exception thrown when a string or some other data does not have an expected
 * format and cannot be parsed or processed.
 */
external sealed interface FormatException : Exception {
    /**
     * A message describing the format error.
     */
    val message: String

    /**
     * The actual source input which caused the error.
     *
     * This is usually a [String], but can be other types too.
     * If it is a string, parts of it may be included in the [toString] message.
     *
     * The source is `null` if omitted or unknown.
     */
    val source: dynamic

    /**
     * The offset in [source] where the error was detected.
     *
     * A zero-based offset into the source that marks the format error causing
     * this exception to be created. If `source` is a string, this should be a
     * string index in the range `0 <= offset <= source.length`.
     *
     * If input is a string, the [toString] method may represent this offset as
     * a line and character position. The offset should be inside the string,
     * or at the end of the string.
     *
     * May be omitted. If present, [source] should also be present if possible.
     */
    val offset: Int?
}

@Deprecated("Use UnsupportedError instead")
external sealed interface IntegerDivisionByZeroException : Exception, UnsupportedError {
    override val message: String?
        get() = "Division resulted in non-finite value"

    override val stackTrace: StackTrace?
        get() = null
}