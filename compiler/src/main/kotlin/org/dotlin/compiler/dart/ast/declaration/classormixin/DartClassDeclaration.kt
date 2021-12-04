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

package org.dotlin.compiler.dart.ast.declaration.classormixin

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.DartClassMember
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier

data class DartClassDeclaration(
    val isAbstract: Boolean = false,
    override val name: DartSimpleIdentifier,
    val extendsClause: DartExtendsClause? = null,
    val implementsClause: DartImplementsClause? = null,
    // TODO: withClause, typeParameters
    override val members: List<DartClassMember> = listOf(),
    override val annotations: List<DartAnnotation> = listOf(),
    override val documentationComment: String? = null,
) : DartClassOrMixinDeclaration {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, context: C): R =
        visitor.visitClassDeclaration(this, context)
}