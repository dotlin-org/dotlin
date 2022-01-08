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

package org.dotlin.compiler.dart.ast.statement

import org.dotlin.compiler.dart.ast.DartAstNode
import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.accept
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList
import org.dotlin.compiler.dart.ast.expression.DartExpression

data class DartForStatement(
    val loopParts: DartForLoopParts,
    val body: DartStatement
) : DartStatement {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitForStatement(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        loopParts.accept(visitor, data)
        body.accept(visitor, data)
    }
}

interface DartForLoopParts : DartAstNode

interface DartForParts : DartForLoopParts {
    val condition: DartExpression
    val updaters: List<DartExpression>
}

data class DartForPartsWithDeclarations(
    val variables: DartVariableDeclarationList, // TODO: Use DeclaredIdentifier
    override val condition: DartExpression,
    override val updaters: List<DartExpression>
) : DartForParts {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitForPartsWithDeclarations(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        variables.accept(visitor, data)
        condition.accept(visitor, data)
        updaters.accept(visitor, data)
    }
}

interface DartForEachParts : DartForLoopParts {
    val iterable: DartExpression
}

data class DartForEachPartsWithDeclarations(
    val variables: DartVariableDeclarationList, // TODO: Use DeclaredIdentifier
    override val iterable: DartExpression,
) : DartForEachParts {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitForEachPartsWithDeclarations(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        variables.accept(visitor, data)
        iterable.accept(visitor, data)
    }
}
