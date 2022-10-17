package org.dotlin.compiler.backend.steps.src2ir.analyze.suppress

import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors

object InlineSuppressor : SubSuppressor {
    override fun Diagnostic.isSuppressed(): Boolean {
        // We don't need `noinline`.
        return factory == Errors.USAGE_IS_NOT_INLINABLE || factory == Errors.NOTHING_TO_INLINE
    }
}