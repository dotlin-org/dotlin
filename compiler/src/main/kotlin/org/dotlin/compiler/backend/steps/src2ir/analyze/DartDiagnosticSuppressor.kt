package org.dotlin.compiler.backend.steps.src2ir.analyze

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters2
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters3
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.typeUtil.isInt
import org.jetbrains.kotlin.types.typeUtil.isLong
import org.jetbrains.kotlin.types.typeUtil.isNotNullThrowable

object DartDiagnosticSuppressor : DiagnosticSuppressor {
    override fun isSuppressed(diagnostic: Diagnostic): Boolean = diagnostic.let {
        it.isLongLiteralUsedOnIntError() ||
                it.isInferredAsLongButIntExpectedError() ||
                it.isInternalLongMemberReferenceError() ||
                it.isThrowableExpected()
    }

    // Throwable
    private fun Diagnostic.isThrowableExpected(): Boolean {
        val isConstant = factory.name == "CONSTANT_EXPECTED_TYPE_MISMATCH"
        if ((factory.name != "TYPE_MISMATCH" && !isConstant) ||
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

    private fun Diagnostic.isLongLiteralUsedOnIntError(): Boolean {
        if (factory.name != "CONSTANT_EXPECTED_TYPE_MISMATCH" ||
            this !is DiagnosticWithParameters2<*, *, *>
        ) {
            return false
        }

        val type = b as? SimpleType ?: return false
        return type.isInt()
    }

    private fun Diagnostic.isInferredAsLongButIntExpectedError(): Boolean {
        if (factory.name != "TYPE_MISMATCH" ||
            this !is DiagnosticWithParameters2<*, *, *>
        ) {
            return false
        }

        val expectedType = a as? SimpleType ?: return false
        val inferredType = b as? SimpleType ?: return false
        return expectedType.isInt() && inferredType.isLong()
    }

    private fun Diagnostic.isInternalLongMemberReferenceError(): Boolean {
        if (factory.name != "INVISIBLE_MEMBER" ||
            this !is DiagnosticWithParameters3<*, *, *, *>
        ) {
            return false
        }

        val parentClass = a as? ClassDescriptor ?: return false
        return parentClass.defaultType.isLong()
    }
}