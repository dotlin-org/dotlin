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

import org.dotlin.compiler.dart.element.DartClassElement
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.impl.ClassDescriptorImpl
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.getValue

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
    private val _visibility = element.kotlinVisibility
    override fun getVisibility() = _visibility

    private val instanceMemberScope by storageManager.createLazyValue {
        DartMemberScope(
            owner = this,
            context,
            elements = element.constructors + element.properties,
        )
    }

    override fun getUnsubstitutedMemberScope(): MemberScope = instanceMemberScope

    private val _constructors by storageManager.createLazyValue {
        instanceMemberScope.getContributedDescriptors().filterIsInstance<DartConstructorDescriptor>()
    }

    override fun getConstructors(): List<DartConstructorDescriptor> = _constructors

    override fun getUnsubstitutedPrimaryConstructor(): ClassConstructorDescriptor? {
        TODO("Not yet implemented")
    }
}