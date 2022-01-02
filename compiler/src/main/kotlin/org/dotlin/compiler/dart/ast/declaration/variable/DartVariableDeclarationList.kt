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

package org.dotlin.compiler.dart.ast.declaration.variable

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.accept
import org.dotlin.compiler.dart.ast.annotation.DartAnnotatedNode
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation

data class DartVariableDeclarationList(
    private val variables: List<DartVariableDeclaration> = listOf(),
    val isConst: Boolean = false,
    val isFinal: Boolean = false,
    val isLate: Boolean = false,
    val type: DartTypeAnnotation? = null,
    override val annotations: List<DartAnnotation> = listOf(),
    override val documentationComment: String? = null,
) : DartAnnotatedNode, List<DartVariableDeclaration> by variables {
    constructor(
        vararg variables: DartVariableDeclaration,
        isConst: Boolean = false,
        isFinal: Boolean = false,
        isLate: Boolean = false,
        type: DartTypeAnnotation? = null,
        annotations: List<DartAnnotation> = listOf(),
        documentationComment: String? = null,
    ) : this(variables.toList(), isConst, isFinal, isLate, type, annotations, documentationComment)


    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C) =
        visitor.visitVariableDeclarationList(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        variables.accept(visitor, data)
        type?.accept(visitor, data)
        annotations.accept(visitor, data)
    }
}