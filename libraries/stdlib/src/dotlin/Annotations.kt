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
 * Specifies the name to use in Dart for the annotated element.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FILE
)
annotation class DartName(val name: String)

/**
 * Specifies that the annotated method will be a getter in Dart.
 *
 * Can only be used on methods with no parameters and a return type that's not [Unit].
 *
 * **Note:** This annotation should not be used in general. This annotation exists purely for
 * standard library compilation.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class DartGetter