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

import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrExpressionLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformation
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.dotlin.compiler.backend.util.hasDifferentDefaultValueInDart
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

/**
 * Add default values of parameters marked with `@DartDifferentDefaultValue` explicitly.
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class DartDifferentDefaultValueArgumentsLowering(override val context: DotlinLoweringContext) : IrExpressionLowering {
    override fun DotlinLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrCall) return noChange()

        val owner = expression.symbol.owner

        val parametersWithDifferentDefaultValue =
            owner.valueParameters.filter { it.hasDifferentDefaultValueInDart() }

        if (parametersWithDifferentDefaultValue.isEmpty()) return noChange()

        for (param in parametersWithDifferentDefaultValue) {
            if (expression.getValueArgument(param.index) != null) continue

            expression.putValueArgument(param.index, param.defaultValue!!.expression)
        }

        return noChange()
    }
}