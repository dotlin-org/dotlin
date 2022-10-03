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

package org.dotlin.compiler.dart.ast.declaration.function

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.accept
import org.dotlin.compiler.dart.ast.annotation.DartAnnotatedNode
import org.dotlin.compiler.dart.ast.expression.DartFunctionExpression
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation

interface DartFunctionDeclaration : DartAnnotatedNode {
    val name: DartSimpleIdentifier?
    val isGetter: Boolean
    val isSetter: Boolean
    val isExternal: Boolean

    val returnType: DartTypeAnnotation

    val function: DartFunctionExpression
}

interface DartNamedFunctionDeclaration : DartFunctionDeclaration {
    override val name: DartSimpleIdentifier

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        name.accept(visitor, data)
        returnType.accept(visitor, data)
        function.accept(visitor, data)
        annotations.accept(visitor, data)
    }
}