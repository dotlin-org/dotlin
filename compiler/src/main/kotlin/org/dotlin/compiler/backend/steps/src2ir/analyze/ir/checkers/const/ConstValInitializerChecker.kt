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

import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.dotlin.compiler.backend.util.isDartConst
import org.dotlin.compiler.backend.util.isDartConstInlineFunction
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.psi.KtDeclaration

object ConstValInitializerChecker : IrDeclarationChecker {
    override val reports = listOf(CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE)

    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration !is IrVariable && declaration !is IrProperty && declaration !is IrField) return
        if (!declaration.isDartConst()) return

        val initializer = when (declaration) {
            // This also accounts for IrProperty's backing field.
            is IrField -> declaration.initializer?.expression
            is IrVariable -> declaration.initializer
            else -> null
        } ?: return

        val isDartConst = initializer.isDartConst(
            implicit = true,
            constInlineContainer = when (declaration) {
                is IrVariable -> {
                    val parent = declaration.parent
                    when {
                        parent.isDartConstInlineFunction() -> parent
                        else -> null
                    }
                }
                else -> null
            }
        )

        if (!isDartConst) {
            trace.report(CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE.on(source))
        }
    }
}