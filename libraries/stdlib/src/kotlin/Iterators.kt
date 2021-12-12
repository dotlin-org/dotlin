/*
 * Copyright 2010-2021 JetBrains s.r.o.
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

package kotlin.collections

/** An iterator over a sequence of values of type `Byte`. */
abstract class ByteIterator : Iterator<Byte> {
    override final fun next() = nextByte()

    /** Returns the next value in the sequence without boxing. */
    abstract fun nextByte(): Byte
}

/** An iterator over a sequence of values of type `Char`. */
abstract class CharIterator : Iterator<Char> {
    override final fun next() = nextChar()

    /** Returns the next value in the sequence without boxing. */
    abstract fun nextChar(): Char
}

/** An iterator over a sequence of values of type `Short`. */
abstract class ShortIterator : Iterator<Short> {
    override final fun next() = nextShort()

    /** Returns the next value in the sequence without boxing. */
    abstract fun nextShort(): Short
}

/** An iterator over a sequence of values of type `Int`. */
abstract class IntIterator : Iterator<Int> {
    override final fun next() = nextInt()

    /** Returns the next value in the sequence without boxing. */
    abstract fun nextInt(): Int
}

/** An iterator over a sequence of values of type `Long`. */
abstract class LongIterator : Iterator<Long> {
    override final fun next() = nextLong()

    /** Returns the next value in the sequence without boxing. */
    abstract fun nextLong(): Long
}

/** An iterator over a sequence of values of type `Float`. */
abstract class FloatIterator : Iterator<Float> {
    override final fun next() = nextFloat()

    /** Returns the next value in the sequence without boxing. */
    abstract fun nextFloat(): Float
}

/** An iterator over a sequence of values of type `Double`. */
abstract class DoubleIterator : Iterator<Double> {
    override final fun next() = nextDouble()

    /** Returns the next value in the sequence without boxing. */
    abstract fun nextDouble(): Double
}

/** An iterator over a sequence of values of type `Boolean`. */
abstract class BooleanIterator : Iterator<Boolean> {
    override final fun next() = nextBoolean()

    /** Returns the next value in the sequence without boxing. */
    abstract fun nextBoolean(): Boolean
}

