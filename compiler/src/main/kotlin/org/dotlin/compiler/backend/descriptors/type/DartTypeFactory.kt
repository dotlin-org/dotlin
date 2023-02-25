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

import org.dotlin.compiler.backend.descriptors.DartClassDescriptor
import org.dotlin.compiler.backend.descriptors.DartDescriptorContext
import org.dotlin.compiler.dart.element.DartInterfaceElement
import org.dotlin.compiler.dart.element.DartInterfaceType
import org.dotlin.compiler.dart.element.DartNullabilitySuffix
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.types.ClassTypeConstructorImpl
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.TypeAttributes

object DartTypeFactory {
    fun simpleType(element: DartInterfaceElement, nullable: Boolean, context: DartDescriptorContext): SimpleType {
        val descriptor = context.fqNameOf(element).let { fqName ->
            context.module
                .getPackage(fqName.parent())
                .fragments
                .firstNotNullOf {
                    it.getMemberScope()
                        .getContributedClassifier(fqName.shortName(), NoLookupLocation.FROM_BACKEND)
                            as? DartClassDescriptor
                }
        }

        return KotlinTypeFactory.simpleType(
            attributes = TypeAttributes.Empty, // TODO
            ClassTypeConstructorImpl(
                descriptor,
                emptyList(), // TODO
                listOf(context.module.builtIns.anyType), // TODO
                context.storageManager,
            ),
            arguments = emptyList(), // TODO
            nullable,
        )
    }

    fun simpleType(type: DartInterfaceType, context: DartDescriptorContext) =
        simpleType(
            context.elementLocator.locate(type.elementLocation),
            type.nullabilitySuffix == DartNullabilitySuffix.QUESTION_MARK,
            context
        )

}