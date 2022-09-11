package org.dotlin.compiler.backend.steps.src2ir.analyze.suppress

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters2
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.types.SimpleType

object ThrowSuppressor : SubSuppressor {
    // Suppress Throwable expected error, we can throw anything (except null).
    override fun Diagnostic.isSuppressed(): Boolean {
        val isConstant = factory == Errors.CONSTANT_EXPECTED_TYPE_MISMATCH
        if ((factory != Errors.TYPE_MISMATCH && !isConstant) ||
            this !is DiagnosticWithParameters2<*, *, *>
        ) {
            return false
        }

        val expectedType = when {
            isConstant -> b as? SimpleType ?: return false
            else -> a as? SimpleType ?: return false
        }

        return KotlinBuiltIns.isThrowableOrNullableThrowable(expectedType)
    }
}