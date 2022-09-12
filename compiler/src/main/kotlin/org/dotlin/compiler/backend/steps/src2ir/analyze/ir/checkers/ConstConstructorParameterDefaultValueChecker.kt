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
import org.dotlin.compiler.backend.util.isDartConst
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.psi.KtDeclaration

object ConstConstructorParameterDefaultValueChecker : IrDeclarationChecker {
    override val reports = listOf(ErrorsDart.NON_CONSTANT_DEFAULT_VALUE_IN_CONST_CONSTRUCTOR)

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration !is IrConstructor || !declaration.isDartConst()) return
        if (!declaration.isDartConst()) return

        val defaultValues = declaration.valueParameters.mapNotNull { it.defaultValue?.expression }

        for (defaultValue in defaultValues) {
            if (!defaultValue.isDartConst()) {
                trace.report(
                    ErrorsDart.NON_CONSTANT_DEFAULT_VALUE_IN_CONST_CONSTRUCTOR.on(
                        defaultValue.ktExpression ?: source
                    )
                )
            }
        }
    }
}