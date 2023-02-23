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
import org.dotlin.compiler.dart.element.DartConstructorElement
import org.dotlin.compiler.dart.element.DartExecutableElement
import org.dotlin.compiler.dart.element.DartFunctionElement
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ClassConstructorDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.KotlinType

class DartSimpleFunctionDescriptor(
    override val element: DartFunctionElement,
    override val context: DartDescriptorContext,
    container: DeclarationDescriptor,
    private val original: DartSimpleFunctionDescriptor? = null,
) : SimpleFunctionDescriptorImpl(
    container,
    original,
    Annotations.EMPTY, // TODO
    element.kotlinName,
    CallableMemberDescriptor.Kind.DECLARATION, // TODO?
    SourceElement.NO_SOURCE // TODO: SourceElement
), DartDescriptor {
    init {
        initialize(
            null, // TODO
            null, // TODO
            emptyList(), // TODO
            emptyList(), // TODO
            element.kotlinValueParametersOf(this),
            element.kotlinReturnType,
            element.kotlinModality,
            element.kotlinVisibility,
        )
    }
}

class DartConstructorDescriptor(
    override val element: DartConstructorElement,
    override val context: DartDescriptorContext,
    container: DartClassDescriptor,
    original: DartConstructorDescriptor? = null,
) : ClassConstructorDescriptorImpl(
    container,
    original,
    Annotations.EMPTY, // TODO,
    false,
    CallableMemberDescriptor.Kind.DECLARATION, // TODO
    SourceElement.NO_SOURCE
), DartDescriptor {
    init {
        initialize(
            element.kotlinValueParametersOf(this),
            element.kotlinVisibility,
        )
    }

    private val _name by storageManager.createLazyValue {
        when {
            element.name.isEmpty -> super.getName()
            else -> element.kotlinName
        }
    }

    override fun getName() = _name

    private val _returnType by storageManager.createLazyValue { element.type.returnType.toKotlinType() }

    override fun getReturnType(): KotlinType = _returnType
}

context(DartDescriptor)
private fun DartExecutableElement.kotlinValueParametersOf(container: FunctionDescriptor) =
    parameters.mapIndexed { index, param ->
        DartValueParameterDescriptor(
            container,
            index,
            Annotations.EMPTY, // TODO
            element = param,
            context,
            original = null,
        )
    }

