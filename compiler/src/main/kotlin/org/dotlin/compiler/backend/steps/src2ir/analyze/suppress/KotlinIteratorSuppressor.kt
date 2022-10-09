package org.dotlin.compiler.backend.steps.src2ir.analyze.suppress

import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors.*

object KotlinIteratorSuppressor : SubSuppressor {
    override fun Diagnostic.isSuppressed(): Boolean {
        return factory in listOf(
            HAS_NEXT_MISSING,
            HAS_NEXT_FUNCTION_TYPE_MISMATCH,
            HAS_NEXT_FUNCTION_NONE_APPLICABLE,
            HAS_NEXT_FUNCTION_AMBIGUITY,
            NEXT_MISSING,
            NEXT_NONE_APPLICABLE,
            NEXT_AMBIGUITY
        )
    }
}