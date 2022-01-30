/*
 * Copyright 2021-2022 Wilko Manger
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
import org.dotlin.compiler.backend.steps.ir2ast.ir.owner
import org.dotlin.compiler.backend.util.runWith
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.parameter.DartFormalParameterList
import org.dotlin.compiler.dart.ast.parameter.DartSimpleFormalParameter
import org.dotlin.compiler.dart.ast.type.DartFunctionType
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.jetbrains.kotlin.builtins.extractParameterNameFromFunctionTypeArgument
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.util.isFunctionTypeOrSubtype
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

fun IrType.accept(context: DartTransformContext, isConstructorType: Boolean = false): DartTypeAnnotation =
    context.runWith(this) {
        if (!isConstructorType && it.isUnit()) {
            return@runWith DartTypeAnnotation.VOID
        }

        when (it) {
            is IrSimpleType -> when {
                it.isFunctionTypeOrSubtype() -> DartFunctionType(
                    returnType = it.arguments.last().accept(context),
                    parameters = DartFormalParameterList(
                        it.arguments.dropLast(1).map { arg ->
                            DartSimpleFormalParameter(
                                identifier = arg.safeAs<IrSimpleType>()
                                    ?.originalKotlinType
                                    ?.extractParameterNameFromFunctionTypeArgument()
                                    ?.identifier
                                    ?.let { name -> DartSimpleIdentifier(name) },
                                type = arg.accept(context),
                            )
                    }
                )
            )
            else -> DartNamedType(
                name = it.owner.dartName,
                isNullable = it.hasQuestionMark,
                typeArguments = DartTypeArgumentList(it.arguments.map { arg -> arg.accept(context) }.toMutableList()),
            )
        }
        is IrDynamicType -> DartTypeAnnotation.DYNAMIC
        else -> throw UnsupportedOperationException()
    }
}

// If typeOrNull returns null, it's a star projection, which corresponds best to dynamic in Dart.
fun IrTypeArgument.accept(context: DartTransformContext): DartTypeAnnotation =
    typeOrNull?.accept(context) ?: DartTypeAnnotation.DYNAMIC