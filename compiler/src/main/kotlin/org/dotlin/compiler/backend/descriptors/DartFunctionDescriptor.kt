/*
 * Copyright 2022 Wilko Manger
 *
 * This file is part of Dotlin.
 *
 * Dotlin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dotlin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Dotlin.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.backend.descriptors.type.toKotlinType
import org.dotlin.compiler.backend.steps.src2ir.DotlinModule
import org.dotlin.compiler.dart.element.DartConstructorElement
import org.dotlin.compiler.dart.element.DartExecutableElement
import org.dotlin.compiler.dart.element.DartFunctionElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeSubstitutor

sealed class DartFunctionDescriptor : LazyDartMemberDescriptor(), DartCallableDescriptor, FunctionDescriptor {
    abstract override val element: DartExecutableElement

    override fun getContainingDeclaration() = container

    override fun <R : Any?, D : Any?> accept(visitor: DeclarationDescriptorVisitor<R, D>, data: D): R =
        visitor.visitFunctionDescriptor(this, data)

    override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>) {
        visitor.visitFunctionDescriptor(this, null)
    }

    override fun getSource(): SourceElement = SourceElement.NO_SOURCE

    override fun substitute(substitutor: TypeSubstitutor): FunctionDescriptor? {
        TODO("Not yet implemented")
    }

    override fun getContextReceiverParameters(): List<ReceiverParameterDescriptor> {
        return emptyList() // TODO
    }

    override fun getExtensionReceiverParameter(): ReceiverParameterDescriptor? {
        return null // TODO
    }

    override fun getDispatchReceiverParameter(): ReceiverParameterDescriptor? {
        return null // TODO
    }

    override fun getTypeParameters(): List<TypeParameterDescriptor> {
        return emptyList() // TODO
    }

    override fun getReturnType(): KotlinType? = element.type.returnType.toKotlinType(module)

    override fun getValueParameters(): List<ValueParameterDescriptor> {
        return emptyList()
    }

    override fun getOverriddenDescriptors(): Collection<FunctionDescriptor> {
        return emptyList() // TODO
    }

    override fun <V : Any?> getUserData(p0: CallableDescriptor.UserDataKey<V>?): V? {
        return null // TODO?
    }

    override fun setOverriddenDescriptors(p0: MutableCollection<out CallableMemberDescriptor>) {
        // TODO
    }

    override fun getKind() = CallableMemberDescriptor.Kind.DECLARATION // TODO?

    override fun isOperator() = false

    override fun getInitialSignatureDescriptor(): FunctionDescriptor? = this // TODO?

    override fun isHiddenToOvercomeSignatureClash(): Boolean = false

    override fun isInfix(): Boolean = false

    override fun isInline(): Boolean {
        return false // TODO: Read @pragma annotations for inline
    }

    override fun isTailrec(): Boolean = false

    override fun isHiddenForResolutionEverywhereBesideSupercalls(): Boolean = false // TODO?

    override fun isSuspend() = element.isAsync // TODO
}

class DartSimpleFunctionDescriptor(
    override val element: DartFunctionElement,
    override val module: DotlinModule,
    override val container: DeclarationDescriptor,
    private val original: DartSimpleFunctionDescriptor? = null
) : DartFunctionDescriptor(), SimpleFunctionDescriptor {
    override fun getOriginal() = original ?: this

    override fun copy(
        p0: DeclarationDescriptor?,
        p1: Modality?,
        p2: DescriptorVisibility?,
        p3: CallableMemberDescriptor.Kind?,
        p4: Boolean
    ): SimpleFunctionDescriptor {
        TODO("Not yet implemented")
    }

    override fun newCopyBuilder(): FunctionDescriptor.CopyBuilder<out SimpleFunctionDescriptor> {
        TODO("Not yet implemented")
    }
}

class DartConstructorDescriptor(
    override val element: DartConstructorElement,
    override val module: DotlinModule,
    override val container: DartClassDescriptor,
    private val original: DartConstructorDescriptor? = null
) : DartFunctionDescriptor(), ConstructorDescriptor {
    override fun getOriginal() = original ?: this

    override fun getContainingDeclaration() = container

    override fun substitute(substitutor: TypeSubstitutor): ConstructorDescriptor? {
        TODO("Not yet implemented")
    }

    override fun getReturnType(): KotlinType {
        TODO("Not yet implemented")
    }

    override fun copy(
        p0: DeclarationDescriptor?,
        p1: Modality?,
        p2: DescriptorVisibility?,
        p3: CallableMemberDescriptor.Kind?,
        p4: Boolean
    ): ConstructorDescriptor {
        TODO("Not yet implemented")
    }

    override fun newCopyBuilder(): FunctionDescriptor.CopyBuilder<out FunctionDescriptor> {
        TODO("Not yet implemented")
    }

    override fun getConstructedClass() = container

    override fun isPrimary() = false // TODO

}

