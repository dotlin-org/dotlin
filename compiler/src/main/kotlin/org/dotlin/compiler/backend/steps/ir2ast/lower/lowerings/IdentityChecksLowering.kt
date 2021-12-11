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
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.util.copyTypeAndValueArgumentsFrom

/**
 * Lowers `a === b` into `identical(a, b)`.
 */
class IdentityChecksLowering(private val context: DartLoweringContext) : IrExpressionTransformer {
    override fun transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrCall || expression.origin != IrStatementOrigin.EQEQEQ) return noChange()

        return replaceWith(
            IrCallImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                type = context.irBuiltIns.booleanType,
                symbol = context.dartBuiltIns.identical,
                typeArgumentsCount = 0,
                valueArgumentsCount = 2,
            ).apply {
                copyTypeAndValueArgumentsFrom(expression)
            }
        )
    }
}