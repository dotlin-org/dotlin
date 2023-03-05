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

package org.dotlin.compiler.backend.descriptors.type

import org.dotlin.compiler.backend.descriptors.DartDescriptorContext
import org.dotlin.compiler.backend.descriptors.dartElement
import org.dotlin.compiler.dart.element.*
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.TypeAttributes
import org.jetbrains.kotlin.types.TypeProjectionImpl

object DartTypeFactory {
    fun simpleType(
        type: DartTypeWithElement,
        context: DartDescriptorContext
    ): SimpleType {
        val descriptor: ClassifierDescriptor = run {
            // For type parameter types, we get the parent element, to then find the type parameter in the descriptor.
            val element = context.elementLocator.locate<DartDeclarationElement>(
                location = when (type) {
                    is DartInterfaceType -> type.elementLocation
                    is DartTypeParameterType -> type.elementLocation.parent
                }
            )

            val (packageName, descriptorName) =  context.fqNameOf(element).let { it.parent() to it.shortName() }
            val descriptor = context.module
                .getPackage(packageName)
                .memberScope
                .getContributedDescriptors { it == descriptorName }
                .first()

            when (type) {
                is DartInterfaceType -> descriptor as ClassDescriptor
                is DartTypeParameterType -> {
                    val typeParameterElement = context.elementLocator
                        .locate<DartTypeParameterElement>(type.elementLocation)

                    val typeParameters = when (descriptor) {
                        is ClassDescriptor -> descriptor.declaredTypeParameters
                        is FunctionDescriptor -> descriptor.typeParameters
                        else -> throw UnsupportedOperationException("Unexpected descriptor: $descriptor")
                    }

                    typeParameters.first { it.dartElement == typeParameterElement }
                }
            }
        }

        return KotlinTypeFactory.simpleType(
            attributes = TypeAttributes.Empty, // TODO
            descriptor.typeConstructor,
            when (type) {
                is DartInterfaceType -> type.typeArguments.map { TypeProjectionImpl(it.toKotlinType(context)) }
                is DartTypeParameterType -> emptyList()
            },
            type.nullabilitySuffix == DartNullabilitySuffix.QUESTION_MARK,
        )
    }
}