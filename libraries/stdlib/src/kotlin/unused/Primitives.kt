/*
 * Copyright 2010-2021 JetBrains s.r.o.
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

@file:Suppress(
    "NON_ABSTRACT_FUNCTION_WITH_NO_BODY",
    "MUST_BE_INITIALIZED_OR_BE_ABSTRACT",
    "UNUSED_PARAMETER",
    "DIVISION_BY_ZERO",
    "INAPPLICABLE_OPERATOR_MODIFIER"
)

package kotlin

@DartLibrary("dart:core")
@DartName("int")
internal external abstract class Byte private constructor() : Number()

@DartLibrary("dart:core")
@DartName("int")
internal external abstract class Short private constructor() : Number()

@DartLibrary("dart:core")
@DartName("double")
internal external abstract class Float private constructor() : Number()