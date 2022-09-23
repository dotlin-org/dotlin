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

import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrConjunctionExpression
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrDisjunctionExpression
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrWhen

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class ConjunctionsDisjunctionsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrWhen) return noChange()

        val irWhen = expression
        val isConjunction = irWhen.origin == IrStatementOrigin.ANDAND
        val isDisjunction = irWhen.origin == IrStatementOrigin.OROR

        if (!isConjunction && !isDisjunction) return noChange()

        val left = irWhen.branches.first().condition
        val right = when {
            isConjunction -> irWhen.branches.first().result
            else -> irWhen.branches.last().result
        }

        val type = irBuiltIns.booleanType

        return replaceWith(
            when {
                isConjunction -> IrConjunctionExpression(left, right, type)
                else -> IrDisjunctionExpression(left, right, type)
            }
        )
    }
}