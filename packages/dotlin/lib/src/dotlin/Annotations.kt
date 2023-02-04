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
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPEALIAS,
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
 * Specifies the index of a value parameter in Dart.
 *
 * For example, the following code:
 * ```kotlin
 * fun process(@DartIndex(1) firstParameter: Int, secondParameter: Int) {}
 * ```
 * Compiles to:
 * ```dart
 * void process(int secondParameter, int firstParameter) {}
 * ```
 *
 * This can be useful to move lambda parameters to the end, to make use of Kotlin's
 * lambda expression syntax.
 *
 * Also applies to parameters whose overridden paramter equivalents have this annotation.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class DartIndex(val index: Int)

/**
 * Specifies that the default value of this parameter is different in Dart.
 *
 * For example, the following code:
 * ```kotlin
 * external fun process(@DartDifferentDefaultValue firstParameter: Int = 2) {}
 *
 * fun main() {
 *     process()
 * }
 * ```
 * Compiles to:
 * ```dart
 * void main() {
 *   process(firstParameter: 2)
 * }
 * ```
 *
 * As you can see, the default value is added explicitely when calling the function.
 *
 * Can only be used on value parameters of `external` functions.
 *
 * Also applies to parameters whose overridden paramter equivalents have this annotation.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class DartDifferentDefaultValue

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

/**
 * Specifies that the function is a constructor in Dart.
 *
 * Can only be used on external companion object methods.
 */
@Target(AnnotationTarget.FUNCTION,)
@Retention(AnnotationRetention.SOURCE)
annotation class DartConstructor