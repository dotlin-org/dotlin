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

import org.dotlin.compiler.dart.element.DartPropertyAccessorElement
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PropertyGetterDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.PropertySetterDescriptorImpl

class DartPropertyGetterDescriptor(
    override val element: DartPropertyAccessorElement,
    override val context: DartDescriptorContext,
    correspondingProperty: PropertyDescriptor,
) : PropertyGetterDescriptorImpl(
    correspondingProperty,
    Annotations.EMPTY, // TODO: Annotations
    correspondingProperty.modality,
    element.kotlinVisibility,
    element.isSynthetic,
    false, // TODO?: isExternal
    false, // TODO: isInline (base on @pragma annotation
    element.callableMemberDescriptorKind,
    null,
    SourceElement.NO_SOURCE, // TODO: SourceElement
), DartDescriptor {
    init {
        initialize(null)
    }
}

class DartPropertySetterDescriptor(
    override val element: DartPropertyAccessorElement,
    override val context: DartDescriptorContext,
    correspondingProperty: PropertyDescriptor,
) : PropertySetterDescriptorImpl(
    correspondingProperty,
    Annotations.EMPTY, // TODO: Annotations
    correspondingProperty.modality,
    element.kotlinVisibility,
    element.isSynthetic,
    false, // TODO?: isExternal
    false, // TODO: isInline (base on @pragma annotation
    element.callableMemberDescriptorKind,
    null,
    SourceElement.NO_SOURCE, // TODO: SourceElement
), DartDescriptor {
    init {
        initializeDefault()
    }
}
