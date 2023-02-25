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
import org.dotlin.compiler.dart.element.DartPropertyElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PropertyDescriptorImpl
import org.jetbrains.kotlin.resolve.DescriptorUtils

class DartPropertyDescriptor(
    override val element: DartPropertyElement,
    override val context: DartDescriptorContext,
    container: DeclarationDescriptor,
) : PropertyDescriptorImpl(
    container,
    null,
    Annotations.EMPTY, // TODO: Annotations
    element.kotlinModality,
    element.kotlinVisibility,
    !element.isFinal,
    element.kotlinName,
    element.callableMemberDescriptorKind,
    SourceElement.NO_SOURCE, // TODO: SourceElement
    element.isLate,
    element.isConst,
    false,
    false,
    false, // TODO?: isExternal
    false,
), DartDescriptor {
    init {
        // Must be called before initialize, because the accessors depend on the property's type.
        setType(
            element.type.toKotlinType(),
            emptyList(),
            DescriptorUtils.getDispatchReceiverParameterIfNeeded(container),
            null, // TODO: Extension receiver parameter
            emptyList(),
        )

        // TODO?: Backing field
        initialize(
            element.getter?.let { DartPropertyGetterDescriptor(it, context, this) },
            element.setter?.let { DartPropertySetterDescriptor(it, context, this) }
        )
    }
}