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

import org.dotlin.compiler.backend.steps.ir2ast.ir.isBackingField
import org.dotlin.compiler.backend.steps.ir2ast.ir.isNullableLong
import org.dotlin.compiler.backend.steps.ir2ast.ir.type
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtTypeReference

object LongTypeReferenceChecker : IrDeclarationChecker {
    override val reports = listOf(ErrorsDart.LONG_REFERENCE, ErrorsDart.IMPLICIT_LONG_REFERENCE)

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        // Stdlib can use Long.
        if (isCurrentModuleBuiltIns) return
        // Properties are handled as IrProperty.
        if (declaration is IrField && declaration.isBackingField) return
        if (declaration is IrFunction && declaration.isPropertyAccessor) return

        val type = when (declaration) {
            is IrValueDeclaration -> declaration.type
            is IrProperty -> declaration.type
            is IrFunction -> declaration.returnType
            else -> return
        }

        if (type.isLong() || type.isNullableLong()) {
            when (val typeNameReference = source.typeNameReference) {
                null -> {
                    val kind = when (declaration) {
                        is IrVariable -> "variable"
                        is IrField -> "field"
                        is IrProperty -> "property"
                        is IrFunction -> when (declaration.parentClassOrNull) {
                            null -> "function"
                            else -> "method"
                        }
                        else -> return
                    }

                    trace.report(ErrorsDart.IMPLICIT_LONG_REFERENCE.on(source, kind))
                }
                else -> {
                    trace.report(ErrorsDart.LONG_REFERENCE.on(typeNameReference))
                }
            }
        }
    }
}

private val KtDeclaration.typeNameReference: KtExpression?
    get() = children.filterIsInstance<KtTypeReference>()
        .firstOrNull()?.typeElement?.children?.firstOrNull() as? KtNameReferenceExpression