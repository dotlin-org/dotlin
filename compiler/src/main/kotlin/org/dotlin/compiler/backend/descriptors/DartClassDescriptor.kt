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
import org.dotlin.compiler.dart.element.DartClassElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.impl.ClassDescriptorImpl
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.AbstractClassTypeConstructor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeConstructor
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner

class DartClassDescriptor(
    override val element: DartClassElement,
    override val context: DartDescriptorContext,
    container: DeclarationDescriptor,
) : ClassDescriptorImpl(
    container,
    element.kotlinName,
    element.kotlinModality,
    ClassKind.CLASS,
    emptyList(), // TODO: superTypes
    SourceElement.NO_SOURCE, // TODO: SourceElement
    false,
    context.storageManager,
), DartDescriptor {
    private inner class DartClassTypeConstructor : AbstractClassTypeConstructor(context.storageManager) {
        private val superTypes by storageManager.createLazyValue {
            element.superTypes.map { it.toKotlinType() }
        }

        override fun computeSupertypes(): Collection<KotlinType> = superTypes
        override fun getDeclarationDescriptor(): ClassDescriptor = this@DartClassDescriptor
        override fun getParameters(): List<TypeParameterDescriptor> = declaredTypeParameters
        override fun isDenotable(): Boolean = true
        override val supertypeLoopChecker: SupertypeLoopChecker = SupertypeLoopChecker.EMPTY
    }

    private val _typeConstructor by storageManager.createLazyValue { DartClassTypeConstructor() }

    override fun getTypeConstructor(): TypeConstructor = _typeConstructor

    private val _visibility = element.kotlinVisibility
    override fun getVisibility() = _visibility

    private fun memberScope(static: Boolean) = DartMemberScope(
        owner = this,
        context,
        elements = buildList {
            addAll(element.properties)
            addAll(element.methods)

            if (static) {
                addAll(element.constructors.filter { it.name.isNotEmpty })
            }
        }.filter { it.isStatic == static },
    )

    private val instanceMemberScope by storageManager.createLazyValue { memberScope(static = false) }

    override fun getUnsubstitutedMemberScope(): MemberScope = instanceMemberScope
    override fun getUnsubstitutedMemberScope(kotlinTypeRefiner: KotlinTypeRefiner): MemberScope = instanceMemberScope

    private val staticMemberScope by storageManager.createLazyValue { memberScope(static = true) }

    override fun getStaticScope(): MemberScope = staticMemberScope

    private val typeParameters by storageManager.createLazyValue {
        element.typeParameters.kotlinTypeParametersOf(this)
    }

    override fun getDeclaredTypeParameters() = typeParameters

    private val _constructors by storageManager.createLazyValue {
        element.constructors
            .filter { it.name.isEmpty }
            .map {
                DartConstructorDescriptor(
                    element = it,
                    context,
                    container = this,
                )
            }
    }

    override fun getConstructors(): List<DartConstructorDescriptor> = _constructors

    override fun getUnsubstitutedPrimaryConstructor(): ClassConstructorDescriptor? {
        TODO("Not yet implemented")
    }
}