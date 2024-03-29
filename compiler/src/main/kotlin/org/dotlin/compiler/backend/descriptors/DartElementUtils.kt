package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.backend.descriptors.type.toKotlinType
import org.dotlin.compiler.dart.element.*
import org.dotlin.compiler.dart.element.DartNullabilitySuffix.QUESTION_MARK
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.DECLARATION
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind.SYNTHESIZED
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PRIVATE
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PUBLIC
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.utils.addIfNotNull

val DartInterfaceElement.superTypes: List<DartInterfaceType>
    get() = buildList {
        addIfNotNull(superType)
        addAll(superInterfaceTypes)
        addAll(superMixinTypes)
    }

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
val DartExecutableElement.kotlinReturnType
    get() = type.returnType.toKotlinType()

val DartElement.callableMemberDescriptorKind: CallableMemberDescriptor.Kind
    get() = when {
        isSynthetic -> SYNTHESIZED
        else -> DECLARATION
    }

context(DartDescriptor)
fun List<DartTypeParameterElement>.kotlinTypeParametersOf(
    container: DeclarationDescriptor
): List<TypeParameterDescriptor> = mapIndexed { index, element ->
    DartTypeParameterDescriptor(
        element,
        context,
        container,
        index
    )
}

val DartNullabilitySuffix.isNullable: Boolean
    get() = this == QUESTION_MARK

context(DartDescriptor)
val DartElement.parent: DartElement
    get() = elementLocator.locate(location.parent)

context(DartDescriptorContext)
val DartElement.parent: DartElement
    get() = elementLocator.locate(location.parent)

context(DeclarationDescriptorNonRoot)
val DartInterfaceMemberElement.kotlinReceiver: ReceiverParameterDescriptor?
    get() = when {
        !isStatic -> DescriptorUtils.getDispatchReceiverParameterIfNeeded(containingDeclaration)
        else -> null
    }