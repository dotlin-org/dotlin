package org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.call

import com.intellij.psi.PsiElement
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.steps.src2ir.isIteratorMethod
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.firstOverridden

object KotlinIteratorMethodCallChecker : CallChecker {
    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        val descriptor = resolvedCall.resultingDescriptor
        // If it's not a call expression in the source, it's used in a loop. In that case we don't
        // want to report the diagnostic.
        if (descriptor !is FunctionDescriptor || reportOn.parent !is KtCallExpression) return

        val iteratorMethod = descriptor.firstOverridden { it.isIteratorMethod() }

        if (iteratorMethod != null) {
            context.trace.report(ErrorsDart.KOTLIN_ITERATOR_METHOD_USAGE.on(reportOn as KtExpression))
        }
    }
}