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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import com.intellij.psi.PsiElement
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrExpressionLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformation
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.backend.jvm.codegen.psiElement
import org.jetbrains.kotlin.cfg.getElementParentDeclaration
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

class AnnotatedExpressionsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
        val annotations = expression
            .getAnnotationNames(bindingContext, container)
            // Turn the annotation names into descriptors.
            .mapNotNull {
                irModuleFragment.descriptor.findClassAcrossModuleDependencies(
                    ClassId(
                        it.parent(),
                        it.shortName()
                    )
                )
            }
            .map { symbolTable.referenceClass(it) }
            // Turn the descriptors into primary constructor calls.
            .map {
                IrConstructorCallImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    type = it.defaultType,
                    symbol = it.owner.primaryConstructor!!.symbol,
                    typeArgumentsCount = 0,
                    constructorTypeArgumentsCount = 0,
                    valueArgumentsCount = 0,
                    origin = null
                )
            }

        if (annotations.isEmpty()) return noChange()

        expression.annotate(annotations)

        return noChange()
    }

}

private fun IrExpression.getAnnotationNames(
    bindingContext: BindingContext,
    container: IrDeclaration,
): List<FqName> {
    val irExpression = this
    val psiElement = container.psiElement as? KtNamedFunction ?: return emptyList()

    val foundAnnotations = mutableListOf<FqName>()

    psiElement.acceptChildren(
        object : KtVisitorVoid() {
            override fun visitKtElement(element: KtElement) {
                super.visitKtElement(element)
                element.acceptChildren(this)
            }

            override fun visitAnnotatedExpression(expression: KtAnnotatedExpression) {
                super.visitAnnotatedExpression(expression)

                foundAnnotations +=
                    expression.baseExpression.let {
                        when {
                            it.isEquivalentTo(irExpression, psiElement) -> {
                                expression.annotationEntries.mapNotNull { annotation ->
                                    annotation.getResolvedCall(bindingContext)
                                        ?.resultingDescriptor
                                        ?.containingDeclaration
                                        ?.fqNameSafe
                                }
                            }
                            else -> emptyList()
                        }
                    }
            }
        }
    )

    return foundAnnotations
}

private fun KtExpression?.isEquivalentTo(irExpression: IrExpression, containerPsi: PsiElement) =
    when (this) {
        null -> false
        else -> irExpression.let {
            // TODO: Is this enough to know the PSI element and IR element are indeed referencing the same?
            startOffset == it.startOffset &&
                    endOffset == it.endOffset &&
                    containerPsi == getElementParentDeclaration()
        }
    }