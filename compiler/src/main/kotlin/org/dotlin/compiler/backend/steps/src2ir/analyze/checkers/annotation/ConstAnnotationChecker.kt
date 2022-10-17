package org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.annotation

import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.util.getFqName
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.resolve.AdditionalAnnotationChecker
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall

object ConstAnnotationChecker : AdditionalAnnotationChecker {
    override fun checkEntries(
        entries: List<KtAnnotationEntry>,
        actualTargets: List<KotlinTarget>,
        trace: BindingTrace,
        annotated: KtAnnotated?,
        languageVersionSettings: LanguageVersionSettings
    ) {
        if (entries.none { it.getFqName(trace.bindingContext) == dotlin.const }) return
        val expression = annotated?.children?.last { it is KtExpression } as? KtExpression ?: return

        when (val calledFunction = expression.getCalledDescriptor(trace.bindingContext)) {
            null -> trace.report(ErrorsDart.ONLY_FUNCTION_AND_CONSTRUCTOR_CALLS_CAN_BE_CONST.on(expression))
            else -> if (!calledFunction.isDartConst()) {
                trace.report(
                    ErrorsDart.CONST_WITH_NON_CONST.on(
                        expression,
                        when (calledFunction) {
                            is ConstructorDescriptor -> "constructor"
                            else -> "function"
                        }
                    )
                )
            }
        }
    }
}

private fun KtExpression.getCalledDescriptor(bindingContext: BindingContext): FunctionDescriptor? {
    return getResolvedCall(bindingContext)?.resultingDescriptor as? FunctionDescriptor
}

private fun FunctionDescriptor.isDartConst(): Boolean {
    val psi = findPsi() as? KtModifierListOwner ?: return false
    return psi.hasModifier(KtTokens.CONST_KEYWORD)
}