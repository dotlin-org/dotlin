package org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.annotation

import org.dotlin.compiler.backend.DotlinAnnotations
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.util.getFqName
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
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
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

object ConstAnnotationChecker : AdditionalAnnotationChecker {
    override fun checkEntries(
        entries: List<KtAnnotationEntry>,
        actualTargets: List<KotlinTarget>,
        trace: BindingTrace,
        annotated: KtAnnotated?,
        languageVersionSettings: LanguageVersionSettings
    ) {
        if (entries.none { it.getFqName(trace.bindingContext) == DotlinAnnotations.const }) return
        val expression = annotated?.children?.last { it is KtExpression } as? KtExpression ?: return

        when (val calledConstructor = expression.getCalledConstructorDescriptor(trace.bindingContext)) {
            null -> trace.report(ErrorsDart.ONLY_CONSTRUCTOR_CALLS_CAN_BE_CONST.on(expression))
            else -> if (!calledConstructor.isDartConst()) {
                trace.report(ErrorsDart.CONST_WITH_NON_CONST.on(expression))
            }
        }
    }
}

private fun KtExpression.getCalledConstructorDescriptor(bindingContext: BindingContext): ConstructorDescriptor? {
    return getResolvedCall(bindingContext)?.resultingDescriptor as? ConstructorDescriptor
}

private fun ConstructorDescriptor.isDartConst(): Boolean {
    val psi = findPsi() as? KtModifierListOwner ?: return false
    return psi.hasModifier(KtTokens.CONST_KEYWORD)
}