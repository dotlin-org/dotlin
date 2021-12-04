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
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl

/**
 * Return expressions returning [Unit] are simplified to two statements: The expression it was returning and an
 * empty return.
 */
class UnitReturnsLowering(private val context: DartLoweringContext) : IrStatementAndBodyExpressionTransformer {
    override val statementTransformer = object : IrStatementTransformer {
        override fun transform(statement: IrStatement, body: IrBlockBody): Transformations<IrStatement> {
            if (statement.isNoMatch()) return noChange()

            statement as IrReturn

            return remove(statement) and add(statement.value) and add(
                IrReturnImpl(
                    UNDEFINED_OFFSET,
                    UNDEFINED_OFFSET,
                    type = context.irBuiltIns.unitType,
                    returnTargetSymbol = statement.returnTargetSymbol,
                    value = context.buildStatement(statement.returnTargetSymbol) {
                        irGetObject(context.irBuiltIns.unitClass)
                    }
                )
            )
        }

    }

    override val bodyExpressionTransformer = object : IrBodyExpressionTransformer {
        override fun transform(expression: IrExpression, body: IrExpressionBody): Transformation<IrExpression>? {
            if (expression.isNoMatch()) return noChange()

            expression as IrReturn

            return replaceWith(expression.value)
        }

    }

    private fun IrStatement.isNoMatch(): Boolean {
        return this !is IrReturn ||
                type != context.irBuiltIns.unitType ||
                returnTargetSymbol.owner !is IrFunction ||
                (returnTargetSymbol.owner as IrFunction).returnType != context.irBuiltIns.unitType
    }
}

