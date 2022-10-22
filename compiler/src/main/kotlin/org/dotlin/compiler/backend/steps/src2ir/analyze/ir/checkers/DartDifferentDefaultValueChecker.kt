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

import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.getAnnotation
import org.dotlin.compiler.backend.util.hasAnnotation
import org.dotlin.compiler.backend.util.isDotlinExternal
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.psi.KtDeclaration

object DartDifferentDefaultValueChecker : IrDeclarationChecker {
    override val reports = listOf(
        ErrorsDart.DART_DIFFERENT_DEFAULT_VALUE_ON_PARAMETER_WITHOUT_DEFAULT_VALUE,
        ErrorsDart.DART_DIFFERENT_DEFAULT_VALUE_ON_NON_EXTERNAL
    )

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration !is IrValueParameter) return

        // We don't use `hasOverriddenAnnotation` here, because we only care about the
        // original declaration.
        if (!declaration.hasAnnotation(dotlin.DartDifferentDefaultValue)) return

        val annotationSource = source.getAnnotation(
            dotlin.DartDifferentDefaultValue,
            trace.bindingContext
        )!!

        if (declaration.defaultValue == null) {
            trace.report(
                ErrorsDart.DART_DIFFERENT_DEFAULT_VALUE_ON_PARAMETER_WITHOUT_DEFAULT_VALUE.on(annotationSource)
            )
        }

        if ((declaration.parent as? IrFunction)?.isDotlinExternal != true) {
            trace.report(
                ErrorsDart.DART_DIFFERENT_DEFAULT_VALUE_ON_NON_EXTERNAL.on(annotationSource)
            )
        }
    }
}