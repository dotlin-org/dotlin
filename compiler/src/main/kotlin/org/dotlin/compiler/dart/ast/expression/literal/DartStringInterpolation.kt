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

package org.dotlin.compiler.dart.ast.expression.literal

import org.dotlin.compiler.dart.ast.DartAstNode
import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.accept
import org.dotlin.compiler.dart.ast.expression.DartExpression

data class DartStringInterpolation(
    val elements: List<DartInterpolationElement>,
    override val isRaw: Boolean = false,
    override val isTripleQuoted: Boolean = false,
) : DartSingleStringLiteral {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitStringInterpolation(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        elements.accept(visitor, data)
    }
}

interface DartInterpolationElement : DartAstNode

data class DartInterpolationExpression(val expression: DartExpression) : DartInterpolationElement {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitInterpolationExpression(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {}
}

data class DartInterpolationString(val value: String) : DartInterpolationElement {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitInterpolationString(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {}
}