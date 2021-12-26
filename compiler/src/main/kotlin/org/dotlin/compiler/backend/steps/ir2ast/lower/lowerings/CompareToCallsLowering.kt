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

import org.dotlin.compiler.backend.steps.ir2ast.ir.valueArguments
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.expressions.*

/**
 * Simplifies `x.compareTo(y) >= 0` to `x.compareTo(y)` which eventually gets simplified to `x >= y`.
 */
class CompareToCallsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
        // Conditions for match: `expression` must be IrCall and a built in operator. First arg must be IrCall and have
        // the same `origin` as `expression`. First arg must be an operator and must have name "compareTo".
        // `expression` must have origins of either LT, GT, LTEQ or GTEQ.

        if (expression !is IrCall || expression.symbol.owner.origin != IrBuiltIns.BUILTIN_OPERATOR) return noChange()

        val firstArg = expression.valueArguments.firstOrNull()

        if (firstArg !is IrCall ||
            expression.origin != firstArg.origin ||
            (expression.origin != IrStatementOrigin.LT &&
                    expression.origin != IrStatementOrigin.GT &&
                    expression.origin != IrStatementOrigin.LTEQ &&
                    expression.origin != IrStatementOrigin.GTEQ) &&
            !firstArg.symbol.owner.isOperator ||
            firstArg.symbol.owner.name.identifier != "compareTo"
        ) {
            return noChange()
        }

        val secondArg = expression.valueArguments[1]

        if (secondArg !is IrConst<*> || secondArg.kind != IrConstKind.Int || secondArg.value != 0) return noChange()

        firstArg.type = irBuiltIns.booleanType

        return replaceWith(firstArg)
    }
}