/*
 * Copyright 2010-2019 JetBrains s.r.o.
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

package kotlin.ranges

/**
 * Represents a range of values (for example, numbers or characters).
 * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/ranges.html) for more information.
 */
interface ClosedRange<T: Comparable<T>> {
    /**
     * The minimum value in the range.
     */
    val start: T

    /**
     * The maximum value in the range (inclusive).
     */
    val endInclusive: T

    /**
     * Checks whether the specified [value] belongs to the range.
     */
    operator fun contains(value: T): Boolean = value >= start && value <= endInclusive

    /**
     * Checks whether the range is empty.
     *
     * The range is empty if its start value is greater than the end value.
     */
    fun isEmpty(): Boolean = start > endInclusive
}
