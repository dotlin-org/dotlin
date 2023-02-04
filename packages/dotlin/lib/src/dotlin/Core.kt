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

package dotlin

/**
 * Puts Dart code as-is into the calling function.
 *
 * The [code] `String` must be a compile-time constant.
 */
external fun dart(code: String): dynamic

external val definedExternally: Nothing

/**
 * Returns a [Type] instance of the given type [T].
 *
 * Behaves exactly like a type literal in Dart. For example,
 * the call of
 * ```kotlin
 * typeOf<String>()
 * ```
 * translates directly to
 * ```dart
 * String
 * ```
 */
inline external fun <T> typeOf(): Type = definedExternally

/**
 * Marker interface to indicate that a class can either implement the implicit interface or be mixed in.
 *
 * @see Interface
 * @see Mixin
 */
sealed interface InterfaceOrMixin

/**
 * Marker object to indicate that the implicit interface from a Dart class should be implemented, instead of
 * the class itself.
 *
 * In Dart, every class has an implicit interface, meaning you can `implement` (as opposed to `extend`) any class.
 * To do the same in Dotlin, you can use a special constructor to indicate that the implicit interface should be used
 * as a super type, instead of the class. For example:
 * ```kotlin
 * class MyClass : SomeDartClass(Interface)
 * ```
 */
object Interface : InterfaceOrMixin

/**
 * Marker object to indicate that a Dart class should be used as a mixin, instead of inherited as a class.
 *
 * In Dart, certain classes can be used as a `mixin`, meaning you can implement them like interfaces, but with
 * behavior (similar to Kotlin interfaces with default implementations).
 *
 * To do the same in Dotlin, you can use a special constructor to indicate that a class should be mixed in as
 * a super type, instead of inherited normally. For example:
 * ```kotlin
 * class MyClass : SomeDartClass(Mixin)
 * ```
 */
object Mixin : InterfaceOrMixin