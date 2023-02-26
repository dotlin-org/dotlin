package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.backend.descriptors.type.toKotlinType
import org.dotlin.compiler.dart.element.*
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.DECLARATION
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.SYNTHESIZED
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PRIVATE
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PUBLIC
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

private val DartNamedElement.defaultKotlinName: Name
    get() = Name.identifier(name.value)

val DartNamedElement.kotlinName: Name
    get() = when (this) {
        is DartConstructorElement -> kotlinName
        else -> defaultKotlinName
    }

val DartConstructorElement.kotlinName: Name
    get() = when {
        name.isEmpty -> SpecialNames.INIT
        else -> defaultKotlinName
    }

val DartAbstractableElement.kotlinModality: Modality
    get() = when {
        // TODO: Add @nonVirtual -> Modality.FINAL
        // TODO: (Dart 3.0) Add sealed case
        isAbstract -> Modality.ABSTRACT
        else -> Modality.OPEN
    }

val DartDeclarationElement.kotlinVisibility: DescriptorVisibility
    get() = when {
        // TODO: Add @protected -> PROTECTED
        // TODO: Add @internal -> INTERNAL
        name.isPrivate -> PRIVATE
        else -> PUBLIC
    }

context(DartDescriptor)
val DartFunctionElement.kotlinReturnType
    get() = type.returnType.toKotlinType()

val DartElement.callableMemberDescriptorKind: CallableMemberDescriptor.Kind
    get() = when {
        isSynthetic -> SYNTHESIZED
        else -> DECLARATION
    }