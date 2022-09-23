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
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrDisjunctionExpression
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.builders.irIs
import org.jetbrains.kotlin.ir.builders.irNotIs
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.util.isFunctionTypeOrSubtype

/**
 * If a variable whose value has a type that is a class that implements a function type is not known
 * at compile time (e.g. is dynamic), runtime type checks will fail for that function type, even if it would've
 * worked if the variable had a proper type annotation. To mitigate this, an extra check is added for the `Function`
 * interfaces (e.g. `Function1`, `Function2`).
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class FunctionTypeIsChecksLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(
        expression: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? {
        if (expression !is IrTypeOperatorCall ||
            (expression.operator != IrTypeOperator.INSTANCEOF &&
                    expression.operator != IrTypeOperator.NOT_INSTANCEOF)) {
            return noChange()
        }

        if (!expression.typeOperand.isFunctionTypeOrSubtype()) return noChange()

        val negated = expression.operator == IrTypeOperator.NOT_INSTANCEOF;

        val instanceOfFunctionInterface = buildStatement(context.container.symbol) {
            val arg = expression.argument
            val type = expression.typeOperand

            when {
                negated -> irNotIs(arg, type)
                else -> irIs(arg, type)
            }
        }

        instanceOfFunctionInterface.isFunctionTypeCheck = true

        return replaceWith(
            IrDisjunctionExpression(
                expression,
                instanceOfFunctionInterface,
                type = irBuiltIns.booleanType
            ).apply {
                isParenthesized = true
            }
        )
    }
}