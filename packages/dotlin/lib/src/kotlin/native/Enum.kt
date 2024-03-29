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

package kotlin

/**
 * The common base class of all enum classes.
 * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/enum-classes.html) for more
 * information on enum classes.
 */
@DartLibrary("dart:core")
external abstract class Enum const constructor(
    /**
     * Returns the name of this enum constant, exactly as declared in its enum declaration.
     */
    final val name: String,
    /**
     * Returns the ordinal of this enumeration constant (its position in its enum declaration, where the initial constant
     * is assigned an ordinal of zero).
     */
    @DartName("index")
    final val ordinal: Int
) {
    companion object {}

    override final fun equals(other: Any?): Boolean
    override final fun hashCode(): Int
    override fun toString() = name
}

/**
 * Returns an array containing enum T entries.
 */
@SinceKotlin("1.1")
external const inline fun <T : Enum> enumValues(): Array<T>

/**
 * Returns an enum entry with specified name.
 */
@SinceKotlin("1.1")
external inline fun <T : Enum> enumValueOf(name: String): T
