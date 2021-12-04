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

package org.dotlin.compiler.dart.ast.declaration.function

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.compilationunit.DartNamedCompilationUnitMember
import org.dotlin.compiler.dart.ast.expression.DartFunctionExpression
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation

data class DartTopLevelFunctionDeclaration(
    override val name: DartSimpleIdentifier,
    override val returnType: DartTypeAnnotation,
    override val function: DartFunctionExpression,
    override val isGetter: Boolean = false,
    override val isSetter: Boolean = false,
    override val isExternal: Boolean = false,
    override val annotations: List<DartAnnotation> = emptyList(),
    override val documentationComment: String? = null,
) : DartFunctionDeclaration, DartNamedCompilationUnitMember {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, context: C): R =
        visitor.visitTopLevelFunctionDeclaration(this, context)
}