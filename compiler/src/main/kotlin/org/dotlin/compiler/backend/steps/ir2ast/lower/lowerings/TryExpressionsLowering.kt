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
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.name.Name
import kotlin.math.exp

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class TryExpressionsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
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

        if (expression.isStatement) return noChange()



        return replaceWith(
            wrapInAnonymousFunctionInvocation(expression, container) {
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