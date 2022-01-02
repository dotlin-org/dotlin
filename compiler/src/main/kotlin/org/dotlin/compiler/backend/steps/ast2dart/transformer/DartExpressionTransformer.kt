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
import org.dotlin.compiler.dart.ast.expression.*
import org.dotlin.compiler.dart.ast.expression.DartAssignmentOperator.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.invocation.DartInvocationExpression
import org.dotlin.compiler.dart.ast.expression.literal.*

object DartExpressionTransformer : DartAstNodeTransformer {
    override fun visitArgumentList(arguments: DartArgumentList, context: DartGenerationContext): String {
        return arguments.joinToString(prefix = "(", postfix = ")") { it.accept(context) }
    }

    override fun visitFunctionExpression(
        functionExpression: DartFunctionExpression,
        context: DartGenerationContext,
    ): String {
        val parameters = if (!context.isGetter) functionExpression.parameters.accept(context) else ""
        val typeParameters = if (!context.isGetter) functionExpression.typeParameters.accept(context) else ""
        val body = functionExpression.body.accept(context)

        return "$typeParameters$parameters$body"
    }

    override fun visitInvocationExpression(
        invocation: DartInvocationExpression,
        context: DartGenerationContext,
    ): String {
        val arguments = invocation.arguments.accept(context)
        val typeArguments = invocation.typeArguments.accept(context)
        val function = invocation.function.accept(context)

        return "$function$typeArguments$arguments"
    }

    override fun visitPropertyAccess(propertyAccess: DartPropertyAccessExpression, context: DartGenerationContext) =
        propertyAccess.let {
            val target = it.target.accept(context)
            val property = it.propertyName.accept(context)
            val dot = if (it.isNullAware) "?." else "."

            "$target$dot$property"
        }

    override fun visitInstanceCreationExpression(
        instanceCreation: DartInstanceCreationExpression,
        context: DartGenerationContext,
    ) = instanceCreation.let {
        val const = if (it.isConst) "const " else ""
        val type = it.type.accept(context)
        val name =
            if (it.constructorName != null)
                "." + it.constructorName.accept(context)
            else
                ""
        val arguments = it.arguments.accept(context)

        "$const$type$name$arguments"
    }

    override fun visitAssignmentExpression(
        assignment: DartAssignmentExpression,
        context: DartGenerationContext,
    ): String {
        val operator = when (assignment.operator) {
            ASSIGN -> "="
            NULL_SHORTED -> "??="
            ADD -> "+="
            SUBTRACT -> "-="
            MULTIPLY -> "*="
            DIVIDE -> "/="
        }

        val left = assignment.left.accept(context)
        val right = assignment.right.accept(context)

        return "$left $operator $right"
    }

    override fun visitNamedExpression(namedExpression: DartNamedExpression, context: DartGenerationContext): String {
        val name = namedExpression.label.accept(context)
        val expression = namedExpression.expression.accept(context)

        return "$name$expression"
    }

    override fun visitParenthesizedExpression(
        parenthesizedExpression: DartParenthesizedExpression,
        context: DartGenerationContext
    ) = parenthesizedExpression.let { "(${it.expression.accept(context)})" }

    override fun visitNegatedExpressionExpression(
        negatedExpression: DartNegatedExpression,
        context: DartGenerationContext
    ) = negatedExpression.let { "!${it.expression.accept(context)}" }

    override fun visitConditionalExpression(
        conditional: DartConditionalExpression,
        context: DartGenerationContext,
    ) = conditional.let {
        val condition = it.condition.accept(context)
        val thenExp = it.thenExpression.accept(context)
        val elseExp = it.elseExpression.accept(context)

        "$condition ? $thenExp : $elseExp"
    }

    override fun visitIsExpression(isExpression: DartIsExpression, context: DartGenerationContext) = isExpression.let {
        val expression = it.expression.accept(context)
        val type = it.type.accept(context)
        val negation = if (it.isNegated) "!" else ""

        "$expression is${negation} $type"
    }

    override fun visitAsExpression(asExpression: DartAsExpression, context: DartGenerationContext) = asExpression.let {
        val expression = it.expression.accept(context)
        val type = it.type.accept(context)

        "$expression as $type"
    }

    override fun visitThisExpression(thisExpression: DartThisExpression, context: DartGenerationContext) = "this"

    override fun visitSuperExpression(superExpression: DartSuperExpression, context: DartGenerationContext) = "super"

    override fun visitBinaryInfixExpression(binaryInfix: DartBinaryInfixExpression, context: DartGenerationContext) =
        binaryInfix.let {
            val left = it.left.accept(context)
            val operator = it.operator.token
            val right = it.right.accept(context)

            "$left $operator $right"
        }

    override fun visitThrowExpression(throwExpression: DartThrowExpression, context: DartGenerationContext) =
        throwExpression.let {
            val exp = it.expression.accept(context)

            "throw $exp"
        }

    // Literals
    override fun visitSimpleStringLiteral(literal: DartSimpleStringLiteral, context: DartGenerationContext) =
        literal.transformBy {
            val value = literal.value

            // TODO: Handle multiline

            "$rawToken$quoteToken$value$quoteToken"
        }

    override fun visitStringInterpolation(literal: DartStringInterpolation, context: DartGenerationContext) =
        literal.transformBy {
            val elements = literal.elements.joinToString(separator = "") { it.accept(context) }

            "$rawToken$quoteToken$elements$quoteToken"
        }

    override fun visitInterpolationString(
        interpolationString: DartInterpolationString,
        context: DartGenerationContext
    ) =
        interpolationString.value

    override fun visitInterpolationExpression(
        element: DartInterpolationExpression,
        context: DartGenerationContext
    ) = "\${${element.expression.accept(context)}}"

    override fun visitNullLiteral(literal: DartNullLiteral, context: DartGenerationContext) = "null"

    override fun visitIntegerLiteral(literal: DartIntegerLiteral, context: DartGenerationContext) =
        literal.value.toString()

    override fun visitDoubleLiteral(literal: DartDoubleLiteral, context: DartGenerationContext) =
        literal.value.toString()

    override fun visitBooleanLiteral(literal: DartBooleanLiteral, context: DartGenerationContext) =
        literal.value.toString()

    override fun visitListLiteral(literal: DartListLiteral, context: DartGenerationContext): String {
        val const = if (literal.isConst) "const " else ""
        val typeArguments = literal.typeArguments.accept(context)
        val elements = "[${literal.elements.accept(context)}]"

        return "$const$typeArguments$elements"
    }

    override fun visitIdentifier(identifier: DartIdentifier, context: DartGenerationContext) =
        identifier.value

    override fun visitCode(code: DartCode, context: DartGenerationContext) = code.value

    private fun DartSingleStringLiteral.transformBy(
        block: DartSingleStringLiteralDefaults.() -> String
    ) = DartSingleStringLiteralDefaults(
        rawToken = if (isRaw) "r" else "",
        quoteToken = if (isSingleQuoted) "'" else "\""
    ).let { block(it) }

    private data class DartSingleStringLiteralDefaults(val rawToken: String, val quoteToken: String)
}

fun DartExpression.accept(context: DartGenerationContext) = accept(DartExpressionTransformer, context)
fun DartArgumentList.accept(context: DartGenerationContext) = accept(DartExpressionTransformer, context)
fun DartInterpolationElement.accept(context: DartGenerationContext) = accept(DartExpressionTransformer, context)