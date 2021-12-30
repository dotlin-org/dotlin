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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDartStatementOrigin
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl

/**
 * Changes the origin so that [WhensWithSubjectExpressionsLowering] doesn't change anything.
 */
class WhensWithSubjectStatementsLowering(override val context: DartLoweringContext) : IrStatementLowering {
    override fun DartLoweringContext.transform(
        statement: IrStatement,
        container: IrStatementContainer
    ): Transformations<IrStatement> {
        if (statement !is IrBlock || statement.origin != IrStatementOrigin.WHEN) return noChange()

        return just {
            statement.let { irBlock ->
                replaceWith(
                    IrBlockImpl(
                        startOffset = irBlock.startOffset,
                        endOffset = irBlock.endOffset,
                        type = irBlock.type,
                        statements = irBlock.statements,
                        origin = IrDartStatementOrigin.WHEN_STATEMENT
                    )
                )
            }
        }
    }
}

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class WhensWithSubjectExpressionsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
        if (expression !is IrBlock || expression.origin != IrStatementOrigin.WHEN) return noChange()

        val whenExpression = expression.statements.last() as IrWhen

        return replaceWith(
            wrapInAnonymousFunctionInvocation(whenExpression, container) {
                expression.statements.withLastAsReturn(at = it)
            }
        )
    }
}