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
import org.dotlin.compiler.backend.util.*
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement

object ConstInlineChecker : IrDeclarationChecker {
    override val reports = listOf(
        ErrorsDart.CONST_INLINE_FUNCTION_WITH_MULTIPLE_RETURNS,
        ErrorsDart.CONST_INLINE_FUNCTION_RETURNS_NON_CONST,
        ErrorsDart.CONST_INLINE_FUNCTION_HAS_INVALID_STATEMENT
    )

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration.isActuallyExternal || !declaration.isDartConstInlineFunction()) {
            return
        }

        val returns = declaration.returnExpressions()
        val singleReturn = returns.singleOrNull()

        // TODO: Add error for functions with no implementation

        if (singleReturn == null) {
            var reportedAtLeastOnce = false
            fun report(element: KtElement) {
                trace.report(ErrorsDart.CONST_INLINE_FUNCTION_WITH_MULTIPLE_RETURNS.on(element))
                reportedAtLeastOnce = true
            }

            for (irReturn in returns) {
                when (val returnSource = irReturn.ktExpression) {
                    null -> continue
                    else -> report(returnSource)
                }
            }

            // If somehow all returns did not have corresponding source elements, report once on the function itself.
            if (!reportedAtLeastOnce) {
                report(source)
            }
        }

        if (singleReturn?.value?.isDartConst(implicit = true, constInlineContainer = declaration) == false) {
            trace.report(
                ErrorsDart.CONST_INLINE_FUNCTION_RETURNS_NON_CONST.on(singleReturn.value.ktExpression ?: source)
            )
        }

        declaration.body?.statements?.forEach { statement ->
            if (statement !is IrReturn && (statement !is IrVariable || !statement.isDartConst())) {
                val reportOn = when (statement) {
                    is IrExpression -> statement.ktExpression ?: source
                    is IrDeclaration -> statement.ktDeclaration ?: source
                    else -> source
                }

                trace.report(
                    ErrorsDart.CONST_INLINE_FUNCTION_HAS_INVALID_STATEMENT.on(reportOn)
                )
            }
        }
    }
}