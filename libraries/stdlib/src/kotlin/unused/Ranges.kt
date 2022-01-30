/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
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

@file:Suppress("EXTERNAL_TYPE_EXTENDS_NON_EXTERNAL_TYPE", "MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED")

package kotlin.ranges

/**
 * A range of values of type `Char`.
 */
internal external interface CharRange : CharProgression, ClosedRange<Char>

/**
 * A range of values of type `Long`.
 */
internal external interface LongRange : LongProgression, ClosedRange<Long>