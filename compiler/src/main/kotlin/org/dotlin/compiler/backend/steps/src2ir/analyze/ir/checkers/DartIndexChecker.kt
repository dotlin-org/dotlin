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
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.DART_INDEX_CONFLICT
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.DART_INDEX_OUT_OF_BOUNDS
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.getAnnotation
import org.dotlin.compiler.backend.util.dartIndex
import org.jetbrains.kotlin.backend.jvm.ir.psiElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtParameter

object DartIndexChecker : IrDeclarationChecker {
    override val reports = listOf(DART_INDEX_OUT_OF_BOUNDS, DART_INDEX_CONFLICT)

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration !is IrFunction) return

        val paramDartIndexesToAnnotationSources = declaration.valueParameters.map {
            it.dartIndex to when (val s = it.psiElement) {
                is KtParameter -> s.getAnnotation(dotlin.DartIndex, trace.bindingContext)
                // If the source is not a KtParameter, it's something else like the `this` parameter of a class.
                else -> null
            }
        }


        for ((dartIndex, annotationSource) in paramDartIndexesToAnnotationSources) {
            if (annotationSource == null) continue

            val maxIndex = declaration.valueParameters.size - 1
            if (dartIndex < 0 || dartIndex > maxIndex) {
                trace.report(
                    ErrorsDart.DART_INDEX_OUT_OF_BOUNDS.on(
                        annotationSource,
                        maxIndex
                    )
                )
            }

            val duplicateSources = paramDartIndexesToAnnotationSources
                .mapNotNull { (i, s) ->
                    when (i) {
                        dartIndex -> s
                        else -> null
                    }
                }

            if (duplicateSources.size > 1) {
                trace.report(ErrorsDart.DART_INDEX_CONFLICT.on(annotationSource))
            }
        }
    }
}