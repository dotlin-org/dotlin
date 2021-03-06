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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDartStatementOrigin
import org.dotlin.compiler.backend.steps.ir2ast.ir.extensionReceiverOrNull
import org.dotlin.compiler.backend.steps.ir2ast.ir.isPrimitiveNumber
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrExpressionLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformation
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.util.primaryConstructor

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class ConflictingExtensionCallsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
        if (expression !is IrCall) return noChange()

        val receiver = expression.extensionReceiverOrNull ?: return noChange()

        val function = expression.symbol.owner
        val extensionContainer = function.extensionContainer ?: return noChange()

        if (!receiver.type.isPrimitiveNumber()) return noChange()

        expression.apply {
            dispatchReceiver = extensionContainer.primaryConstructor!!.let { constructor ->
                IrConstructorCallImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    type = constructor.returnType,
                    symbol = constructor.symbol,
                    typeArgumentsCount = 0,
                    constructorTypeArgumentsCount = 0,
                    valueArgumentsCount = 1,
                    origin = IrDartStatementOrigin.EXTENSION_CONSTRUCTOR_CALL
                ).apply {
                    putValueArgument(0, receiver)
                }
            }
            extensionReceiver = null
        }

        return noChange()
    }
}