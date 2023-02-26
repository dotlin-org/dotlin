package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.dart.element.DartElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

val DeclarationDescriptor.dartElement: DartElement?
    get() = (this as? DartDescriptor)?.element

@Suppress("UNCHECKED_CAST")
fun <E : DartElement> DeclarationDescriptor.dartElementAs(): E? = dartElement as? E