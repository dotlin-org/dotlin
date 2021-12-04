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
import org.dotlin.compiler.dart.ast.statement.*

object DartStatementTransformer : DartAstNodeTransformer {
    override fun visitBlock(block: DartBlock, context: DartGenerationContext): String {
        return block.statements.joinToString(separator = "", prefix = "{", postfix = "}") { it.accept(context) }
    }

    override fun visitExpressionStatement(statement: DartExpressionStatement, context: DartGenerationContext): String {
        return statement.expression.accept(context) + ";"
    }

    override fun visitVariableDeclarationStatement(
        statement: DartVariableDeclarationStatement,
        context: DartGenerationContext,
    ) = statement.variables.accept(context) + ";"

    override fun visitReturnStatement(statement: DartReturnStatement, context: DartGenerationContext): String {
        val value = statement.expression.accept(context)

        return "return $value;"
    }

    override fun visitIfStatement(statement: DartIfStatement, context: DartGenerationContext): String {
        val condition = "if (${statement.condition.accept(context)})"
        val thenStatement = statement.thenStatement.accept(context)
        val elseStatement = statement.elseStatement?.accept(context)?.let { "else $it" } ?: ""

        return "$condition$thenStatement$elseStatement"
    }
}

fun DartStatement.accept(context: DartGenerationContext) = accept(DartStatementTransformer, context)