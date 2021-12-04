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

package org.dotlin.compiler.backend.steps.ast2dart.transformer

import org.dotlin.compiler.backend.steps.ast2dart.DartGenerationContext
import org.dotlin.compiler.dart.ast.declaration.function.body.DartBlockFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartEmptyFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartExpressionFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartFunctionBody

object DartFunctionBodyTransformer : DartAstNodeTransformer {
    override fun visitEmptyFunctionBody(body: DartEmptyFunctionBody, context: DartGenerationContext): String = ";"

    override fun visitBlockFunctionBody(body: DartBlockFunctionBody, context: DartGenerationContext): String {
        // TODO: async / generator

        return body.block.accept(context)
    }

    override fun visitExpressionFunctionBody(body: DartExpressionFunctionBody, context: DartGenerationContext): String {
        val expression = body.expression.accept(context)
        val async = if (body.isAsync) "async " else ""

        return "$async=> $expression;"
    }
}

fun DartFunctionBody.accept(context: DartGenerationContext) = accept(DartFunctionBodyTransformer, context)