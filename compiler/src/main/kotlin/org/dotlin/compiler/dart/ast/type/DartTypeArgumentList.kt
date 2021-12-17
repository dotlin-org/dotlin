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

package org.dotlin.compiler.dart.ast.type

import org.dotlin.compiler.dart.ast.DartAstNode
import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.accept

data class DartTypeArgumentList(
    private val arguments: List<DartTypeAnnotation> = listOf(),
) : DartAstNode, List<DartTypeAnnotation> by arguments {
    constructor(vararg arguments: DartTypeAnnotation) : this(arguments.toList())

    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C) =
        visitor.visitTypeArgumentList(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        arguments.accept(visitor, data)
    }
}