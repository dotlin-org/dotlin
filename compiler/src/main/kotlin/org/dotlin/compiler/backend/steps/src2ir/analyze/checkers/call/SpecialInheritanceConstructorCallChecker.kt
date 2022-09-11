package org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.call

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall

object SpecialInheritanceConstructorCallChecker : CallChecker{
    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        TODO("Not yet implemented")
    }

}