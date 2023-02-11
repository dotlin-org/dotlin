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
import org.dotlin.compiler.dart.element.DartParameterElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.constants.ConstantValue
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeSubstitutor

class DartValueParameterDescriptor(
    override val container: DartCallableDescriptor,
    override val index: Int,
    override val annotations: Annotations,
    override val element: DartParameterElement,
    override val module: DotlinModule,
    private val original: DartValueParameterDescriptor? = null,
    override val storageManager: StorageManager,
) : LazyDartDeclarationDescriptor(storageManager), DartParameterDescriptor, ValueParameterDescriptor {
    // TODO?
    override fun cleanCompileTimeInitializerCache() {}

    override fun copy(newOwner: CallableDescriptor, newName: Name, newIndex: Int): ValueParameterDescriptor {
        TODO("Not yet implemented")
    }

    override fun declaresDefaultValue(): Boolean = element.hasDefaultValue

    // TODO
    override fun getCompileTimeInitializer(): ConstantValue<*>? = null

    override fun getContainingDeclaration(): DartCallableDescriptor = container

    override fun getContextReceiverParameters(): List<ReceiverParameterDescriptor> = emptyList()

    override fun getDispatchReceiverParameter(): ReceiverParameterDescriptor? = null

    override fun getExtensionReceiverParameter(): ReceiverParameterDescriptor? = null

    override fun getOriginal(): DartValueParameterDescriptor = original ?: this

    override fun getOverriddenDescriptors(): Collection<ValueParameterDescriptor> {
        TODO("Not yet implemented")
    }

    override fun getReturnType(): KotlinType = type

    override fun getSource(): SourceElement = SourceElement.NO_SOURCE // TODO

    @get:JvmName("_type")
    private val type by storageManager.createLazyValue { element.type.toKotlinType(module) }
    override fun getType(): KotlinType = type

    override fun getTypeParameters(): List<TypeParameterDescriptor> = emptyList()

    override fun <V : Any?> getUserData(key: CallableDescriptor.UserDataKey<V>?): V? {
        TODO("Not yet implemented")
    }

    override fun getValueParameters(): List<ValueParameterDescriptor> = emptyList()

    override fun getVisibility(): DescriptorVisibility {
        TODO("Not yet implemented")
    }

    override fun isConst(): Boolean {
        TODO("Not yet implemented")
    }

    // Dart parameters are vars.
    override fun isVar(): Boolean = true

    override fun substitute(substitutor: TypeSubstitutor): ValueParameterDescriptor {
        TODO("Not yet implemented")
    }

    override fun <R : Any?, D : Any?> accept(visitor: DeclarationDescriptorVisitor<R, D>, data: D): R =
        visitor.visitValueParameterDescriptor(this, data)

    override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>) {
        visitor.visitValueParameterDescriptor(this, null)
    }

    override val isCrossinline: Boolean = false
    override val isNoinline: Boolean = false

    override val varargElementType: KotlinType? = null
}