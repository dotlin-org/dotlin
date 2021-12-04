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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrConjunctionExpression
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDisjunctionExpression
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
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
import kotlin.math.exp

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class ConjunctionsDisjunctionsLowering(private val context: DartLoweringContext) : IrExpressionTransformer {
    override fun transform(
        expression: IrExpression,
        containerParent: IrDeclarationParent
    ): Transformation<IrExpression>? {
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

        return replaceWith(
            when {
                isConjunction -> IrConjunctionExpression(context, left, right)
                else -> IrDisjunctionExpression(context, left, right)
            }
        )
    }
}