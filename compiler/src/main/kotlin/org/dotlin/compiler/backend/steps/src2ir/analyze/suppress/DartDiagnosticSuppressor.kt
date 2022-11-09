package org.dotlin.compiler.backend.steps.src2ir.analyze.suppress

import org.dotlin.compiler.backend.steps.src2ir.markSuppressed
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.resolve.BindingTraceContext
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor

class DartDiagnosticSuppressor(
    private val trace: BindingTraceContext,
) : DiagnosticSuppressor {
    private val subSuppressors = listOf(
        NumberPrimitiveSuppressor,
        ThrowSuppressor,
        SpecialInheritanceSuppressor(trace),
        ConstSuppressor(trace),
        TypeErasureSuppressor,
        LateInitSuppressor,
        KotlinIteratorSuppressor,
        InlineSuppressor,
    )

    override fun isSuppressed(diagnostic: Diagnostic): Boolean = diagnostic.let {
        return subSuppressors.any { with(it) { diagnostic.isSuppressed() } }.also { isSuppressed ->
            if (isSuppressed) {
                trace.markSuppressed(diagnostic)
            }
        }
    }
}

interface SubSuppressor {
    fun Diagnostic.isSuppressed(): Boolean
}