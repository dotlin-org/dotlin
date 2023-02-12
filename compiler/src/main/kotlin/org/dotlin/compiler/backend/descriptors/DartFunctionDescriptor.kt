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
import org.dotlin.compiler.dart.element.DartConstructorElement
import org.dotlin.compiler.dart.element.DartExecutableElement
import org.dotlin.compiler.dart.element.DartFunctionElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeSubstitutor

sealed class DartFunctionDescriptor(storageManager: StorageManager) : LazyDartMemberDescriptor(storageManager),
    DartCallableDescriptor, FunctionDescriptor {
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

    @get:JvmName("_returnType")
    private val returnType by storageManager.createLazyValue { element.type.returnType.toKotlinType() }
    override fun getReturnType(): KotlinType = returnType

    @get:JvmName("_valueParameters")
    private val valueParameters by storageManager.createLazyValue {
        element.parameters.mapIndexed { index, param ->
            DartValueParameterDescriptor(
                container = this,
                index,
                Annotations.EMPTY, // TODO
                element = param,
                context,
                original = null,
                storageManager,
            )
        }
    }

    override fun getValueParameters(): List<ValueParameterDescriptor> = valueParameters

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
    override val context: DartDescriptorContext,
    override val container: DeclarationDescriptor,
    private val original: DartSimpleFunctionDescriptor? = null,
) : DartFunctionDescriptor(context.storageManager), SimpleFunctionDescriptor {
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
    override val context: DartDescriptorContext,
    override val container: DartClassDescriptor,
    private val original: DartConstructorDescriptor? = null,
) : DartFunctionDescriptor(context.storageManager), ClassConstructorDescriptor {
    private val _name by storageManager.createLazyValue {
        when {
            element.name.isEmpty -> Name.special("<init>")
            else -> super.getName()
        }
    }

    override fun getName() = _name

    override fun getOriginal() = original ?: this

    override fun getContainingDeclaration() = container

    override fun substitute(substitutor: TypeSubstitutor): DartConstructorDescriptor? {
        TODO("Not yet implemented")
    }

    override fun copy(
        newOwner: DeclarationDescriptor,
        modality: Modality,
        visibility: DescriptorVisibility,
        kind: CallableMemberDescriptor.Kind,
        copyOverrides: Boolean
    ): DartConstructorDescriptor {
        TODO("Not yet implemented")
    }

    @get:JvmName("_returnType")
    private val returnType by storageManager.createLazyValue { element.type.returnType.toKotlinType() }
    override fun getReturnType(): KotlinType = returnType

    override fun newCopyBuilder(): FunctionDescriptor.CopyBuilder<out FunctionDescriptor> {
        TODO("Not yet implemented")
    }

    override fun getConstructedClass() = container

    override fun isPrimary() = false // TODO
}

