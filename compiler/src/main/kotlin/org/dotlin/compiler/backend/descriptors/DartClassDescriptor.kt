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

import org.dotlin.compiler.backend.descriptors.type.DartInterfaceTypeConstructor
import org.dotlin.compiler.backend.descriptors.type.DartSimpleType
import org.dotlin.compiler.backend.steps.src2ir.DotlinModule
import org.dotlin.compiler.dart.element.DartClassElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.*

class DartClassDescriptor(
    override val element: DartClassElement,
    override val module: DotlinModule,
    override val container: DeclarationDescriptor,
    private val original: DartClassDescriptor? = null,
    override val storageManager: StorageManager,
) : LazyDartMemberDescriptor(storageManager), ClassDescriptor {
    override fun getOriginal() = original ?: this

    override fun getContainingDeclaration() = container

    override fun <R : Any?, D : Any?> accept(visitor: DeclarationDescriptorVisitor<R, D>, data: D): R =
        visitor.visitClassDescriptor(this, data)

    override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>) {
        visitor.visitClassDescriptor(this, null)
    }

    override fun getSource(): SourceElement = SourceElement.NO_SOURCE

    override fun getTypeConstructor(): DartInterfaceTypeConstructor =
        DartInterfaceTypeConstructor(descriptor = this, module.builtIns)

    @get:JvmName("_defaultType")
    private val defaultType by lazy {
        DartSimpleType(
            typeConstructor,
            arguments = emptyList(), // TODO
            attributes = TypeAttributes.Empty, // TODO
            isMarkedNullable = false
        )
    }

    override fun getDefaultType(): SimpleType = defaultType

    override fun substitute(substitutor: TypeSubstitutor): ClassifierDescriptorWithTypeParameters {
        TODO("Not yet implemented")
    }

    override fun isInner() = false

    override fun getDeclaredTypeParameters(): List<TypeParameterDescriptor> {
        return emptyList() // TODO
    }

    override fun getMemberScope(typeArguments: MutableList<out TypeProjection>): MemberScope {
        TODO("Not yet implemented")
    }

    override fun getMemberScope(typeSubstitution: TypeSubstitution): MemberScope {
        TODO("Not yet implemented")
    }


    private val instanceMemberScope by storageManager.createLazyValue {
        DartMemberScope(owner = this, module, elements = element.constructors + element.fields, storageManager)
    }

    override fun getUnsubstitutedMemberScope(): MemberScope = instanceMemberScope

    override fun getUnsubstitutedInnerClassesScope(): MemberScope {
        TODO("Not yet implemented")
    }

    override fun getStaticScope(): MemberScope {
        return MemberScope.Empty // TODO
    }

    @get:JvmName("_constructors")
    private val constructors by storageManager.createLazyValue {
        instanceMemberScope.getContributedDescriptors().filterIsInstance<DartConstructorDescriptor>()
    }

    override fun getConstructors(): List<DartConstructorDescriptor> = constructors

    override fun getCompanionObjectDescriptor(): ClassDescriptor? = null

    override fun getKind(): ClassKind = ClassKind.CLASS // TODO

    override fun isCompanionObject() = false
    override fun isData() = false
    override fun isInline() = false
    override fun isFun() = false
    override fun isValue() = false

    private val thisReceiver by lazy {
        DartReceiverParameterDescriptor(Name.identifier("this"), type = defaultType, owner = this, module)
    }

    override fun getThisAsReceiverParameter(): ReceiverParameterDescriptor = thisReceiver

    override fun getContextReceivers(): List<ReceiverParameterDescriptor> = emptyList()

    override fun getUnsubstitutedPrimaryConstructor(): ClassConstructorDescriptor? {
        TODO("Not yet implemented")
    }

    override fun getSealedSubclasses(): Collection<ClassDescriptor> = emptyList()
    override fun getValueClassRepresentation(): ValueClassRepresentation<SimpleType>? = null
    override fun getDefaultFunctionTypeForSamInterface(): SimpleType? = null
    override fun isDefinitelyNotSamInterface(): Boolean = true
}