package org.dotlin.compiler.backend.steps.src2ir.analyze.suppress

import org.dotlin.compiler.backend.dotlin
import org.jetbrains.kotlin.cfg.getDeclarationDescriptorIncludingConstructors
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters2
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.resolve.BindingTraceContext

class ConstSuppressor(private val trace: BindingTraceContext) : SubSuppressor {
    override fun Diagnostic.isSuppressed(): Boolean {
        return isWrongModifierTargetError() ||
                isTypeCannotBeUsedForConstValError() ||
                isConstValWithNonConstInitializerError()
    }

    // This is always suppressed, we analyze whether const vals are valid ourselves later.
    private fun Diagnostic.isTypeCannotBeUsedForConstValError(): Boolean =
        factory == Errors.TYPE_CANT_BE_USED_FOR_CONST_VAL

    // This is always suppressed, we analyze whether const vals are valid ourselves later.
    private fun Diagnostic.isConstValWithNonConstInitializerError(): Boolean =
        factory == Errors.CONST_VAL_WITH_NON_CONST_INITIALIZER

    private fun Diagnostic.isWrongModifierTargetError(): Boolean {
        if ((factory != Errors.WRONG_MODIFIER_TARGET) || this !is DiagnosticWithParameters2<*, *, *>) {
            return false
        }

        val modifier = a as? KtModifierKeywordToken ?: return false

        if (modifier != KtTokens.CONST_KEYWORD) return false

        val target = b as? String ?: return false

        val descriptor by lazy {
            (psiElement.parent.parent as KtFunction).getDeclarationDescriptorIncludingConstructors(trace.bindingContext)
        }

        return when (target) {
            "member function" -> descriptor?.annotations?.hasAnnotation(dotlin.DartConstructor) == true
            "constructor", "local variable", "top level function" -> true
            else -> false
        }
    }
}