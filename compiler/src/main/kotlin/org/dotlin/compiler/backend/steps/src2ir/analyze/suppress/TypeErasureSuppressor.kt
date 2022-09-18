package org.dotlin.compiler.backend.steps.src2ir.analyze.suppress

import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors

object TypeErasureSuppressor : SubSuppressor {
    override fun Diagnostic.isSuppressed(): Boolean {
        return factory == Errors.CANNOT_CHECK_FOR_ERASED
    }
}