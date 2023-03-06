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
import org.dotlin.compiler.backend.descriptors.isNullable
import org.dotlin.compiler.backend.descriptors.type.DartTypeFactory.toKotlinTypeProjections
import org.dotlin.compiler.backend.steps.src2ir.DotlinBuiltIns
import org.dotlin.compiler.dart.element.*
import org.jetbrains.kotlin.types.*

context(DartDescriptor)
fun DartType.toKotlinType(): KotlinType = toKotlinType(context)

// TODO?: Cache
fun DartType.toKotlinType(context: DartDescriptorContext): KotlinType {
    val module = context.module
    val builtIns = context.module.builtIns as DotlinBuiltIns
    return when (this) {
        is DartInterfaceType -> {
            fun toCollectionType(lower: TypeConstructor, upper: TypeConstructor): FlexibleType {
                val arguments = typeArguments.toKotlinTypeProjections(context)
                val nullable = nullabilitySuffix.isNullable

                return DartTypeFactory.flexibleType(
                    KotlinTypeFactory.simpleType(
                        TypeAttributes.Empty,
                        lower,
                        arguments,
                        nullable,
                    ),
                    KotlinTypeFactory.simpleType(
                        TypeAttributes.Empty,
                        upper,
                        arguments,
                        nullable
                    )
                )
            }

            fun dartCore(className: String) = "dart:core;dart:core/${className.lowercase()}.dart;$className"

            when (elementLocation.toString()) {
                dartCore("bool") -> builtIns.booleanType
                dartCore("int") -> builtIns.intType
                dartCore("double") -> builtIns.doubleType
                dartCore("String") -> builtIns.stringType
                dartCore("num") -> builtIns.numberType
                dartCore("Object") -> builtIns.anyType
                dartCore("Never") -> builtIns.nothingType
                dartCore("List") -> toCollectionType(
                    lower = builtIns.anyList.typeConstructor,
                    upper = builtIns.list.typeConstructor,
                )
                dartCore("Set") -> toCollectionType(
                    lower = builtIns.anySet.typeConstructor,
                    upper = builtIns.set.typeConstructor,
                )
                dartCore("Map") -> toCollectionType(
                    lower = builtIns.anyMap.typeConstructor,
                    upper = builtIns.map.typeConstructor,
                )
                // TODO: Comparable
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