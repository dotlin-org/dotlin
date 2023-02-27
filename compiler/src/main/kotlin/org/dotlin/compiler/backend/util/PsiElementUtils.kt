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

package org.dotlin.compiler.backend.util

import org.dotlin.compiler.backend.steps.ir2ast.ir.correspondingProperty
import org.jetbrains.kotlin.backend.jvm.ir.psiElement
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

val IrDeclaration.ktDeclaration: KtDeclaration?
    get() = psiElement as? KtDeclaration ?: when (this) {
        is IrAttributeContainer -> attributeOwnerId.safeAs<IrDeclaration>()?.psiElement as? KtDeclaration
        is IrField -> correspondingProperty?.ktDeclaration
        else -> null
    }

fun KtAnnotationEntry.getFqName(bindingContext: BindingContext) = getResolvedCall(bindingContext)
    ?.resultingDescriptor
    ?.containingDeclaration
    ?.fqNameSafe

fun KtStringTemplateExpression.isTripleQuoted() = text.startsWith("\"\"\"")

fun KtImportDirective.descriptor(context: BindingContext) =
    context.get(BindingContext.REFERENCE_TARGET, declarationReference)

private val KtImportDirective.declarationReference: KtNameReferenceExpression
    get() = when (val firstChild = children[0]) {
        is KtNameReferenceExpression -> firstChild
        is KtDotQualifiedExpression -> firstChild.lastChild as KtNameReferenceExpression
        else -> throw UnsupportedOperationException("Unsupported child: ${firstChild::class.simpleName}")
    }