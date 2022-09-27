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
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameter
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList

object DartTypeAnnotationTransformer : DartAstNodeTransformer() {
    override fun DartGenerationContext.visitTypeArgumentList(typeArguments: DartTypeArgumentList) =
        typeArguments.accept(separator = ", ", prefix = "<", suffix = ">", ifEmpty = "")

    override fun DartGenerationContext.visitNamedType(type: DartNamedType) = type.run {
        val name = acceptChild { name }
        val typeArguments = acceptChild { typeArguments }
        val questionMark = if (type.isNullable) "?" else ""

        "$name$typeArguments$questionMark"
    }

    override fun DartGenerationContext.visitFunctionType(type: DartFunctionType) = type.run {
        val returnType = acceptChild { returnType }
        val typeParameters = acceptChild { typeParameters }
        val parameters = acceptChild { parameters }
        val questionMark = if (type.isNullable) "?" else ""

        "$returnType Function$typeParameters$parameters$questionMark"
    }

    override fun DartGenerationContext.visitTypeParameter(typeParameter: DartTypeParameter) =
        typeParameter.run {
            val name = acceptChild { name }
            val bound = acceptChild(prefix = " extends ") { bound }

            "$name$bound"
        }

    override fun DartGenerationContext.visitTypeParameterList(typeParameters: DartTypeParameterList) =
        typeParameters.accept(separator = ", ", prefix = "<", suffix = ">", ifEmpty = "")
}