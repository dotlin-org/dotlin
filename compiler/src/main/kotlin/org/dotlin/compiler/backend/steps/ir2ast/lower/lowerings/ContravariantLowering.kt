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
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrFileLowering
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isFunctionTypeOrSubtype
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.types.Variance

/**
 * Dart has no equivalent of `in` variance. This is why dynamic is used for the generic type that was `in` when
 * necessary.
 */
// TODO: Add annotation specifying the original type.
class ContravariantLowering(override val context: DartLoweringContext) : IrFileLowering {
    override fun DartLoweringContext.transform(file: IrFile) {
        fun IrType.makeContravariantArgumentsDynamic(): IrType {
            if (this !is IrSimpleType) return this

            val contravariantArguments = contravariantTypeArguments()

            return IrSimpleTypeImpl(
                originalKotlinType,
                classifier,
                hasQuestionMark,
                arguments = arguments
                    .map {
                        when (it) {
                            in contravariantArguments -> makeTypeProjection(dynamicType, Variance.INVARIANT)
                            else -> when (it) {
                                is IrTypeProjection -> makeTypeProjection(
                                    it.type.makeContravariantArgumentsDynamic(),
                                    it.variance
                                )
                                else -> it
                            }
                        }
                    },
                annotations,
                abbreviation
            )
        }

        file.transformChildrenVoid(
            object : IrElementTransformerVoid() {
                override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
                    if (declaration is IrTypeParametersContainer) {
                        declaration.apply {
                            typeParameters.forEach { param ->
                                param.superTypes = param.superTypes.map { it.makeContravariantArgumentsDynamic() }
                            }
                        }
                    }

                    return super.visitDeclaration(declaration)
                }

                override fun visitVariable(declaration: IrVariable): IrStatement {
                    declaration.apply {
                        type = type.makeContravariantArgumentsDynamic()
                    }

                    return super.visitVariable(declaration)
                }

                override fun visitFunction(declaration: IrFunction): IrStatement {
                    declaration.apply {
                        valueParameters.forEach {
                            it.type = it.type.makeContravariantArgumentsDynamic()
                        }
                    }

                    return super.visitFunction(declaration)
                }

                override fun visitField(declaration: IrField): IrStatement {
                    declaration.apply {
                        type = type.makeContravariantArgumentsDynamic()
                    }
                    return super.visitField(declaration)
                }
            }
        )
    }
}

private fun IrSimpleType.contravariantTypeArguments(): List<IrTypeArgument> {
    val owner = classOrNull?.owner ?: return emptyList()

    // Function types have their value parameter types declared as covariant, we ignore this.
    if (owner.defaultType.isFunctionTypeOrSubtype()) return emptyList()

    return parametersByArguments()
        .entries
        .mapNotNull { (param, arg) ->
            when (param.variance) {
                Variance.IN_VARIANCE -> arg
                else -> null
            }
        }
}