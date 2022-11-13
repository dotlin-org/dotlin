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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDotlinStatementOrigin
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrExpressionContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.irCall
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrWhen

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class ElvisLowering(override val context: DotlinLoweringContext) : IrExpressionLowering {
    override fun DotlinLoweringContext.transform(
        expression: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? {
        if (expression !is IrBlock || expression.origin != IrStatementOrigin.ELVIS) return noChange()

        val irBlock = expression

        val left = (irBlock.statements.first() as IrVariable).initializer!!
        val right = irBlock.statements.last().let {
            it as IrWhen
            it.branches.first().result
        }

        return replaceWith(
            buildStatement(context.container.symbol) {
                irCall(
                    dotlinIrBuiltIns.ifNull(irBlock.type),
                    receiver = left,
                    right,
                    origin = IrDotlinStatementOrigin.IF_NULL
                )
            }
        )
    }
}