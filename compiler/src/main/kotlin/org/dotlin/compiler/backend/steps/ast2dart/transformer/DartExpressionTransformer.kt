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
import org.dotlin.compiler.dart.ast.declaration.function.DartFunctionDeclaration
import org.dotlin.compiler.dart.ast.declaration.function.body.DartExpressionFunctionBody
import org.dotlin.compiler.dart.ast.expression.*
import org.dotlin.compiler.dart.ast.expression.DartAssignmentOperator.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.invocation.DartInvocationExpression
import org.dotlin.compiler.dart.ast.expression.literal.*

object DartExpressionTransformer : DartAstNodeTransformer() {
    override fun DartGenerationContext.visitArgumentList(arguments: DartArgumentList): String {
        return arguments.accept(separator = ", ", prefix = "(", suffix = ")")
    }

    override fun DartGenerationContext.visitFunctionExpression(functionExpression: DartFunctionExpression) =
        functionExpression.run {
            var isGetter = false
            var semicolon = ""

            when (val parent = parent) {
                is DartFunctionDeclaration -> {
                    isGetter = parent.isGetter

                    when (body) {
                        is DartExpressionFunctionBody -> semicolon = ";"
                    }
                }
            }

            val parameters = if (!isGetter) acceptChild { parameters } else ""
            val typeParameters = if (!isGetter) acceptChild { typeParameters } else ""
            val body = acceptChild { body }

            "$typeParameters$parameters$body$semicolon"
        }

    override fun DartGenerationContext.visitFunctionReference(functionReference: DartFunctionReference) =
        functionReference.run {
            val function = acceptChild { function }
            val typeArguments = acceptChild { typeArguments }

            "$function$typeArguments"
        }

    override fun DartGenerationContext.visitInvocationExpression(invocation: DartInvocationExpression) =
        invocation.run {
            val arguments = acceptChild { arguments }
            val typeArguments = acceptChild { typeArguments }
            val function = acceptChild { function }

            "$function$typeArguments$arguments"
        }

    override fun DartGenerationContext.visitPropertyAccess(propertyAccess: DartPropertyAccessExpression) =
        propertyAccess.run {
            val target = acceptChild { target }
            val property = acceptChild { propertyName }
            val dot = if (isNullAware) "?." else "."

            "$target$dot$property"
        }

    override fun DartGenerationContext.visitInstanceCreationExpression(instanceCreation: DartInstanceCreationExpression) =
        instanceCreation.run {
            val const = if (isConst) "const " else ""
            val type = acceptChild { type }
            val name = acceptChild(prefix = ".") { constructorName }
            val arguments = acceptChild { arguments }

            "$const$type$name$arguments"
        }

    override fun DartGenerationContext.visitAssignmentExpression(assignment: DartAssignmentExpression) =
        assignment.run {
            val operator = when (operator) {
                ASSIGN -> "="
                NULL_SHORTED -> "??="
                ADD -> "+="
                SUBTRACT -> "-="
                MULTIPLY -> "*="
                DIVIDE -> "/="
                INTEGER_DIVIDE -> "~/="
            }

            val left = acceptChild { left }
            val right = acceptChild { right }

            "$left $operator $right"
        }

    override fun DartGenerationContext.visitNamedExpression(namedExpression: DartNamedExpression) =
        namedExpression.run {
            val name = acceptChild { label }
            val expression = acceptChild { expression }

            "$name$expression"
        }

    override fun DartGenerationContext.visitIndexExpression(indexExpression: DartIndexExpression) =
        indexExpression.run {
            val target = acceptChild { target }
            val index = acceptChild { index }

            "$target[$index]"
        }

    override fun DartGenerationContext.visitParenthesizedExpression(parenthesizedExpression: DartParenthesizedExpression) =
        parenthesizedExpression.run { "(${acceptChild { expression }})" }

    override fun DartGenerationContext.visitPrefixExpression(prefixExpression: DartPrefixExpression) =
        prefixExpression.run { operator.token + acceptChild { expression } }

    override fun DartGenerationContext.visitPostfixExpression(postfixExpression: DartPostfixExpression) =
        postfixExpression.run { acceptChild { expression } + operator.token }

