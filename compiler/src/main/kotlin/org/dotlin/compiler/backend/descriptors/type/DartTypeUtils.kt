/*
 * Copyright 2023 Wilko Manger
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

import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.descriptors.DartDescriptorContext
import org.dotlin.compiler.dart.element.*
import org.jetbrains.kotlin.types.*

context(DartDescriptor)
fun DartType.toKotlinType(): KotlinType = toKotlinType(context)

// TODO?: Cache
fun DartType.toKotlinType(context: DartDescriptorContext): KotlinType {
    val module = context.module
    val builtIns = context.module.builtIns
    return when (this) {
        is DartInterfaceType -> {
            fun dartCore(className: String) = "dart:core;dart:core/${className.lowercase()}.dart;$className"

            when (elementLocation.toString()) {
                dartCore("bool") -> builtIns.booleanType
                dartCore("int") -> builtIns.intType
                dartCore("double") -> builtIns.doubleType
                dartCore("String") -> builtIns.stringType
                dartCore("num") -> builtIns.numberType
                dartCore("Object") -> builtIns.anyType
                dartCore("Never") -> builtIns.nothingType
                // TODO: Comparable
                // TODO: Handle collection types
                else -> DartTypeFactory.simpleType(this, context)
            }
        }

        DartDynamicType -> DynamicType(
            builtIns = builtIns,
            attributes = TypeAttributes.Empty
        )

        is DartFunctionType -> module.builtIns
            .getFunction(parameters.size)
            .defaultType
            .replace(newArguments = buildList {
                add(TypeProjectionImpl(returnType.toKotlinType(context)))
                addAll(parameters.map { TypeProjectionImpl(it.type.toKotlinType(context)) })
            })

        is DartTypeParameterType -> DartTypeFactory.simpleType(this, context)
        DartNeverType -> builtIns.nothingType
        DartVoidType -> builtIns.unitType // TODO
    }
}