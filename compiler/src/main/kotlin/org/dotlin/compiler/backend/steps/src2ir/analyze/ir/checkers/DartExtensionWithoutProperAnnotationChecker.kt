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

import org.dotlin.compiler.backend.hasDartExtensionNameAnnotation
import org.dotlin.compiler.backend.steps.ir2ast.ir.isExtension
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.EXTENSION_WITHOUT_EXPLICIT_DART_EXTENSION_NAME_IN_PUBLIC_PACKAGE
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.psi.KtDeclaration

object DartExtensionWithoutProperAnnotationChecker : IrDeclarationChecker {
    override val reports = listOf(EXTENSION_WITHOUT_EXPLICIT_DART_EXTENSION_NAME_IN_PUBLIC_PACKAGE)

    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        // TODO: Report if mixing Dart and Kotlin code
        if (!dartProject.isPublishable) return
        if (!declaration.isExtension) return
        if (declaration !is IrSimpleFunction && declaration !is IrProperty) return

        if (!declaration.hasDartExtensionNameAnnotation()) {
            trace.report(
                EXTENSION_WITHOUT_EXPLICIT_DART_EXTENSION_NAME_IN_PUBLIC_PACKAGE.on(source)
            )
        }
    }
}