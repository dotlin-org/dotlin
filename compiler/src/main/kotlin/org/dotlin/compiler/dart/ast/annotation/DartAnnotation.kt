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

package org.dotlin.compiler.dart.ast.annotation

import org.dotlin.compiler.dart.ast.DartAstNode
import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.expression.DartArgumentList
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.toDartIdentifier
import org.dotlin.compiler.dart.ast.expression.literal.DartSimpleStringLiteral
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList

data class DartAnnotation(
    val name: DartIdentifier,
    val constructorName: DartSimpleIdentifier? = null,
    val arguments: DartArgumentList? = null,
    val typeArguments: DartTypeArgumentList? = null,
) : DartAstNode {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C) = visitor.visitAnnotation(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        name.accept(visitor, data)
        constructorName?.accept(visitor, data)
        arguments?.acceptChildren(visitor, data)
        typeArguments?.acceptChildren(visitor, data)
    }

    companion object {
        val OVERRIDE = DartAnnotation("override".toDartIdentifier())

        fun pragma(name: String) =
            DartAnnotation(
                "pragma".toDartIdentifier(),
                arguments = DartArgumentList(DartSimpleStringLiteral(name))
            )
    }
}