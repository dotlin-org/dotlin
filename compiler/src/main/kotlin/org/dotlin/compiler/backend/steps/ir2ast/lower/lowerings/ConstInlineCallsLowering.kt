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
import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopy
import org.dotlin.compiler.backend.steps.ir2ast.ir.transformExpressions
import org.dotlin.compiler.backend.steps.ir2ast.ir.valueArgumentsOrDefaults
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.isDartConst
import org.dotlin.compiler.backend.util.returnExpressions
import org.jetbrains.kotlin.backend.common.deepCopyWithVariables
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue

/**
 * Must run before [ConstLambdaLiteralsLowering].
 */
@Suppress("UnnecessaryVariable")
class ConstInlineCallsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(
        expression: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? = context.run {
        if (expression !is IrCall ||
            !expression.isDartConst(initializedIn = context.initializerContainer?.declaration)
        ) {
            return noChange()
        }

        val function = expression.symbol.owner

        if (!function.isInline || !function.isDartConst()) return noChange()

        val singleReturn = function.returnExpressions().singleOrNull()?.deepCopyWithVariables() ?: return noChange()

        return replaceWith(
            singleReturn.let {
                it.inlineExpressionsFrom(call = expression, parent = context.container)

                it.value
            }
        )
    }

    private fun IrElement.inlineExpressionsFrom(call: IrCall, parent: IrDeclaration) {
        val calledFunction = call.symbol.owner

        transformExpressions(initialParent = parent) { exp, _ ->
            when (exp) {
                is IrGetValue -> {
                    val declaration = exp.symbol.owner

                    when (declaration.parent) {
                        calledFunction -> when (declaration) {
                            is IrValueParameter -> call.valueArgumentsOrDefaults[declaration.index]
                            is IrVariable -> declaration.deepCopy(remapReferences = false).let { variable ->
                                variable.initializer?.also {
                                    variable.inlineExpressionsFrom(call, parent)
                                }
                            } ?: exp
                            else -> exp
                        }
                        else -> exp
                    }
                }
                else -> exp
            }
        }
    }
}