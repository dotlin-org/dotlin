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

package dotlin

/**
 * Specifies that the annotated method will be a getter in Dart.
 *
 * Can only be used on methods with no parameters and a return type that's not [Unit].
 *
 * Applies to any overrides in subtypes as well.
 *
 * **Note:** This annotation should not be used in general. This annotation exists purely for
 * standard library compilation.
 */
@Target(AnnotationTarget.FUNCTION)
internal annotation class DartGetter

/**
 * Specifies that the annotated element is Dart built-in and should not be compiled.
 *
 * **Note:** This annotation should not be used in general. This annotation exists purely for
 * standard library compilation.
 */
@Target(AnnotationTarget.CLASS)
internal annotation class DartBuiltIn