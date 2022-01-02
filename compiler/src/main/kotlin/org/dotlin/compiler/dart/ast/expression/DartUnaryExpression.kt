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

package org.dotlin.compiler.dart.ast.expression

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor

sealed interface DartUnaryExpression : DartExpression {
    val expression: DartExpression
    val operator: DartUnaryOperator

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        expression.accept(visitor, data)
    }
}

@JvmInline
value class DartUnaryOperator(val token: String)

sealed interface DartPrefixExpression : DartUnaryExpression {
    override fun <R, D> accept(visitor: DartAstNodeVisitor<R, D>, data: D): R =
        visitor.visitPrefixExpression(this, data)
}

sealed interface DartPostfixExpression : DartUnaryExpression {
    override fun <R, D> accept(visitor: DartAstNodeVisitor<R, D>, data: D): R =
        visitor.visitPostfixExpression(this, data)
}

data class DartNegatedExpression(
    override val expression: DartExpression,
) : DartPrefixExpression {
    override val operator = DartUnaryOperator("!")
}

data class DartNotNullAssertionExpression(
    override val expression: DartExpression,
) : DartPostfixExpression {
    override val operator = DartUnaryOperator("!")
}

data class DartUnaryMinusExpression(
    override val expression: DartExpression,
) : DartPrefixExpression {
    override val operator = DartUnaryOperator("-")
}