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

package org.dotlin.compiler.backend.descriptors.annotation

import org.dotlin.compiler.backend.descriptors.DartSyntheticDescriptor
import org.dotlin.compiler.backend.descriptors.dartElementAs
import org.dotlin.compiler.dart.element.DartConstructorElement
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ClassConstructorDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ClassDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.resolve.scopes.MemberScopeImpl
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.utils.Printer

class DartSyntheticAnnotationClassDescriptor(
    val from: DeclarationDescriptor,
    val container: DeclarationDescriptor,
    val storageManager: StorageManager,
) : ClassDescriptorImpl(
    container,
    from.name,
    Modality.FINAL,
    ClassKind.ANNOTATION_CLASS,
    emptyList(), // TODO: superTypes: Annotation
    SourceElement.NO_SOURCE,
    false,
    storageManager,
), DartSyntheticDescriptor {
    init {
        require(from is ClassDescriptor || from is PropertyDescriptor)
    }

    private inner class Constructor(from: ClassConstructorDescriptor?) : ClassConstructorDescriptorImpl(
        this@DartSyntheticAnnotationClassDescriptor,
        null,
        Annotations.EMPTY,
        true,
        CallableMemberDescriptor.Kind.SYNTHESIZED,
        SourceElement.NO_SOURCE,
    ), DartSyntheticDescriptor {
        init {
            initialize(
                from?.valueParameters?.map { it.copy(this, it.name, it.index) } ?: emptyList(),
                from?.visibility ?: this@DartSyntheticAnnotationClassDescriptor.visibility,
            )

            returnType = getDefaultType()
        }
    }

    private val constructor by storageManager.createLazyValue {
        // TODO: Support named constructors?
        val originalConstructor = when (from) {
            is ClassDescriptor -> from.constructors.first { ctor ->
                val element = ctor.dartElementAs<DartConstructorElement>()
                element?.isConst == true && element.name.isEmpty
            }
            else -> null
        }

        Constructor(originalConstructor)
    }

    private val constructorAsList by storageManager.createLazyValue { listOf(constructor) }

    // TODO?: Make static scope available from original? To prevent needing to import both synthetic annotation class
    // and original.

    inner class Scope : MemberScopeImpl() {
        override fun getContributedDescriptors(
            kindFilter: DescriptorKindFilter,
            nameFilter: (Name) -> Boolean
        ): Collection<DeclarationDescriptor> {
            if (!nameFilter(constructor.name) || !kindFilter.accepts(constructor)) {
                return emptyList()
            }

            return constructorAsList
        }

        override fun printScopeStructure(p: Printer) {
            p.println(constructor)
        }
    }

    private val scope = Scope()

    override fun getUnsubstitutedMemberScope(): MemberScope = scope

    override fun getConstructors(): List<ClassConstructorDescriptor> = listOf(constructor)

    override fun getUnsubstitutedPrimaryConstructor(): ClassConstructorDescriptor = constructor

    override fun getStaticScope(): MemberScope {
        return MemberScope.Empty // TODO
    }
}

val DeclarationDescriptor.isDartSyntheticPropertyAnnotation: Boolean
    get() = this is DartSyntheticAnnotationClassDescriptor && from is PropertyDescriptor