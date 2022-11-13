/*
 * Copyright 2021-2022 Wilko Manger
 *
 * This file is part of Dotlin.
 *
 * Dotlin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dotlin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Dotlin.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.dotlin.compiler.backend.steps.ir2ast.ir

import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.types.IrType

@Suppress("ClassName")
sealed class IrDotlinDeclarationOrigin(private val name: String) : IrDeclarationOrigin {
    /**
     * Dart does not support parameters with complex (non-const) default values. This origin marks the class
     * to use for an instance as a default value for such a parameter. Then in the body, if the parameter is indeed
     * equal to the instance value, the real complex value (e.g. a function call) will be used.
     *
     * The `DefaultValue` class has two forms: One for Dart core built-ins which cannot be implemented, and any other
     * type. For other types, for example a `Vector`, it will take the form of `DefaultVectorValue`.
     */
    object COMPLEX_PARAM_DEFAULT_VALUE : IrDotlinDeclarationOrigin("COMPLEX_PARAM_DEFAULT_VALUE_MARKER")

    data class WAS_COMPLEX_PARAM(val originalType: IrType) : IrDotlinDeclarationOrigin("WAS_COMPLEX_PARAM")

    object SYNTHETIC_OPERATOR : IrDotlinDeclarationOrigin("SYNTHETIC_OPERATOR")

    object WAS_OPERATOR : IrDotlinDeclarationOrigin("WAS_OPERATOR")

    object EXTENSION : IrDotlinDeclarationOrigin("EXTENSION")

    object STATIC_OBJECT_MEMBER : IrDotlinDeclarationOrigin("STATIC_OBJECT_MEMBER")

    object WAS_CONST_OBJECT_MEMBER : IrDotlinDeclarationOrigin("WAS_CONST_OBJECT_MEMBER")

    /**
     * The constructor should be a `factory` in Dart.
     */
    object FACTORY : IrDotlinDeclarationOrigin("FACTORY")

    /**
     * Used by the `Comparable` lowerings. A temporary `compareTo` extension to `kotlin.Comparable` is added so that
     * the [OperatorsLowering] generates the correct Dart operators. Then this `compareTo` extension is removed again,
     * since `compareTo` is already defined in Dart's `Comparable`.
     */
    object COMPARABLE_TEMPORARY_COMPARE_TO : IrDotlinDeclarationOrigin("COMPARABLE_TEMPORARY_COMPARE_TO")

    /**
     * Default implementations from interfaces are copied over to the class that implements in the interface, since
     * in Dart no super implementations are available when `implements` is used.
     *
     * ALso used in [CollectionImplementersLowering].
     */
    object COPIED_OVERRIDE : IrDotlinDeclarationOrigin("COPIED_OVERRIDE")

    /**
     * Variable was created because of a property reference. The `KProperty` instance is cached in a variable.
     */
    object PROPERTY_REFERENCE : IrDotlinDeclarationOrigin("PROPERTY_REFERENCE")

    /**
     * Local accessor function created for local delegated properties.
     */
    object LOCAL_DELEGATED_PROPERTY_REFERENCE_ACCESSOR :
        IrDotlinDeclarationOrigin("LOCAL_DELEGATED_PROPERTY_REFERENCE_ACCESSOR")

    override fun toString() = name
}