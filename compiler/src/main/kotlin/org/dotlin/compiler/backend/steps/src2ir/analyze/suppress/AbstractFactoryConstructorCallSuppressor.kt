package org.dotlin.compiler.backend.steps.src2ir.analyze.suppress

import org.dotlin.compiler.backend.descriptors.dartElementAs
import org.dotlin.compiler.dart.element.DartConstructorElement
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Errors.CREATING_AN_INSTANCE_OF_ABSTRACT_CLASS
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingTraceContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall

class AbstractFactoryConstructorCallSuppressor(private val trace: BindingTraceContext) : SubSuppressor {
    override fun Diagnostic.isSuppressed(): Boolean {
        if (factory != CREATING_AN_INSTANCE_OF_ABSTRACT_CLASS) return false

        val constructorCall = (this.psiElement as KtElement).getResolvedCall(trace.bindingContext) ?: return false
        val constructor = constructorCall.candidateDescriptor

        return constructor.dartElementAs<DartConstructorElement>()?.isFactory == true
    }
}