package org.dotlin.compiler.backend.steps.src2ir.analyze.suppress

import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters1
import org.jetbrains.kotlin.diagnostics.Errors

object LateInitSuppressor : SubSuppressor {
    override fun Diagnostic.isSuppressed(): Boolean {
        if (factory != Errors.INAPPLICABLE_LATEINIT_MODIFIER || this !is DiagnosticWithParameters1<*, *>) {
            return false
        }

        val message = a as? String ?: return false

        return message.endsWith("nullable types") ||
                message.endsWith("primitive types") ||
                message.endsWith("nullable upper bound")
    }
}