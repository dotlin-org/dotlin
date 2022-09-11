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

package org.dotlin.compiler.dart.ast.declaration.classormixin

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.accept
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.DartClassMember
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList

data class DartClassDeclaration(
    val isAbstract: Boolean = false,
    override val name: DartSimpleIdentifier,
    override val typeParameters: DartTypeParameterList = DartTypeParameterList(),
    val extendsClause: DartExtendsClause? = null,
    val implementsClause: DartImplementsClause? = null,
    val withClause: DartWithClause? = null,
    // TODO: typeParameters
    override val members: List<DartClassMember> = listOf(),
    override val annotations: List<DartAnnotation> = listOf(),
    override val documentationComment: String? = null,
) : DartClassOrMixinDeclaration {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitClassDeclaration(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        name.accept(visitor, data)
        typeParameters.accept(visitor, data)
        extendsClause?.accept(visitor, data)
        implementsClause?.accept(visitor, data)
        members.accept(visitor, data)
        annotations.accept(visitor, data)
    }
}