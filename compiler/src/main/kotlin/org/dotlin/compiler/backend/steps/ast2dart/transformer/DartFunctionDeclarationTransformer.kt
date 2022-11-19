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
import org.dotlin.compiler.dart.ast.declaration.classlike.member.DartMethodDeclaration
import org.dotlin.compiler.dart.ast.declaration.function.DartNamedFunctionDeclaration
import org.dotlin.compiler.dart.ast.declaration.function.body.DartBlockFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartEmptyFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartExpressionFunctionBody

object DartFunctionDeclarationTransformer : DartAstNodeTransformer() {
    override fun DartGenerationContext.visitNamedFunctionDeclaration(
        functionDeclaration: DartNamedFunctionDeclaration
    ) = functionDeclaration.run {
        val annotations = acceptChildAnnotations()
        val name = acceptChild { name }
        val returnType = acceptChild { returnType }
        val function = acceptChild { function }

        val getOrSet = when {
            isGetter -> "get "
            isSetter -> "set "
            else -> ""
        }

        var operator = ""
        var static = ""

        if (this is DartMethodDeclaration) {
            when {
                isOperator -> operator = "operator "
                isStatic -> static = "static "
            }
        }

        "$annotations$static$returnType $operator$getOrSet$name$function"
    }

    override fun DartGenerationContext.visitEmptyFunctionBody(body: DartEmptyFunctionBody): String = ";"

    override fun DartGenerationContext.visitBlockFunctionBody(body: DartBlockFunctionBody) = body.run {
        // TODO: async / generator

        acceptChild { block }
    }

    override fun DartGenerationContext.visitExpressionFunctionBody(body: DartExpressionFunctionBody) = body.run {
        val expression = acceptChild { expression }
        val async = if (body.isAsync) "async " else ""

        "$async=> $expression"
    }
}