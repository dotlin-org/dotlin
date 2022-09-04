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

@file:Suppress("INAPPLICABLE_LATEINIT_MODIFIER") // TODO: Fix in analyzer

package dotlin

internal open class ExternalIterator<T>(open val delegate: dart.core.Iterator<T>) : kotlin.collections.Iterator<T>, dart.core.Iterator<T> {
    protected lateinit var currentValue: T
    private lateinit var nextValue: T

    private var hasNextValue = false

    init {
        peekNext(isInitial = true)
    }

    private fun peekNext(isInitial: Boolean = false) {
        if (!isInitial) {
            currentValue = nextValue
        }

        // Not lifted yet since that's technically slower. TODO: Fix that.
        @Suppress("LiftReturnOrAssignment")
        when (delegate.moveNext()) {
            true -> {
                nextValue = delegate.current
                hasNextValue = true
            }
            else -> hasNextValue = false
        }
    }

    override fun hasNext() = hasNextValue

    override fun next(): T {
        peekNext()
        return current
    }

    override val current: T
        get() = try {
            currentValue
        } catch (t: dynamic) {
            throw StateError("No such element")
        }

    override fun moveNext(): Boolean {
        peekNext()
        return hasNextValue
    }
}

internal open class ExternalBidirectionalIterator<T>(override val delegate: dart.core.BidirectionalIterator<T>) :
    ExternalIterator<T>(delegate), kotlin.collections.BidirectionalIterator<T>, dart.core.BidirectionalIterator<T> {

    private lateinit var previousValue: T
    private var hasPreviousValue = false

    init {
        peekPrevious(isInitial = true)
    }

    private fun peekPrevious(isInitial: Boolean = false) {
        if (!isInitial) {
            currentValue = previousValue
        }

        // Not lifted yet since that's technically slower. TODO: Fix that.
        @Suppress("LiftReturnOrAssignment")
        when (delegate.movePrevious()) {
            true -> {
                previousValue = delegate.current
                hasPreviousValue = true
            }
            else -> hasPreviousValue = false
        }
    }

    override fun hasPrevious() = hasPreviousValue

    override fun previous(): T {
        peekPrevious()
        return current
    }

    override fun movePrevious(): Boolean {
        peekPrevious()
        return hasPreviousValue
    }
}