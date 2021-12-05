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

import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrNullAwareExpression
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class SafeCallsLowering(private val context: DartLoweringContext) : IrExpressionTransformer {
    override fun transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrBlock || expression.origin != IrStatementOrigin.SAFE_CALL) return noChange()

        val irBlock = expression

        val receiver = (irBlock.statements.first() as IrVariable).initializer!!

        val value = expression.statements.last().let {
            it as IrWhen

            it.branches.last().result
        } as IrMemberAccessExpression<*>

        value.dispatchReceiver = receiver

        return replaceWith(
            IrNullAwareExpression(value)
        )
    }
}