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

package org.dotlin.compiler.backend.steps.ast2dart.transformer

import org.dotlin.compiler.backend.steps.ast2dart.DartGenerationContext
import org.dotlin.compiler.dart.ast.type.DartFunctionType
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameter
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList

object DartTypeAnnotationTransformer : DartAstNodeTransformer {
    override fun visitTypeArgumentList(typeArguments: DartTypeArgumentList, context: DartGenerationContext): String {
        return when {
            typeArguments.isNotEmpty() -> typeArguments.joinToString(prefix = "<", postfix = ">") { it.accept(context) }
            else -> ""
        }
    }

    override fun visitNamedType(type: DartNamedType, context: DartGenerationContext): String {
        val name = type.name.accept(context)
        val typeArguments = type.typeArguments.accept(context)
        val questionMark = if (type.isNullable) "?" else ""

        return "$name$typeArguments$questionMark"
    }

    override fun visitFunctionType(type: DartFunctionType, context: DartGenerationContext) = type.let {
        val returnType = it.returnType.accept(context)
        val typeParameters = it.typeParameters.accept(context)
        val parameters = it.parameters.accept(context)
        val questionMark = if (type.isNullable) "?" else ""

        "$returnType Function$typeParameters$parameters$questionMark"
    }

    override fun visitTypeParameter(typeParameter: DartTypeParameter, context: DartGenerationContext) =
        typeParameter.let {
            val name = it.name
            val bound = if (it.bound != null) " extends ${it.bound.accept(context)}" else ""

            "$name$bound"
        }

    override fun visitTypeParameterList(typeParameters: DartTypeParameterList, context: DartGenerationContext) =
        if (typeParameters.isNotEmpty())
            typeParameters.joinToString(prefix = "<", postfix = ">") { it.accept(context) }
        else
            ""
}

fun DartTypeAnnotation.accept(context: DartGenerationContext) = accept(DartTypeAnnotationTransformer, context)
fun DartTypeArgumentList.accept(context: DartGenerationContext) = accept(DartTypeAnnotationTransformer, context)
fun DartTypeParameter.accept(context: DartGenerationContext) = accept(DartTypeAnnotationTransformer, context)
fun DartTypeParameterList.accept(context: DartGenerationContext) = accept(DartTypeAnnotationTransformer, context)