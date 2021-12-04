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

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier

data class DartNamedType(
    val name: DartIdentifier,
    val typeArguments: DartTypeArgumentList = DartTypeArgumentList(),
    override val isNullable: Boolean = false,
    val isDeferred: Boolean = false,
) : DartTypeAnnotation {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, context: C): R = visitor.visitNamedType(this, context)
}

fun DartNamedType.toNullable() = copy(isNullable = true)