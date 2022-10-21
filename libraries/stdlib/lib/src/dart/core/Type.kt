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

package dart.core

/**
 * Runtime representation of a type.
 *
 * Type objects represent types.
 * A type object can be created in several ways:
 *  * By a *type literal*, a type name occurring as an expression,
 * like `Type type = int;`,
 * or a type variable occurring as an expression, like `Type type = T;`.
 *  * By reading the run-time type of an object,
 * like `Type type = o.runtimeType;`.
 *  * Through `dart:mirrors`.
 *
 * A type object is intended as an entry point for using `dart:mirrors`.
 * The only operations supported are comparing to other type objects
 * for equality, and converting it to a string for debugging.
 */
external abstract class Type {
    /**
     * A hash code for the type which is compatible with [operator==].
     */
    override fun hashCode(): Int

    /**
     * Whether [other] is a [Type] instance representing an equivalent type.
     *
     * The language specification dictates which types are considered
     * to be the equivalent.
     * If two types are equivalent, it's guaranteed that they are subtypes
     * of each other,
     * but there are also types which are subtypes of each other,
     * and which are not equivalent (for example `dynamic` and `void`,
     * or `FutureOr<Object>` and `Object`).
     */
    override operator fun equals(other: Any?): Boolean

    /**
     * Returns a string which represents the underlying type.
     *
     * The string is only intended for providing information to a reader
     * while debugging.
     * There is no guaranteed format,
     * the string value returned for a [Type] instances is entirely
     * implementation dependent.
     *
     * The string should be consistent, so a `Type` object for the *same* type
     * returns the same string throughout a program execution.
     * The string may or may not contain parts corresponding to the
     * source name of declaration of the type, if the type has a source name
     * at all (some types reachable through `dart:mirrors` may not).
     */
    override fun toString(): String
}