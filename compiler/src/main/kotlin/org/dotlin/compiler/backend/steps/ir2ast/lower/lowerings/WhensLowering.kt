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
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.makeNotNull

/**
 * Since a temporary subject is used, smart cast does not work, so explicit casts are added.
 */
class WhensWithSubjectCastToNonNullLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(
        expression: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? {
        if (expression !is IrBlock || expression.origin != IrStatementOrigin.WHEN) return noChange()

        val originalSubject = (expression.statements.first() as IrVariable).initializer as? IrGetValue
            ?: return noChange()

        val whenExpression = expression.statements.last() as IrWhen

        var mustAssertNotNull = false
        for (branch in whenExpression.branches) {
            if (branch.condition.isEqualsNull()) {
                mustAssertNotNull = true
                continue
            }

            if (mustAssertNotNull) {
                branch.result = branch.result.transform(
                    object : IrCustomElementTransformerVoid() {
                        override fun visitExpression(expression: IrExpression): IrExpression {
                            expression.transformChildrenVoid()
                            if (expression !is IrGetValue || expression.symbol != originalSubject.symbol) {
                                return expression
                            }

                            return buildStatement(context.container.symbol) {
                                irCall(
                                    irBuiltIns.checkNotNullSymbol.owner,
                                    receiver = null,
                                    valueArguments = arrayOf(expression),
                                    origin = IrStatementOrigin.EXCLEXCL
                                ).apply {
                                    type = expression.type.makeNotNull()
                                }
                            }
                        }
                    },
                    data = null
                )
            }
        }

        return noChange()
    }

    private fun IrExpression.isEqualsNull(): Boolean {
        if (this !is IrCall || origin != IrStatementOrigin.EQEQ) return false

        val argument = valueArguments.lastOrNull() ?: return false
        return argument is IrConst<*> && argument.kind == IrConstKind.Null
    }
}

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class WhensWithSubjectExpressionsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(
        expression: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? {
        if (expression !is IrBlock || expression.origin != IrStatementOrigin.WHEN ||
            expression.isStatementIn(context.container)) {
            return noChange()
        }

        val whenExpression = expression.statements.last() as IrWhen

        return replaceWith(
            wrapInAnonymousFunctionInvocation(whenExpression, context.container) {
                expression.statements.withLastAsReturn(at = it)
            }
        )
    }
}