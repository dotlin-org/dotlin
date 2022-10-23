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
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.INAPPLICABLE_CONST_FUNCTION_MODIFIER
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.dotlin.compiler.backend.util.hasAnnotation
import org.dotlin.compiler.backend.util.hasConstModifierOrAnnotation
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.isPropertyAccessor
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtDeclaration

object ConstFunctionWithoutInlineChecker : IrDeclarationChecker {
    override val reports = listOf(INAPPLICABLE_CONST_FUNCTION_MODIFIER)

    override fun IrAnalyzerContext.check(source: KtDeclaration, element: IrDeclaration) {
        if (element !is IrSimpleFunction || element.isPropertyAccessor || !element.hasConstModifierOrAnnotation()) {
            return
        }

        if (!element.isInline && !element.hasAnnotation(dotlin.DartConstructor)) {
            trace.report(
                INAPPLICABLE_CONST_FUNCTION_MODIFIER.on(
                    source.modifierList?.getModifier(KtTokens.CONST_KEYWORD) ?: source
                )
            )
        }
    }
}