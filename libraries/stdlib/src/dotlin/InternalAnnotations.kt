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
 * Specifies that the annotated method is a getter in Dart.
 *
 * Can only be used on methods with no parameters and a return type that's not [Unit].
 *
 * Applies to any overrides in subtypes as well.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
internal annotation class DartGetter

/**
 * Specifies that the annotated method or property is an extension in Dart.
 *
 * Applies to any overrides in subtypes as well.
 *
 * If used on an `abstract` member, the member must be `external`.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY
)
@Retention(AnnotationRetention.SOURCE)
internal annotation class DartExtension

/**
 * Specifies that whenever this declaration is referenced, `dart:core` will be imported with a `hide` with the same
 * name as this declaration. For example, if this annotation is used on a class named `Foo`, the generated import
 * will be `import 'dart:core' hide Foo;`. This can be used to prevent name clashes.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
internal annotation class DartHideNameFromCore

/**
 * Specifies that whenever this type is used in a catch clause, it should be caught as the type [T] instead.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
internal annotation class DartCatchAs<T>