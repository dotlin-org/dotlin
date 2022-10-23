/*
 * Copyright 2022 Wilko Manger
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

package org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers.const

import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrExpressionContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.CONST_WITH_NON_CONST
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrExpressionChecker
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.psi.KtExpression

object ConstAnnotationChecker : IrExpressionChecker {
    override val reports = listOf(CONST_WITH_NON_CONST)

    override fun IrAnalyzerContext.check(source: KtExpression, element: IrExpression, context: IrExpressionContext) {
        if (!element.hasAnnotation(dotlin.const)) return

        when (element) {
            is IrCall, is IrConstructorCall -> if (!element.isDartConst()) {
                trace.report(
                    CONST_WITH_NON_CONST.on(
                        source,
                        when (element) {
                            is IrConstructorCall -> "constructor"
                            else -> "function"
                        }
                    )
                )
            }
            else -> trace.report(ErrorsDart.ONLY_FUNCTION_AND_CONSTRUCTOR_CALLS_CAN_BE_CONST.on(source))
        }
    }
}