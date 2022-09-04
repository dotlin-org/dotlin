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

package kotlin.ranges

/**
 * An iterator over a progression of values of type `Int`.
 * @property step the number by which the value is incremented on each step.
 */
internal class IntProgressionIterator(first: Int, last: Int, val step: Int) : kotlin.collections.Iterator<Int> {
    private val finalElement: Int = last
    private var hasNext: Boolean = if (step > 0) first <= last else first >= last
    private var next: Int = if (hasNext) first else finalElement

    override fun hasNext(): Boolean = hasNext

    override fun next(): Int {
        val value = next
        if (value == finalElement) {
            if (!hasNext) throw StateError("No such element")
            hasNext = false
        }
        else {
            next += step
        }
        return value
    }
}