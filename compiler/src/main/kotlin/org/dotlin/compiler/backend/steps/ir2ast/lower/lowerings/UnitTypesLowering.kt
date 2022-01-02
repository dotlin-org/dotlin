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

import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.TypeRemapper
import org.jetbrains.kotlin.ir.util.remapTypes
import org.jetbrains.kotlin.types.Variance

class UnitTypesLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        fun IrFunction.transformReturnType() {
            if (returnType.isUnit()) returnType = dartBuiltIns.voidType
        }

        declaration.also {
            when (it) {
                is IrFunction -> it.transformReturnType()
                is IrProperty -> it.apply {
                    getter?.transformReturnType()
                    setter?.transformReturnType()
                }
                is IrClass -> it.superTypes.forEach { superType ->
                    when (superType) {
                        is IrSimpleType -> IrSimpleTypeImpl(
                            kotlinType = superType.originalKotlinType,
                            classifier = superType.classifier,
                            hasQuestionMark = superType.hasQuestionMark,
                            arguments = superType.arguments.map { arg ->
                                when {
                                    arg.typeOrNull?.isUnit() == true -> makeTypeProjection(
                                        type = dartBuiltIns.voidType,
                                        variance = Variance.INVARIANT
                                    )
                                    else -> arg
                                }
                            },
                            annotations = superType.annotations,
                            abbreviation = superType.abbreviation,
                        ).also { newType ->
                            it.remapTypes(
                                object : TypeRemapper {
                                    override fun enterScope(irTypeParametersContainer: IrTypeParametersContainer) {}
                                    override fun leaveScope() {}

                                    override fun remapType(type: IrType) = when (type) {
                                        superType -> newType
                                        else -> type
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        return noChange()
    }
}

