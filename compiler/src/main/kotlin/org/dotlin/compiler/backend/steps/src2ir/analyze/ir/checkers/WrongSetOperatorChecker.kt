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

import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.types.toKotlinType
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.psi.KtDeclaration

abstract class WrongSetOperatorChecker : IrDeclarationChecker {
    protected fun IrDeclaration.isNotRelevantSet() = this !is IrSimpleFunction || !isOperator ||
            name.asString() != "set" || valueParameters.size != 2
}

object WrongSetOperatorReturnTypeChecker : WrongSetOperatorChecker() {
    override val reports = listOf(ErrorsDart.WRONG_SET_OPERATOR_RETURN_TYPE)

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration.isNotRelevantSet()) return

        declaration as IrSimpleFunction

        val parameterType = declaration.valueParameters[1].type
        if (parameterType != declaration.returnType) {
            trace.report(ErrorsDart.WRONG_SET_OPERATOR_RETURN_TYPE.on(source, parameterType.toKotlinType()))
        }
    }
}

object WrongSetOperatorReturnChecker : WrongSetOperatorChecker() {
    override val reports = listOf(ErrorsDart.WRONG_SET_OPERATOR_RETURN)

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration.isNotRelevantSet()) return

        declaration as IrSimpleFunction

        val valueParameter = declaration.valueParameters[1]

        declaration.body?.acceptChildrenVoid(
            object : IrElementVisitorVoid {
                override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

                override fun visitReturn(expression: IrReturn) {
                    super.visitReturn(expression)

                    val returnValue = expression.value

                    if (returnValue !is IrGetValue || returnValue.symbol != valueParameter.symbol) {
                        trace.report(ErrorsDart.WRONG_SET_OPERATOR_RETURN.on(source, valueParameter.name.toString()))
                    }
                }
            }
        )
    }
}