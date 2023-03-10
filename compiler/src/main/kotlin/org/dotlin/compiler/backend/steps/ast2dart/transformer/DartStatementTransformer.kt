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
import org.dotlin.compiler.dart.ast.statement.*
import org.dotlin.compiler.dart.ast.statement.declaration.DartVariableDeclarationStatement
import org.dotlin.compiler.dart.ast.statement.trycatch.DartCatchClause
import org.dotlin.compiler.dart.ast.statement.trycatch.DartTryStatement

object DartStatementTransformer : DartAstNodeTransformer() {
    override fun DartGenerationContext.visitBlock(block: DartBlock) =
        block.acceptChild(separator = "", prefix = "{", suffix = "}") { statements }

    override fun DartGenerationContext.visitExpressionStatement(statement: DartExpressionStatement) =
        statement.acceptChild { expression } + ";"

    override fun DartGenerationContext.visitVariableDeclarationStatement(statement: DartVariableDeclarationStatement) =
        statement.acceptChild { variables } + ";"


    override fun DartGenerationContext.visitReturnStatement(statement: DartReturnStatement) = statement.run {
        val value = acceptChild(prefix = " ") { expression }
        "return$value;"
    }

    override fun DartGenerationContext.visitContinueStatement(statement: DartContinueStatement) = "continue;"
    override fun DartGenerationContext.visitBreakStatement(statement: DartBreakStatement) = "break;"

    override fun DartGenerationContext.visitIfStatement(statement: DartIfStatement) = statement.run {
        val condition = "if (${acceptChild { condition }})"
        val thenStatement = acceptChild { thenStatement }
        val elseStatement = acceptChild(prefix = "else ") { elseStatement }

        "$condition$thenStatement$elseStatement"
    }

    override fun DartGenerationContext.visitTryStatement(statement: DartTryStatement) = statement.run {
        val body = acceptChild { body }
        val catchClauses = acceptChild(separator = " ") { catchClauses }
        val finallyClause = acceptChild(prefix = "finally ") { finallyBlock }

        "try $body$catchClauses $finallyClause"
    }

    override fun DartGenerationContext.visitCatchClause(catchClause: DartCatchClause) = catchClause.run {
        val catchPart = acceptChild(separator = ", ", "catch (", suffix = ")") {
            listOfNotNull(
                exceptionParameter,
                stackTraceParameter
            )
        }

        val onPart = acceptChild(prefix = "on ") { exceptionType }
        val body = acceptChild { body }

        "$onPart $catchPart$body"
    }

    override fun DartGenerationContext.visitWhileStatement(statement: DartWhileStatement) = statement.run {
        val condition = acceptChild { condition }
        val body = acceptChild { body }

        when {
            isDoWhile() -> "do $body while ($condition);"
            else -> "while ($condition) $body"
        }
    }

    override fun DartGenerationContext.visitForStatement(statement: DartForStatement) = statement.run {
        val parts = acceptChild { loopParts }
        val body = acceptChild { body }

        "for ($parts) $body"
    }

    override fun DartGenerationContext.visitForPartsWithDeclarations(forParts: DartForPartsWithDeclarations) =
        forParts.run {
            val variables = acceptChild { variables }
            val condition = acceptChild { condition }
            val updaters = acceptChild(separator = ";") { updaters }

            "$variables; $condition; $updaters"
        }

    override fun DartGenerationContext.visitForEachPartsWithDeclarations(forParts: DartForEachPartsWithDeclarations) =
        forParts.run {
            val variables = acceptChild { variables }
            val iterable = acceptChild { iterable }

            "$variables in $iterable"
        }
}