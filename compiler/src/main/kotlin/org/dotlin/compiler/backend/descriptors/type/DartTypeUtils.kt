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

import org.dotlin.compiler.backend.steps.src2ir.DotlinModule
import org.dotlin.compiler.dart.element.*
import org.dotlin.compiler.dart.element.DartNullabilitySuffix.QUESTION_MARK
import org.jetbrains.kotlin.types.*

fun DartType.toKotlinType(module: DotlinModule): KotlinType {
    return when (this) {
        is DartInterfaceType -> {
            fun dartCore(className: String) = "dart:core;dart:core/${className.lowercase()}.dart;$className"

            when (elementLocation.toString()) {
                dartCore("int") -> module.builtIns.intType
                dartCore("double") -> module.builtIns.doubleType
                dartCore("String") -> module.builtIns.stringType
                dartCore("num") -> module.builtIns.numberType
                dartCore("Object") -> module.builtIns.anyType
                // TODO: Comparable
                // TODO: Handle collection types
                else -> DartSimpleType(
                    DartInterfaceTypeConstructor(this, module),
                    arguments = emptyList(), // TODO
                    attributes = TypeAttributes.Empty, // TODO
                    isMarkedNullable = nullabilitySuffix == QUESTION_MARK
                )
            }
        }

        DartDynamicType -> DynamicType(
            builtIns = module.builtIns,
            attributes = TypeAttributes.Empty
        )

        is DartFunctionType -> module.builtIns
            .getFunction(parameters.size)
            .defaultType
            .replace(newArguments = buildList {
                add(TypeProjectionImpl(returnType.toKotlinType(module)))
                addAll(parameters.map { TypeProjectionImpl(it.type.toKotlinType(module)) })
            })

        DartNeverType -> module.builtIns.nothingType
        DartVoidType -> module.builtIns.unitType // TODO
    }
}