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

import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.copyAttributes
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.util.isThrowable

/**
 * No casts are necessary to `Throwable`, it does not exist.
 */
class RemoveThrowableCastsLowering(override val context: DotlinLoweringContext) : IrExpressionLowering {
    override fun DotlinLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrTypeOperatorCall ||
            !expression.type.isThrowable() ||
            expression.operator != IrTypeOperator.IMPLICIT_CAST) {
            return noChange()
        }

        return replaceWith(
            expression.argument.also {
                it.copyAttributes(expression)
            }
        )
    }
}

