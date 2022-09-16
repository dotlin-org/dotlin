package org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.call

import com.intellij.psi.PsiElement
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.util.isSpecialInheritanceMarker
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtSuperTypeCallEntry
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall

object SpecialInheritanceConstructorCallChecker : CallChecker {
    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        val descriptor = resolvedCall.resultingDescriptor
        if (descriptor !is ClassConstructorDescriptor) return

        // Should be a constructor with a single parameter with the marker type.
        descriptor.valueParameters.singleOrNull{ it.type.isSpecialInheritanceMarker() } ?: return

        // As as super type is the right usage, return.
        if (reportOn.parent is KtSuperTypeCallEntry) return

        context.trace.report(ErrorsDart.SPECIAL_INHERITANCE_CONSTRUCTOR_MISUSE.on(reportOn as KtExpression))
    }
}