    override fun DartGenerationContext.visitConditionalExpression(conditional: DartConditionalExpression) =
        conditional.run {
            val condition = acceptChild { condition }
            val thenExp = acceptChild { thenExpression }
            val elseExp = acceptChild { elseExpression }

            "$condition ? $thenExp : $elseExp"
        }

    override fun DartGenerationContext.visitIsExpression(isExpression: DartIsExpression) = isExpression.run {
        val expression = acceptChild { expression }
        val type = acceptChild { type }
        val negation = if (isNegated) "!" else ""

        "$expression is${negation} $type"
    }

    override fun DartGenerationContext.visitAsExpression(asExpression: DartAsExpression) = asExpression.run {
        val expression = acceptChild { expression }
        val type = acceptChild { type }

        "$expression as $type"
    }

    override fun DartGenerationContext.visitThisExpression(thisExpression: DartThisExpression) = "this"

    override fun DartGenerationContext.visitSuperExpression(superExpression: DartSuperExpression) = "super"

    override fun DartGenerationContext.visitBinaryInfixExpression(binaryInfix: DartBinaryInfixExpression) =
        binaryInfix.run {
            val left = acceptChild { left }
            val operator = operator.token
            val right = acceptChild { right }

            "$left $operator $right"
        }

    override fun DartGenerationContext.visitThrowExpression(throwExpression: DartThrowExpression) =
        throwExpression.run {
            val exp = acceptChild { expression }

            "throw $exp"
        }

    // Literals
    override fun DartGenerationContext.visitSimpleStringLiteral(literal: DartSimpleStringLiteral) =
        literal.run {
            transformBy { (rawToken, quoteToken) ->
                val value = literal.value

                // TODO: Handle multiline

                "$rawToken$quoteToken$value$quoteToken"
            }
        }

    override fun DartGenerationContext.visitStringInterpolation(literal: DartStringInterpolation) =
        literal.run {
            transformBy { (rawToken, quoteToken) ->
                val elements = acceptChild(separator = "") { elements }

                "$rawToken$quoteToken$elements$quoteToken"
            }
        }

    override fun DartGenerationContext.visitInterpolationString(interpolationString: DartInterpolationString) =
        interpolationString.value

    override fun DartGenerationContext.visitInterpolationExpression(element: DartInterpolationExpression) =
        element.run { "\${${acceptChild { expression }}}" }

    override fun DartGenerationContext.visitNullLiteral(literal: DartNullLiteral) = "null"

    override fun DartGenerationContext.visitTypeLiteral(literal: DartTypeLiteral) =
        literal.acceptChild { type }

    override fun DartGenerationContext.visitIntegerLiteral(literal: DartIntegerLiteral) =
        literal.value.toString()

    override fun DartGenerationContext.visitDoubleLiteral(literal: DartDoubleLiteral) =
        literal.value.toString()

    override fun DartGenerationContext.visitBooleanLiteral(literal: DartBooleanLiteral) =
        literal.value.toString()

    override fun DartGenerationContext.visitCollectionLiteral(literal: DartCollectionLiteral) = literal.run {
        val const = if (literal.isConst) "const " else ""
        val typeArguments = acceptChild { typeArguments }
        val (start, end) = when (literal) {
            is DartListLiteral -> "[" to "]"
            is DartSetLiteral, is DartMapLiteral -> "{" to "}"
        }
        val elements = "$start${acceptChild { elements }}$end"

        "$const$typeArguments$elements"
    }

    override fun DartGenerationContext.visitIdentifier(identifier: DartIdentifier) =
        identifier.value

    override fun DartGenerationContext.visitCode(code: DartCode) = code.value

    private fun DartSingleStringLiteral.transformBy(
        block: (DartSingleStringLiteralDefaults) -> String
    ) = DartSingleStringLiteralDefaults(
        rawToken = if (isRaw) "r" else "",
        quoteToken = "\""
    ).let { block(it) }

    private data class DartSingleStringLiteralDefaults(val rawToken: String, val quoteToken: String)
}