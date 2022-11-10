package org.dotlin.compiler.backend.steps.ir2ast

import com.intellij.psi.PsiElement
import org.dotlin.compiler.backend.attributes.IrAttributes
import org.jetbrains.kotlin.backend.jvm.ir.getKtFile
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.IrConstKind.String
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.psi.*
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

private fun IrExpression.isEquivalentTo(ktExpression: KtExpression): Boolean {
    var startOffset: Int = startOffset
    var endOffset: Int = endOffset

    // For `IrStringConcatenation`, the offsets are actually already correct, quotes
    // don't have to be removed.
    if (this !is IrStringConcatenation) {
        val correction = when (ktExpression) {
            // IrConst<String>s always include offsets without the quotes.
            is KtStringTemplateExpression -> when {
                // It's a triple-quoted string.
                ktExpression.text.startsWith("\"\"\"") -> 3
                // Single quoted string.
                else -> 1
            }
            else -> 0
        }

        startOffset -= correction
        endOffset += correction
    }

    return ktExpression.let {
        startOffset == it.startOffset &&
                endOffset == it.endOffset &&
                matchesType(it)
    }
}

private fun IrExpression.matchesType(ktExpression: KtExpression) = ktExpression.let {
    when (this) {
        is IrFunctionAccessExpression -> it is KtCallExpression
        is IrGetValue -> it is KtNameReferenceExpression
        is IrReturn -> it is KtReturnExpression
        is IrWhen -> it is KtWhenExpression || it is KtIfExpression
        is IrTry -> it is KtTryExpression
        is IrConst<*> -> when (kind) {
            String -> it is KtStringTemplateExpression
            else -> it is KtConstantExpression
        }
        is IrStringConcatenation -> it is KtStringTemplateExpression
        is IrBlock -> it is KtBlockExpression
        is IrBreakContinue -> it is KtBreakExpression || it is KtContinueExpression
        is IrThrow -> it is KtThrowExpression
        // TODO: Support more types
        else -> false
    }
}