/*
 * Copyright 2021 Wilko Manger
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

import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.name.Name

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class WhensWithSubjectExpressionsLowering(private val context: DartLoweringContext) : IrExpressionTransformer {
    override fun <D> transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
        if (expression !is IrBlock || expression.origin != IrStatementOrigin.WHEN) return noChange()

        val whenExpression = expression.statements.last() as IrWhen

        val anonymousFunction = context.irFactory.buildFun {
            name = Name.special("<anonymous>")
            returnType = whenExpression.type
        }.apply {
            parent = container

            body = IrBlockBodyImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                statements = expression.statements.dropLast(1) + whenExpression.let {
                    IrReturnImpl(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        type = (it as IrExpression).type,
                        returnTargetSymbol = symbol,
                        value = it
                    )
                }
            )
        }

        val invokeMethod = context.irFactory.buildFun {
            name = Name.identifier("invoke")
            isOperator = true
            returnType = whenExpression.type
        }.apply {
            parent = anonymousFunction
            dispatchReceiverParameter = context.irBuiltIns.functionN(0).thisReceiver
        }

        val functionExpression = IrFunctionExpressionImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            type = whenExpression.type,
            function = anonymousFunction,
            origin = IrStatementOrigin.WHEN
        )

        return replaceWith(
            IrCallImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                type = expression.type,
                symbol = invokeMethod.symbol,
                typeArgumentsCount = 0,
                valueArgumentsCount = 0,
                origin = IrStatementOrigin.INVOKE,
            ).apply {
                dispatchReceiver = functionExpression
            }
        )
    }
}