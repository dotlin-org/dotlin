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

@file:Suppress("EXTERNAL_TYPE_EXTENDS_NON_EXTERNAL_TYPE")

package kotlin.collections

internal external interface ByteIterator : Iterator<Byte>
internal external interface CharIterator : Iterator<Char>
internal external interface ShortIterator : Iterator<Short>
internal external interface IntIterator : Iterator<Int>
internal external interface LongIterator : Iterator<Long>
internal external interface FloatIterator : Iterator<Float>
internal external interface DoubleIterator : Iterator<Double>
internal external interface BooleanIterator : Iterator<Boolean>