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

package org.dotlin.compiler.backend.util

import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isSetter
import org.jetbrains.kotlin.util.collectionUtils.filterIsInstanceAnd
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

val IrSimpleFunction.isOverload: Boolean
    get() = overloads.isNotEmpty()

// TODO: Handle local functions
// TODO: Handle extensions
val IrSimpleFunction.overloadsWithSelf: Collection<IrSimpleFunction>
    get() = parents.firstIsInstance<IrDeclarationContainer>()
        .declarations
        .filterIsInstanceAnd { it.name == name && !it.isGetter && !it.isSetter }

val IrSimpleFunction.overloads: Collection<IrSimpleFunction>
    get() = overloadsWithSelf - this

/**
 * The root overload is the overload with the least parameters. Can be this.
 *
 * If multiple overloads have the least amount of parameters of all overloads, but have the same amount, the base
 * overload is the first source occurrence of those overloads.
 */
val IrSimpleFunction.rootOverload: IrSimpleFunction
    get() = overloadsWithSelf.minByOrNull { it.valueParameters.size + it.typeParameters.size }!!

val IrSimpleFunction.isRootOverload: Boolean
    get() = isOverload && this == rootOverload

/**
 * The base overload is the first occurrence overload with the same amount of parameters _and_ parameter names, in the
 * same order. Accounts for both value and type parameters. Can be this.
 */
val IrSimpleFunction.baseOverload: IrSimpleFunction
    get() = overloadsWithSelf.asSequence()
        .filter { it.valueParameters.size == valueParameters.size && it.typeParameters.size == typeParameters.size }
        .filter { overload ->
            fun <T> List<T>.allSameSignatureNames(
                other: List<T>,
                toOverloadSignature: (T) -> OverloadParameterSignature
            ) = joinToString { toOverloadSignature(it).name } == other.joinToString { toOverloadSignature(it).name }

            overload.valueParameters.allSameSignatureNames(valueParameters) { it.overloadSignature } &&
                    overload.typeParameters.allSameSignatureNames(typeParameters) { it.overloadSignature }
        }
        .minByOrNull { it.valueParameters.size + it.typeParameters.size }!!

fun <T> List<T>.uniqueParametersComparedTo(
    parameters: List<T>,
    toOverloadSignature: (T) -> OverloadParameterSignature
): List<T> {
    fun List<T>.overloadSignatures() = map { toOverloadSignature(it) }

    return overloadSignatures()
        .subtract(parameters.overloadSignatures().toSet())
        .let {
            filter { param -> it.any { sig -> sig == toOverloadSignature(param) } }
        }
}

fun IrSimpleFunction.uniqueValueParametersComparedTo(function: IrSimpleFunction) =
    valueParameters.uniqueParametersComparedTo(function.valueParameters) { it.overloadSignature }

fun IrSimpleFunction.uniqueTypeParametersComparedTo(function: IrSimpleFunction) =
    typeParameters.uniqueParametersComparedTo(function.typeParameters) { it.overloadSignature }

/**
 * Overload signature for [IrValueParameter] or [IrTypeParameter]. If it's a [type] parameter,
 * [type] refers to the upper bounds concatenated.
 */
data class OverloadParameterSignature(val name: String, val type: String)

val IrValueParameter.overloadSignature: OverloadParameterSignature
    get() = OverloadParameterSignature(name.toString(), type.classFqName?.asString() ?: "<none>")

val IrTypeParameter.overloadSignature: OverloadParameterSignature
    get() = OverloadParameterSignature(
        name.toString(),
        superTypes.joinToString { it.classFqName?.asString() ?: "<none>" }
    )