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
annotation class DartConst()

/**
 * Specifies that the parameters with default values of this function (or constructor)
 * are positional instead of named in Dart.
 *
 * Also applies to methods that override this method.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
annotation class DartPositional()