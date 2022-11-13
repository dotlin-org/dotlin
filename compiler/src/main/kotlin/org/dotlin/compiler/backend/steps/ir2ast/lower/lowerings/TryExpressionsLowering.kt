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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrExpressionContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.isStatementIn
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTry
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class TryExpressionsLowering(override val context: DotlinLoweringContext) : IrExpressionLowering {
    override fun DotlinLoweringContext.transform(
        expression: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? {
        if (expression !is IrTry) return noChange()
        if (expression !is IrTryImpl) throw UnsupportedOperationException("IrThrow must be IrThrowImpl")

        // We do this regardless whether this try-catch is a statement or not.
        expression.apply {
            tryResult = tryResult.wrapInBlock()
            finallyExpression = finallyExpression?.wrapInBlock()
            catches.forEach {
                it.result = it.result.wrapInBlock()
            }
        }

        if (expression.isStatementIn(context.container)) return noChange()

        return replaceWith(
            wrapInAnonymousFunctionInvocation(expression, context.container) {
                listOf(
                    // If we put it in an anonymous function,
                    // the last statements in the try and catch blocks must return.
                    expression.apply {
                        tryResult.replaceLastStatementWithReturn(at = it)
                        finallyExpression?.replaceLastStatementWithReturn(at = it)
                        catches.forEach { catch ->
                            catch.result.replaceLastStatementWithReturn(at = it)
                        }
                    }
                )
            }
        )
    }

    private fun IrExpression.replaceLastStatementWithReturn(at: IrSimpleFunction) {
        (this as IrBlock).statements.apply {
            toList().let {
                clear()
                addAll(it.withLastAsReturn(at))
            }
        }
    }

    private fun IrExpression.wrapInBlock() = when (this) {
        is IrBlock -> this
        else -> IrBlockImpl(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            type = type,
            origin = null,
            statements = listOf(this)
        )
    }
}