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

package org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementVisitorVoid
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrExpressionContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.valueArguments
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrExpressionChecker
import org.dotlin.compiler.backend.util.isAccessibleInDartConstLambda
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.psi.KtExpression

object ConstLambdaChecker : IrExpressionChecker {
    override val reports = listOf(ErrorsDart.CONST_LAMBDA_ACCESSING_NON_GLOBAL_VALUE)

    // TODO: Check for non-local returns
    override fun IrAnalyzerContext.check(source: KtExpression, element: IrExpression, data: IrExpressionContext) {
        if (element !is IrFunctionAccessExpression ||
            !element.isDartConst(initializedIn = data.initializerContainer?.declaration)
        ) {
            return
        }

        val args = element.valueArguments

        for (arg in args) {
            if (arg !is IrFunctionExpression) continue

            arg.function.body?.acceptChildrenVoid(
                object : IrCustomElementVisitorVoid {
                    override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

                    override fun visitDeclarationReference(expression: IrDeclarationReference) {
                        super.visitDeclarationReference(expression)

                        if (!expression.isAccessibleInDartConstLambda(arg.function)) {
                            trace.report(
                                ErrorsDart.CONST_LAMBDA_ACCESSING_NON_GLOBAL_VALUE.on(
                                    expression.ktExpression ?: arg.ktExpression ?: source
                                )
                            )
                        }
                    }
                }
            )
        }
    }
}