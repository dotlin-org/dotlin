package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.backend.descriptors.annotation.DartInteropAnnotationClassDescriptor
import org.dotlin.compiler.dart.element.DartElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

val DeclarationDescriptor.dartElement: DartElement?
    get() = when (this) {
        is DartDescriptor -> element
        is DartInteropAnnotationClassDescriptor<*> -> from.element
        else -> null
    }

@Suppress("UNCHECKED_CAST")
fun <E : DartElement> DeclarationDescriptor.dartElementAs(): E? = dartElement as? E