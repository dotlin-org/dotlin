/*
 * Copyright 2010-2020 JetBrains s.r.o.
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

package kotlin.reflect

/**
 * Represents a type projection. Type projection is usually the argument to another type in a type usage.
 * For example, in the type `Array<out Number>`, `out Number` is the covariant projection of the type represented by the class `Number`.
 *
 * Type projection is either the star projection, or an entity consisting of a specific type plus optional variance.
 *
 * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/generics.html#type-projections)
 * for more information.
 */
@SinceKotlin("1.1")
data class KTypeProjection constructor(
    /**
     * The use-site variance specified in the projection, or `null` if this is a star projection.
     */
    val variance: KVariance?,
    /**
     * The type specified in the projection, or `null` if this is a star projection.
     */
    val type: KType?
) {

    init {
        require((variance == null) == (type == null)) {
            if (variance == null)
                "Star projection must have no type specified."
            else
                "The projection variance $variance requires type to be specified."
        }
    }

    override fun toString(): String = when (variance) {
        null -> "*"
        KVariance.INVARIANT -> type.toString()
        KVariance.IN -> "in $type"
        KVariance.OUT -> "out $type"
    }

    companion object {
        // provided for compiler access
        // TODO: Remove?
        @PublishedApi
        internal val star: KTypeProjection = KTypeProjection(null, null)

        /**
         * Star projection, denoted by the `*` character.
         * For example, in the type `KClass<*>`, `*` is the star projection.
         * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/generics.html#star-projections)
         * for more information.
         */
        val STAR: KTypeProjection get() = star

        /**
         * Creates an invariant projection of a given type. Invariant projection is just the type itself,
         * without any use-site variance modifiers applied to it.
         * For example, in the type `Set<String>`, `String` is an invariant projection of the type represented by the class `String`.
         */
        fun invariant(type: KType): KTypeProjection =
            KTypeProjection(KVariance.INVARIANT, type)

        /**
         * Creates a contravariant projection of a given type, denoted by the `in` modifier applied to a type.
         * For example, in the type `MutableList<in Number>`, `in Number` is a contravariant projection of the type of class `Number`.
         */
        fun contravariant(type: KType): KTypeProjection =
            KTypeProjection(KVariance.IN, type)

        /**
         * Creates a covariant projection of a given type, denoted by the `out` modifier applied to a type.
         * For example, in the type `Array<out Number>`, `out Number` is a covariant projection of the type of class `Number`.
         */
        fun covariant(type: KType): KTypeProjection =
            KTypeProjection(KVariance.OUT, type)
    }
}