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

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.mergeNullability
import org.jetbrains.kotlin.ir.util.*

class ExtensionsLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        val extensionContainer = declaration.extensionContainer ?: return noChange()

        declaration.file.apply {
            if (extensionContainer !in declarations) {
                addChild(extensionContainer)
            }
        }

        extensionContainer.apply {
            declaration.file.declarations.remove(declaration)
            addChild(declaration)
        }

        val oldReceiverTypeParameters = declaration.receiverTypeParameters
        if (oldReceiverTypeParameters.isNotEmpty()) {
            if (declaration is IrFunction) {
                declaration.typeParameters -= oldReceiverTypeParameters
            }

            val newReceiverTypeParameter = extensionContainer.typeParameters

            val newToOldReceiverTypeParameters = oldReceiverTypeParameters
                .zip(newReceiverTypeParameter)
                .toMap()

            // Remap type parameters.
            declaration.remapTypes(
                object : TypeRemapper {
                    override fun enterScope(irTypeParametersContainer: IrTypeParametersContainer) {}

                    override fun leaveScope() {}

                    override fun remapType(type: IrType): IrType {
                        val typeParameter = type.typeParameterOrNull ?: return type
                        return newToOldReceiverTypeParameters[typeParameter]?.defaultType?.mergeNullability(type)
                            ?: return type
                    }
                }
            )
        }

        return just { remove() }
    }

    /**
     * Removes the receiver type parameter from function calls.
     */
    class RemoveReceiverTypeArguments(override val context: DotlinLoweringContext) : IrExpressionLowering {
        override fun DotlinLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
            if (expression !is IrCall) return noChange()

            val function = expression.symbol.owner

            if (!function.isExtension) return noChange()

            val receiverTypeParameters = function.receiverTypeParameters
            val newTypeArguments = expression.typeArguments.mapIndexedNotNull { index, arg ->
                when {
                    function.typeParameters[index] in receiverTypeParameters -> null
                    else -> arg
                }
            }

            return replaceWith(
                IrCallImpl(
                    expression.startOffset, expression.endOffset,
                    expression.type,
                    expression.symbol,
                    typeArgumentsCount = newTypeArguments.size,
                    expression.valueArgumentsCount,
                    expression.origin,
                    expression.superQualifierSymbol,
                ).apply {
                    copyValueArgumentsFrom(expression, destFunction = symbol.owner)
                    newTypeArguments.forEachIndexed { i, arg -> putTypeArgument(i, arg) }
                }
            )
        }
    }
}

private val IrDeclaration.receiverTypeParameters: List<IrTypeParameter>
    get() = extensionReceiverParameterOrNull!!.type.typeParametersOrSelf