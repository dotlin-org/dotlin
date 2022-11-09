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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.parametersByArguments
import org.dotlin.compiler.backend.steps.ir2ast.ir.polymorphicallyIs
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.types.*

class TypeParametersWithImplicitSuperTypesLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrTypeParametersContainer) {
            return noChange()
        }

        for (it in declaration.typeParameters) {
            val implicitSuperTypes = it.implicitSuperTypes()
            if (implicitSuperTypes.isEmpty()) continue

            // Super types that are super types of all implicit super types are removed. This means that Any? is always
            // removed.
            it.superTypes = it.superTypes
                .filter { superType ->
                    implicitSuperTypes.none { implicitSuperType -> implicitSuperType polymorphicallyIs superType }
                }

            it.superTypes += implicitSuperTypes
        }

        return noChange()
    }

    private fun IrTypeParameter.implicitSuperTypes(): Set<IrType> {
        val otherTypeParameters = (parent as IrTypeParametersContainer).typeParameters.filter { it != this }

        return otherTypeParameters
            .asSequence()
            .map {
                it.superTypes
                    .filterIsInstance<IrSimpleType>()
                    .map { t ->
                        t.parametersByArguments().entries
                            .filter { (_, arg) -> arg.typeOrNull?.classifierOrNull == this.symbol }
                            .map { (param, _) -> param.superTypes.filter { s -> s !in this.superTypes } }
                    }
            }
            .flatten()
            .flatten()
            .flatten()
            .filter { !it.isNullableAny() } // We don't care about Any?.
            .toSet()
    }
}