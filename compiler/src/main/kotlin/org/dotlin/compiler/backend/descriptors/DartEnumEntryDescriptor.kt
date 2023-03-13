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
import org.dotlin.compiler.dart.element.DartInterfaceType
import org.dotlin.compiler.dart.element.DartPropertyElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind.ENUM_ENTRY
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.impl.ClassDescriptorImpl
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.SimpleType

class DartEnumEntryDescriptor(
    override val element: DartPropertyElement,
    override val context: DartDescriptorContext,
    private val container: ClassDescriptor,
) : ClassDescriptorImpl(
    container,
    element.kotlinName,
    element.kotlinModality,
    ENUM_ENTRY,
    emptyList(), // TODO.
    SourceElement.NO_SOURCE, // TODO: SourceElement
    false,
    context.storageManager,
), DartDescriptor {
    private val substitutedContainer by storageManager.createLazyValue {
        val dartType = element.type
        when {
            dartType is DartInterfaceType && dartType.typeArguments.isEmpty() -> container
            // An enum entry's type is derived from the containing declaration, the enum itself. Dart supports generic
            // enum entries, which means we have to pretend the container has the correct type, so
            // the Kotlin type checker assumes the full, correct generic type.
            else -> object : ClassDescriptor by container {
                private val _defaultType = element.type.toKotlinType() as SimpleType
                override fun getDefaultType(): SimpleType = _defaultType
            }
        }
    }

    override fun getContainingDeclaration() = substitutedContainer

    private val _visibility = element.kotlinVisibility
    override fun getVisibility() = _visibility

    override fun getUnsubstitutedMemberScope(): MemberScope = MemberScope.Empty
}