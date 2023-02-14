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

import kotlin.annotation.AnnotationTarget.*
import kotlin.annotation.AnnotationRetention.*

/**
 * Specifies that the annotated method is a getter in Dart.
 *
 * Can only be used on methods with no parameters and a return type that's not [Unit].
 *
 * Applies to any overrides in subtypes as well.
 */
@Target(FUNCTION)
@Retention(SOURCE)
internal annotation class DartGetter

/**
 * Specifies that the annotated method or property is an extension in Dart.
 *
 * Applies to any overrides in subtypes as well.
 *
 * If used on an `abstract` member, the member must be `external`.
 */
@Target(
    FUNCTION,
    PROPERTY
)
@Retention(SOURCE)
internal annotation class DartExtension

/**
 * Specifies that whenever this declaration is referenced, [library] should be imported in the file this declaration
 * was referenced in. Must only be used on `external` declarations.
 *
 * @param library The library the declaration should be imported from.
 *                Should be a full import string, e.g. `dart:core` or `package:transmogrify/transmogrify.dart`.
 */
@Target(CLASS, FUNCTION, FILE)
@Retention(SOURCE)
annotation class DartLibrary(val library: String)