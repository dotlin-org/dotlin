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

import org.dotlin.compiler.backend.descriptors.*
import org.dotlin.compiler.dart.element.*
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.utils.addToStdlib.cast

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

            val (parentFqName, descriptorName) = context.fqNameOf(element).let { it.parent() to it.shortName() }
            val descriptor = run {
                val isClassMember = with(context) { element.parent is DartInterfaceElement }

                val memberScope = when {
                    isClassMember -> context.module
                        .getPackage(parentFqName.parent())
                        .memberScope
                        .getContributedClassifier(parentFqName.shortName(), NoLookupLocation.FROM_BACKEND)
                        .cast<ClassDescriptor>()
                        .unsubstitutedMemberScope

                    else -> context.module
                        .getPackage(parentFqName)
                        .memberScope
                }

                memberScope
                    .getContributedDescriptors { it == descriptorName }
                    .first()
            }

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
                is DartInterfaceType -> type.typeArguments.toKotlinTypeProjections(context)
                is DartTypeParameterType -> emptyList()
            },
            type.nullabilitySuffix.isNullable,
        )
    }

    fun flexibleType(lower: SimpleType, upper: SimpleType): FlexibleType {
        require(lower != upper)
        return DotlinFlexibleType(KotlinTypeFactory.flexibleType(lower, upper) as FlexibleType)
    }

    fun List<DartType>.toKotlinTypeProjections(context: DartDescriptorContext) =
        map { TypeProjectionImpl(it.toKotlinType(context)) }
}