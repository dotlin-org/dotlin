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

import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.util.copyTypeAndValueArgumentsFrom
import org.jetbrains.kotlin.name.Name

/**
 * This lowering mostly applies to anonymous function expressions that are invoked.
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class InvokeCallsLowering(private val context: DartLoweringContext) : IrExpressionTransformer {
    override fun transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrCall || expression.origin != IrStatementOrigin.INVOKE) return noChange()

        val invokeMethod = expression.symbol.owner

        // In most cases the operator is already correctly changed, so we don't have to anything here.
        if (invokeMethod.name.identifier == "call") return noChange()

        val callMethod = invokeMethod.deepCopyWith {
            name = Name.identifier("call")
        }

        return replaceWith(
            IrCallImpl(
                startOffset = expression.startOffset,
                endOffset = UNDEFINED_OFFSET,
                type = expression.type,
                symbol = callMethod.symbol,
                typeArgumentsCount = expression.typeArgumentsCount,
                valueArgumentsCount = expression.valueArgumentsCount,
                origin = IrStatementOrigin.INVOKE,
                superQualifierSymbol = expression.superQualifierSymbol,
            ).apply {
                dispatchReceiver = expression.dispatchReceiver
                extensionReceiver = expression.extensionReceiver

                copyTypeAndValueArgumentsFrom(expression)
            }
        )
    }
}