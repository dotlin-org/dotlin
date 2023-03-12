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
import org.dotlin.compiler.backend.steps.ir2ast.ir.correspondingProperty
import org.dotlin.compiler.backend.steps.ir2ast.ir.isExtension
import org.dotlin.compiler.backend.steps.ir2ast.ir.type
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.utils.addToStdlib.cast

class AssignExpressionsLowering(override val context: DotlinLoweringContext) : IrExpressionLowering {
    private val origins = listOf(PLUSEQ, MINUSEQ, MULTEQ, DIVEQ)

    override fun DotlinLoweringContext.transform(
        expression: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? {
        if (expression !is IrBlock || expression.origin !in origins) return noChange()

        val originalReceiver = expression.statements.first().cast<IrVariable>().initializer!!
        val setCall = expression.statements.last().cast<IrCall>()
        val setProperty = setCall.symbol.owner.correspondingProperty!!

        val receiver = buildStatement(context.container.symbol) {
            irGet(setProperty.type, originalReceiver, setProperty.getter!!.symbol)
        }

        setCall.apply {
            when {
                symbol.owner.isExtension -> extensionReceiver = receiver
                else -> dispatchReceiver = receiver
            }
        }

        return replaceWith(setCall)
    }
}

