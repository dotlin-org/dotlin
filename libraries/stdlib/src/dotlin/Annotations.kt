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
 * Specifies the name to use in Dart for the annotated element.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FILE
)
@Retention(AnnotationRetention.SOURCE)
annotation class DartName(val name: String)

/**
 * Specifies that the constructor call should be `const` in Dart. Only works on `const constructor`s.
 *
 * For defining a `const constructor`, you can use the keyword itself, for example:
 * ```kotlin
 * class Example const constructor()
 * ```
 *
 * Only when _invoking_ a `const constructor` must you use the annotation `@const`. For example:
 * ```
 * val myExample = @const Example()
 * ```
 */
@Target(AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class const

/**
 * Specifies that the parameters with default values of this function (or constructor)
 * are positional instead of named in Dart.
 *
 * Also applies to methods that override this method.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
annotation class DartPositional

/**
 * Specifies that whenever this declaration is referenced, [library] should be imported in the file this declaration
 * was referenced in. Can only be used on `external` declarations.
 *
 * @param library The library the declaration should be imported from.
 *                Should be a full import string, e.g. `dart:core` or `package:transmogrify/transmogrify.dart`.
 * @param aliased Whether the library should be imported with an alias. This means that whenever the declartion is
 *                referenced, it will do so with a library prefix. The alias is defined by the compiler based on
 *                [library]. This can be used to prevent name clashes.
 * @param hidden Whether the name of this declaration should be hidden from the import. Can be used to prevent name
 *               clashes.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class DartLibrary(val library: String, val aliased: Boolean = false)

/**
 * Specifies that whenever a companion object member is referenced, it will do so statically in Dart.
 *
 * This is meant for external companion object or their members for external classes, to call Dart static code.
 *
 * Remember that by default companion objects and their members are _not_ external, even if inside `external` classes.
 *  If the companion object is not
 * `external` itself and this annotation is used on a member of that object, the member must be explicitely marked
 * `external`.
 *
 * For example, if in Dart there's a static method `Foo.bar()`, the Kotlin external declaration would look like so:
 *
 * ```kotlin
 * external class Foo {
 *     companion object {
 *         @DartStatic
 *         external fun bar() { .. }
 *     }
 * }
 * ```
 *
 * A call to `Foo.bar()` in Kotlin would then translate to `Foo.bar()` in Dart (as opposed to `Foo.$instance.$bar()`).
 *
 * It's also possbile to to annotate the companion object itself with `@DartStatic`. Then it behaves the same as if
 * every member of the companion object is annotated with `@DartStatic`.
 *
 * Not to be confused with how `@JvmStatic` works, which generates a static member. In Dotlin static members are always
 * generated.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class DartStatic

// TODO: Error for external companion object (or member) without a @DartStatic annotation, atleast
// until Dart singleton pattern is supported.

/**
 * Specifies the Dart extension container name for this declaration.
 *
 * In Dart, extensions live in an extension container. By default in Dotlin, names for these are generated using the
 * type on which the extension is on, _and_ a hash based on the file location. The hash is necessary when exporting
 * extensions, since if there are multiple extensions on the same types but in different files, the extension names
 * would clash without a hash.
 *
 * Since the extension container name is considered part of the API, it's recommend to use this annotation for all
 * public extensions in a public (published) package, to prevent the name changing when renaming or
 * moving the containing file.
 *
 * It's also recommended to use this annotation when mixing Dart and Kotlin code, for the same reason.
 *
 * If applied to a file, it behaves the same as if all extension methods/properties were annotated individually.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
annotation class DartExtensionName(val name: String)