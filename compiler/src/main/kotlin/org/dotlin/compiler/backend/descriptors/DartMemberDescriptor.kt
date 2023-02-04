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

@file:Suppress("INAPPLICABLE_JVM_NAME")

package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.dart.element.*
import org.jetbrains.kotlin.descriptors.*

interface DartMemberDescriptor : DartDeclarationDescriptor, MemberDescriptor {
    /**
     * Must be a [DartInterfaceElement] or [DartInterfaceMemberElement].
     */
    override val element: DartDeclarationElement

    override val container: DeclarationDescriptor
    override fun getContainingDeclaration(): DeclarationDescriptor = container

    override fun isExpect() = false
    override fun isActual() = false
    override fun isExternal() = false // TODO

    override fun getModality(): Modality {
        val element = element
        require(element is DartAbstractableElement)

        return when {
            // TODO: Add @nonVirtual -> Modality.FINAL
            // TODO: (Dart 3.0) Add sealed case
            element.isAbstract -> Modality.ABSTRACT
            else -> Modality.OPEN
        }
    }

    override fun getVisibility(): DescriptorVisibility = when {
        // TODO: Add @protected -> PROTECTED
        // TODO: Add @internal -> INTERNAL
        element.name.isPrivate -> DescriptorVisibilities.PRIVATE
        else -> DescriptorVisibilities.PUBLIC
    }
}

abstract class LazyDartMemberDescriptor : DartMemberDescriptor {
    @get:JvmName("_name")
    private val name by lazy { super.getName() }

    @get:JvmName("_visibility")
    private val visibility by lazy { super.getVisibility() }

    @get:JvmName("_modality")
    private val modality by lazy { super.getModality() }

    override fun getVisibility() = visibility
    override fun getModality() = modality
    override fun getName() = name
}