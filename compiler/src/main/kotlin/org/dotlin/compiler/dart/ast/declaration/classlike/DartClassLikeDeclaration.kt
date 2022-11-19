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

package org.dotlin.compiler.dart.ast.declaration.classlike

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.accept
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnitMember
import org.dotlin.compiler.dart.ast.compilationunit.DartNamedCompilationUnitMember
import org.dotlin.compiler.dart.ast.declaration.classlike.member.DartClassMember
import org.dotlin.compiler.dart.ast.expression.DartArgumentList
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList

sealed interface DartClassLikeDeclaration : DartCompilationUnitMember {
    val name: DartSimpleIdentifier?
    val typeParameters: DartTypeParameterList
    val implementsClause: DartImplementsClause?
    val members: List<DartClassMember>
}

data class DartClassDeclaration(
    val isAbstract: Boolean = false,
    override val name: DartSimpleIdentifier,
    override val typeParameters: DartTypeParameterList = DartTypeParameterList(),
    val extendsClause: DartExtendsClause? = null,
    override val implementsClause: DartImplementsClause? = null,
    val withClause: DartWithClause? = null,
    override val members: List<DartClassMember> = listOf(),
    override val annotations: List<DartAnnotation> = listOf(),
    override val documentationComment: String? = null,
) : DartNamedCompilationUnitMember, DartClassLikeDeclaration {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitClassDeclaration(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        name.accept(visitor, data)
        typeParameters.accept(visitor, data)
        extendsClause?.accept(visitor, data)
        implementsClause?.accept(visitor, data)
        withClause?.accept(visitor, data)
        members.accept(visitor, data)
        annotations.accept(visitor, data)
    }
}

data class DartExtensionDeclaration(
    override val name: DartSimpleIdentifier? = null,
    override val typeParameters: DartTypeParameterList = DartTypeParameterList(),
    val extendedType: DartTypeAnnotation,
    override val members: List<DartClassMember> = emptyList(),
    override val annotations: List<DartAnnotation> = emptyList(),
    override val documentationComment: String? = null,
) : DartCompilationUnitMember, DartClassLikeDeclaration {
    override val implementsClause: DartImplementsClause? = null

    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitExtensionDeclaration(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        name?.accept(visitor, data)
        extendedType.accept(visitor, data)
        members.accept(visitor, data)
        typeParameters.accept(visitor, data)
        annotations.accept(visitor, data)
    }
}

data class DartEnumDeclaration(
    override val name: DartSimpleIdentifier,
    override val typeParameters: DartTypeParameterList = DartTypeParameterList(),
    override val implementsClause: DartImplementsClause? = null,
    val withClause: DartWithClause? = null,
    val constants: List<Constant>,
    override val members: List<DartClassMember> = listOf(),
    override val annotations: List<DartAnnotation> = listOf(),
    override val documentationComment: String? = null,
) : DartNamedCompilationUnitMember, DartClassLikeDeclaration {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitEnumDeclaration(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        name.accept(visitor, data)
        typeParameters.accept(visitor, data)
        withClause?.accept(visitor, data)
        implementsClause?.accept(visitor, data)
        members.accept(visitor, data)
        annotations.accept(visitor, data)
    }

    data class Constant(
        val name: DartSimpleIdentifier,
        val typeArguments: DartTypeArgumentList = DartTypeArgumentList(),
        val constructorName: DartSimpleIdentifier?,
        val arguments: DartArgumentList?,
        override val annotations: List<DartAnnotation> = emptyList(),
        override val documentationComment: String? = null
    ) : DartClassMember {
        override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
            visitor.visitEnumConstantDeclaration(this, data)

        override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
            name.accept(visitor, data)
            typeArguments.accept(visitor, data)
            arguments?.accept(visitor, data)
            annotations.accept(visitor, data)
        }
    }
}