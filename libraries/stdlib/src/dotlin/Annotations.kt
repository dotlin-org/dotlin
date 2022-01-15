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