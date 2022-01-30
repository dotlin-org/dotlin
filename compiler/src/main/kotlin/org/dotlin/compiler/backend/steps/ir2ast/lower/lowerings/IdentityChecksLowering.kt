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
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.copyAttributes
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.util.copyTypeAndValueArgumentsFrom

/**
 * Lowers `a === b` into `identical(a, b)`.
 */
class IdentityChecksLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrCall || (expression.origin != EQEQEQ && expression.origin != EXCLEQEQ)) return noChange()

        // For EXCLEQEQ, we only want to change the 'EQEQEQ' call itself. However, the 'not' call should be fixed
        // to have the correct origin.
        if (expression.origin == EXCLEQEQ && expression.symbol.owner.name.identifierOrNullIfSpecial == "not") {
            return expression.let {
                replaceWith(
                    IrCallImpl(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        it.type,
                        it.symbol,
                        it.typeArgumentsCount,
                        it.valueArgumentsCount,
                        origin = EXCL
                    ).apply {
                        copyAttributes(expression)
                        copyTypeAndValueArgumentsFrom(expression)
                        dispatchReceiver = expression.dispatchReceiver
                        extensionReceiver = expression.extensionReceiver
                    }
                )
            }
        }

        return replaceWith(
            IrCallImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                type = irBuiltIns.booleanType,
                symbol = dartBuiltIns.identical,
                typeArgumentsCount = 0,
                valueArgumentsCount = 2,
            ).apply {
                copyAttributes(expression)
                copyTypeAndValueArgumentsFrom(expression)
            }
        )
    }
}