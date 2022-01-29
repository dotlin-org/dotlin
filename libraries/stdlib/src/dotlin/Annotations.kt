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
 * Specifies that the constructor or constructor call should be `const` in Dart.
 */
@Target(AnnotationTarget.CONSTRUCTOR, AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class DartConst

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
 * Specifies that the class is the implementation of the corresponding interface in Dart.
 *
 * This annotation is used for Kotlin headers that were generated from Dart code. In Dart, it's possible to either
 * extend or implement (almost) any class. Since this might be part of the API, it should also be possible to choose
 * whether to implement a Dart class like an interface or extend it like a class. That's why generated Dart classes
 * have a nested class `Impl`. It compiles to the same class in Dart, but can be used to specify that you want to
 * _extend_ the class instead of _implement_ it.
 *
 * This annotation is used on those `Impl` classes.
 *
 * Only allowed on `external` classes.
 *
 * The annotated class cannot have a `companion object` itself. The companion object of the corresponding interface is
 * used, if any.
 *
 * @param fqName The fully qualified name of the interface this class is an implementation of.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DartImplementationOf(val fqName: String)

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
 * Specifies that whenever this declaration is referenced, `dart:core` will be imported with a `hide` with the same
 * name as this declaration. For example, if this annotation is used on a class named `Foo`, the generated import
 * will be `import 'dart:core' hide Foo;`. This can be used to prevent name clashes.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class DartHideNameFromCore()