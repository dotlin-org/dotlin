package org.dotlin.compiler.backend.steps.ir2ast

import com.intellij.psi.PsiElement
import org.dotlin.compiler.backend.steps.ir2ast.attributes.IrAttributes
import org.jetbrains.kotlin.backend.jvm.ir.getKtFile
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset

object IrExpressionSourceMapper {
    /**
     * Maps [IrExpression]s to their corresponding [KtExpression]s.
     *
     * Must be run before lowering.
     */
    fun run(files: Iterable<IrFile>, irAttributes: IrAttributes) {
        for (file in files) {
            val ktFile = file.getKtFile() ?: continue

            val irExpressions = mutableListOf<IrExpression>()
            val ktExpressions = mutableListOf<KtExpression>()

            file.acceptVoid(object : IrElementVisitorVoid {
                override fun visitElement(element: IrElement) {
                    element.acceptChildrenVoid(this)
                }

                override fun visitExpression(expression: IrExpression) {
                    super.visitExpression(expression)
                    irExpressions.add(expression)
                }
            })

            ktFile.acceptChildren(object : KtVisitorVoid() {
                override fun visitElement(element: PsiElement) {
                    element.acceptChildren(this)
                }

                override fun visitExpression(expression: KtExpression) {
                    super.visitExpression(expression)
                    ktExpressions.add(expression)
                }
            })

            val mapped = mapOf(
                *ktExpressions
                    .mapNotNull {
                        when (val key = irExpressions.firstOrNull { ir -> ir.isEquivalentTo(it) }) {
                            null -> null
                            else -> key to it
                        }
                    }
                    .toTypedArray()
            )

            ktExpressions
                .asSequence()
                .mapNotNull {
                    when (val key = irExpressions.firstOrNull { ir -> ir.isEquivalentTo(it) }) {
                        null -> null
                        else -> key to it
                    }
                }.forEach { (irExp, ktExp) ->
                    with(irAttributes) {
                        irExp.ktExpression = ktExp
                    }
                }
        }
    }
}

private fun IrExpression.isEquivalentTo(ktExpression: KtExpression) =
    ktExpression.let {
        startOffset == it.startOffset &&
                endOffset == it.endOffset &&
                matchesType(it)
    }

private fun IrExpression.matchesType(ktExpression: KtExpression) = ktExpression.let {
    when (this) {
        is IrFunctionAccessExpression -> it is KtCallExpression
        is IrGetValue -> it is KtNameReferenceExpression
        // TODO: Support more types
        else -> false
    }
}