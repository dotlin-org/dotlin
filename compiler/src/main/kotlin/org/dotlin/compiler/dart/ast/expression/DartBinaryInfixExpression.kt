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

sealed interface DartBinaryInfixExpression : DartExpression {
    val left: DartExpression
    val operator: DartBinaryInfixOperator
    val right: DartExpression

    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C) =
        visitor.visitBinaryInfixExpression(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        left.accept(visitor, data)
        right.accept(visitor, data)
    }
}

@JvmInline
value class DartBinaryInfixOperator(val token: String)

data class DartPlusExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("+")
}

data class DartMinusExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("-")
}

data class DartMultiplyExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("*")
}

data class DartDivideExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("/")
}

data class DartIntegerDivideExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("~/")
}

data class DartModuloExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("%")
}

data class DartComparisonExpression(
    override val left: DartExpression,
    override val operator: DartBinaryInfixOperator,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    init {
        require(operator in Operators.ALL)
    }

    object Operators {
        val GREATER = DartBinaryInfixOperator(">")
        val LESS = DartBinaryInfixOperator("<")
        val GREATER_OR_EQUAL = DartBinaryInfixOperator(">=")
        val LESS_OR_EQUAL = DartBinaryInfixOperator("<=")

        val ALL = listOf(GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL)
    }
}

data class DartIfNullExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("??")
}

data class DartEqualsExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("==")
}

data class DartNotEqualsExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("!=")
}

data class DartConjunctionExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("&&")
}

data class DartDisjunctionExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("||")
}

data class DartBitwiseShiftLeftExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("<<")
}

data class DartBitwiseShiftRightExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator(">>")
}

data class DartBitwiseUnsignedShiftRightExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator(">>>")
}

data class DartBitwiseAndExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("&")
}

data class DartBitwiseOrExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("|")
}

data class DartBitwiseExclusiveOrExpression(
    override val left: DartExpression,
    override val right: DartExpression,
) : DartBinaryInfixExpression {
    override val operator = DartBinaryInfixOperator("^")
}



