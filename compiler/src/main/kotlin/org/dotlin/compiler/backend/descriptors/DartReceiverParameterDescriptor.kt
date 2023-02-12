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

import org.dotlin.compiler.dart.element.DartElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.receivers.AbstractReceiverValue
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeSubstitutor

class DartReceiverParameterDescriptor(
    name: Name,
    type: KotlinType,
    private val owner: DeclarationDescriptor,
    override val context: DartDescriptorContext,
) : DartParameterDescriptor, ReceiverParameterDescriptor {
    override val element: DartElement
        get() = throw UnsupportedOperationException("DartReceiverParameter has no element")

    private val _name = name
    override fun getName(): Name = _name

    private val _type = type
    override fun getReturnType(): KotlinType = _type
    override fun getType(): KotlinType = _type

    private val receiverValue by lazy { DartReceiverValue(_type) }
    override fun getValue(): ReceiverValue = receiverValue

    override fun getOriginal(): ParameterDescriptor = this

    override fun getContainingDeclaration(): DeclarationDescriptor = owner
    override fun getSource(): SourceElement = SourceElement.NO_SOURCE // TODO

    override fun getVisibility(): DescriptorVisibility {
        TODO("Not yet implemented")
    }

    override fun substitute(substitutor: TypeSubstitutor): ReceiverParameterDescriptor? {
        TODO("Not yet implemented")
    }

    override fun getContextReceiverParameters(): List<ReceiverParameterDescriptor> {
        TODO("Not yet implemented")
    }

    override fun getExtensionReceiverParameter(): ReceiverParameterDescriptor? {
        TODO("Not yet implemented")
    }

    override fun getDispatchReceiverParameter(): ReceiverParameterDescriptor? {
        TODO("Not yet implemented")
    }

    override fun getTypeParameters(): List<TypeParameterDescriptor> {
        return emptyList() // TODO
    }


    override fun getValueParameters(): List<ValueParameterDescriptor> {
        return emptyList() // TODO?
    }

    override fun getOverriddenDescriptors(): Collection<CallableDescriptor> {
        return emptyList() // TODO?
    }

    override fun <V : Any?> getUserData(key: CallableDescriptor.UserDataKey<V>?): V? {
        TODO("Not yet implemented")
    }

    override fun copy(newOwner: DeclarationDescriptor): ReceiverParameterDescriptor {
        TODO("Not yet implemented")
    }

    override val annotations: Annotations
        get() = Annotations.EMPTY // TODO

    override fun <R : Any?, D : Any?> accept(visitor: DeclarationDescriptorVisitor<R, D>, data: D): R =
        visitor.visitReceiverParameterDescriptor(this, data)

    override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>) {
        visitor.visitReceiverParameterDescriptor(this, null)
    }
}

class DartReceiverValue(type: KotlinType, original: DartReceiverValue? = null) : AbstractReceiverValue(type, original) {
    override fun replaceType(newType: KotlinType): ReceiverValue = DartReceiverValue(newType, original = this)
}