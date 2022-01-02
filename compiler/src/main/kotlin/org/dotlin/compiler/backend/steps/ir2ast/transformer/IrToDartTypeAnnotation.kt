/*
 * Copyright 2021 Wilko Manger
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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.IrVoidType
import org.dotlin.compiler.backend.steps.ir2ast.ir.owner
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.dartName
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.jetbrains.kotlin.ir.types.*

fun IrType.accept(context: DartTransformContext): DartTypeAnnotation {
    // TODO: Check for function type

    return when (this) {
        is IrSimpleType -> DartNamedType(
            name = owner.dartName,
            isNullable = hasQuestionMark,
            typeArguments = DartTypeArgumentList(arguments.map { it.accept(context) }.toMutableList()),
        )
        is IrDynamicType -> DartTypeAnnotation.DYNAMIC
        is IrVoidType -> DartTypeAnnotation.VOID
        else -> throw UnsupportedOperationException()
    }
}

// If typeOrNull returns null, it's a star projection, which corresponds best to dynamic in Dart.
fun IrTypeArgument.accept(context: DartTransformContext): DartTypeAnnotation =
    typeOrNull?.accept(context) ?: DartTypeAnnotation.DYNAMIC