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
 * Specifies that the annotated element should not be compiled as a declaration on its own, since a declaration
 * already is already defined.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
internal annotation class DartBuiltIn {
    /**
     * Specifies that the annotated method is a getter in Dart.
     *
     * Can only be used on methods with no parameters and a return type that's not [Unit].
     *
     * Applies to any overrides in subtypes as well.
     */
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Getter

    /**
     * Specifies that whenever this declaration is referenced it should do so with a certain alias. This can be used
     * to circumvent name conflicts with existing Dart names.
     *
     * @param library The library the declaration should be imported from.
     * Should be a full import string, e.g. `dart:core`.
     */
    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.FUNCTION,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ImportAlias(val library: String)

    /**
     * Specifies that whenever this declaration is referenced a declaration from [library] should be hidden. This can
     * be used to circumvent name conflicts with existing Dart names.
     *
     * Cannot be used together with [ImportAlias].
     *
     * @param library The library the declaration should be hidden from.
     * Should be a full import string, e.g. `dart:core`.
     */
    @Target(
        AnnotationTarget.CLASS,
        AnnotationTarget.FUNCTION,
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class HideImport(val library: String)
}
