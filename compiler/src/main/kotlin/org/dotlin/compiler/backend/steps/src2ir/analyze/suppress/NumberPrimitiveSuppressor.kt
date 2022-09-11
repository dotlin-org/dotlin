package org.dotlin.compiler.backend.steps.src2ir.analyze.suppress

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters2
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters3
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.typeUtil.isInt
import org.jetbrains.kotlin.types.typeUtil.isLong

object NumberPrimitiveSuppressor : SubSuppressor {
    override fun Diagnostic.isSuppressed() = isLongLiteralUsedOnIntError() ||
            isInferredAsLongButIntExpectedError() ||
            isInternalLongMemberReferenceError()

    private fun Diagnostic.isLongLiteralUsedOnIntError(): Boolean {
        if (factory != Errors.CONSTANT_EXPECTED_TYPE_MISMATCH ||
            this !is DiagnosticWithParameters2<*, *, *>
        ) {
            return false
        }

        val type = b as? SimpleType ?: return false
        return type.isInt()
    }

    private fun Diagnostic.isInferredAsLongButIntExpectedError(): Boolean {
        if (factory != Errors.TYPE_MISMATCH ||
            this !is DiagnosticWithParameters2<*, *, *>
        ) {
            return false
        }

        val expectedType = a as? SimpleType ?: return false
        val inferredType = b as? SimpleType ?: return false
        return expectedType.isInt() && inferredType.isLong()
    }

    private fun Diagnostic.isInternalLongMemberReferenceError(): Boolean {
        if (factory != Errors.INVISIBLE_MEMBER ||
            this !is DiagnosticWithParameters3<*, *, *, *>
        ) {
            return false
        }

        val parentClass = a as? ClassDescriptor ?: return false
        return parentClass.defaultType.isLong()
    }
}