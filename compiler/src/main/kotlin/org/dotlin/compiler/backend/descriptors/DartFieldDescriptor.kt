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

import org.dotlin.compiler.backend.steps.src2ir.DotlinModule
import org.dotlin.compiler.dart.element.DartFieldElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeSubstitutor

class DartFieldDescriptor(
    override val element: DartFieldElement,
    override val module: DotlinModule,
    override val container: DartDeclarationDescriptor,
    private val _original: DartFieldDescriptor? = null,
    override val getter: PropertyGetterDescriptor? = null,
    override val setter: PropertySetterDescriptor? = null,
    override val isDelegated: Boolean = false,
) : DartMemberDescriptor, DartCallableDescriptor, PropertyDescriptor {
    override fun getOriginal() = _original ?: this
    override fun getContainingDeclaration() = container

    override fun <R : Any?, D : Any?> accept(visitor: DeclarationDescriptorVisitor<R, D>, data: D): R =
        visitor.visitPropertyDescriptor(this, data)

    override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>) {
        visitor.visitPropertyDescriptor(this, null)
    }

    override fun getSource(): SourceElement = SourceElement.NO_SOURCE // TODO

    override fun substitute(substitutor: TypeSubstitutor): DartFieldDescriptor {
        TODO("Not yet implemented")
    }

    override fun getContextReceiverParameters(): List<ReceiverParameterDescriptor> {
        TODO("Not yet implemented")
    }

    override fun getExtensionReceiverParameter(): ReceiverParameterDescriptor? {
        return null // TODO
    }

    override fun getDispatchReceiverParameter(): ReceiverParameterDescriptor? {
        return null // TODO
    }

    override fun getTypeParameters(): List<TypeParameterDescriptor> = emptyList()

    override fun getReturnType(): KotlinType? {
        return null // TODO
    }

    override fun getValueParameters(): List<ValueParameterDescriptor> = emptyList()

    override fun getOverriddenDescriptors(): Collection<PropertyDescriptor> {
        return emptyList() // TODO
    }

    override fun setOverriddenDescriptors(overriddenDescriptors: MutableCollection<out CallableMemberDescriptor>) {
        TODO("Not yet implemented")
    }

    override fun <V : Any?> getUserData(key: CallableDescriptor.UserDataKey<V>?): V? {
        return null // TODO
    }

    // TODO: Other kinds
    override fun getKind() = CallableMemberDescriptor.Kind.DECLARATION

    override fun copy(
        newOwner: DeclarationDescriptor?,
        modality: Modality?,
        visibility: DescriptorVisibility?,
        kind: CallableMemberDescriptor.Kind?,
        copyOverrides: Boolean
    ): CallableMemberDescriptor {
        // This will never get called, so no need to implement this.
        throw UnsupportedOperationException()
    }

    override fun newCopyBuilder(): CallableMemberDescriptor.CopyBuilder<out PropertyDescriptor> {
        TODO("Not yet implemented")
    }

    override fun getType(): KotlinType {
        TODO("Not yet implemented")
    }

    override fun isVar() = !element.isFinal
    override fun isConst() = element.isConst
    override fun isLateInit() = element.isLate

    override fun getCompileTimeInitializer(): ConstantValue<*>? {
        return null // TODO
    }

    override fun cleanCompileTimeInitializerCache() {
        // TODO ?
    }

    override fun isSetterProjectedOut() = false // TODO?

    override fun getAccessors(): List<PropertyAccessorDescriptor> = emptyList()
    override fun getBackingField() = null
    override fun getDelegateField() = null

    override fun getInType(): KotlinType? = null // TODO?
}