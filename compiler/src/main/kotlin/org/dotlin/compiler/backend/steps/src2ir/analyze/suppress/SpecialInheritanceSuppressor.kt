package org.dotlin.compiler.backend.steps.src2ir.analyze.suppress

import org.dotlin.compiler.backend.util.isImplicitInterfaceConstructorCall
import org.dotlin.compiler.backend.util.isSpecialInheritanceConstructorCall
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.resolve.BindingTraceContext

class SpecialInheritanceSuppressor(
    private val trace: BindingTraceContext,
) : SubSuppressor {
    override fun Diagnostic.isSuppressed() =
        isManySuperTypesError() || isInterfaceWithSuperClassError() || isInterfacesCannotInitializeSuperTypesError()

    private fun Diagnostic.isManySuperTypesError(): Boolean {
        if (factory != Errors.MANY_CLASSES_IN_SUPERTYPE_LIST) return false

        return psiElement.isSpecialInheritanceConstructorCall(trace.bindingContext)
    }

    private fun Diagnostic.isInterfaceWithSuperClassError(): Boolean {
        if (factory != Errors.INTERFACE_WITH_SUPERCLASS) return false

        return psiElement.isImplicitInterfaceConstructorCall(trace.bindingContext)
    }

    private fun Diagnostic.isInterfacesCannotInitializeSuperTypesError(): Boolean {
        if (factory != Errors.SUPERTYPE_INITIALIZED_IN_INTERFACE) return false

        return psiElement.parent?.children?.get(0)?.children?.get(0)
            ?.isImplicitInterfaceConstructorCall(trace.bindingContext) == true
    }
}