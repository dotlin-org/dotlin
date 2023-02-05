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

import org.dotlin.compiler.backend.steps.ir2ast.ir.resolveRootOverride
import org.dotlin.compiler.backend.steps.ir2ast.ir.valueArguments
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.types.isComparable
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentClassOrNull

/**
 * Makes sure that calls to `Comparable<T>`'s `compareTo` do not get translated as `>`, `<=`, etc. calls.
 */
// TODO: Once we can import extensions written in Dart, we could just import the extension instead.
class ComparableCompareToCallsLowering(override val context: DotlinLoweringContext) : IrExpressionLowering {
    private val comparisonOrigins = listOf(LT, GT, LTEQ, GTEQ)

    override fun DotlinLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrCall || expression.origin !in comparisonOrigins) return noChange()

        val compareToCall = expression.valueArguments.firstOrNull()

        if (compareToCall !is IrCall) return noChange()

        val compareToMethod = compareToCall.symbol.owner.let {
            it.resolveRootOverride() ?: it
        }

        if (compareToMethod.parentClassOrNull?.defaultType?.isComparable() != true) {
            return noChange()
        }

        return replaceWith(
            expression.copy { index ->
                when (index) {
                    0 -> compareToCall.copy(origin = null)
                    else -> expression.getValueArgument(index)
                }
            }
        )
    }
}

private fun IrCall.copy(
    origin: IrStatementOrigin? = this.origin,
    mapValueArg: (Int) -> IrExpression? = { getValueArgument(it) }
): IrCall {
    val copy = IrCallImpl(
        startOffset,
        endOffset,
        type,
        symbol,
        typeArgumentsCount,
        valueArgumentsCount,
        origin,
        superQualifierSymbol,
    )

    val original = this@copy

    for (i in 0 until original.valueArgumentsCount) {
        copy.putValueArgument(i, mapValueArg(i))
    }

    copy.dispatchReceiver = original.dispatchReceiver
    copy.extensionReceiver = original.extensionReceiver

    return copy
}
