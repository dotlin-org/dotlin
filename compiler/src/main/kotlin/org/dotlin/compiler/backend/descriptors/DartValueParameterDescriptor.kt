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
import org.dotlin.compiler.dart.element.DartParameterElement
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl

class DartValueParameterDescriptor(
    container: FunctionDescriptor,
    override val index: Int,
    override val annotations: Annotations,
    override val element: DartParameterElement,
    override val context: DartDescriptorContext,
    original: DartValueParameterDescriptor? = null,
) : ValueParameterDescriptorImpl(
    container,
    original,
    index,
    annotations,
    element.kotlinName,
    element.type.toKotlinType(context),
    element.hasDefaultValue,
    isCrossinline = false,
    isNoinline = false,
    varargElementType = null,
    SourceElement.NO_SOURCE, // TODO: SourceElement
), DartDescriptor {
    // Dart parameters are vars.
    override fun isVar(): Boolean = true
}