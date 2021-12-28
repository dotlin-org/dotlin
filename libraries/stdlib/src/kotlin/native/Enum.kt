/*
 * Copyright 2010-2019 JetBrains s.r.o.
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

@file:Suppress("NON_ABSTRACT_FUNCTION_WITH_NO_BODY", "MUST_BE_INITIALIZED_OR_BE_ABSTRACT", "UNUSED_PARAMETER")

package kotlin

/**
 * The common base class of all enum classes.
 * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/enum-classes.html) for more
 * information on enum classes.
 */
@DartHideImport("dart:core")
abstract class Enum<E : Enum<E>> @DartConst constructor(
    /**
     * Returns the name of this enum constant, exactly as declared in its enum declaration.
     */
    final val name: String,
    /**
     * Returns the ordinal of this enumeration constant (its position in its enum declaration, where the initial constant
     * is assigned an ordinal of zero).
     */
    final val ordinal: Int
): Comparable<E> {
    companion object {}

    override final fun compareTo(other: E) = ordinal.compareTo(other.ordinal)

    /**
     * Throws an exception since enum constants cannot be cloned.
     * This method prevents enum classes from inheriting from `Cloneable`.
     */
    protected final fun clone(): Any

    override final fun equals(other: Any?) = this === other

    override final fun hashCode(): Int
    override fun toString() = name
}
