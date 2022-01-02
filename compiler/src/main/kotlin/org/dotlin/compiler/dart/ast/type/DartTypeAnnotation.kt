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

package org.dotlin.compiler.dart.ast.type

import org.dotlin.compiler.dart.ast.DartAstNode
import org.dotlin.compiler.dart.ast.expression.identifier.toDartSimpleIdentifier

interface DartTypeAnnotation : DartAstNode {
    val isNullable: Boolean

    companion object {
        val BOOL = DartNamedType("bool".toDartSimpleIdentifier())
        val DOUBLE = DartNamedType("double".toDartSimpleIdentifier())
        val INT = DartNamedType("int".toDartSimpleIdentifier())
        val NUM = DartNamedType("num".toDartSimpleIdentifier())
        val SET = DartNamedType("Set".toDartSimpleIdentifier())
        val STRING = DartNamedType("String".toDartSimpleIdentifier())
        val NULL = DartNamedType("Null".toDartSimpleIdentifier())
        val VOID = DartNamedType("void".toDartSimpleIdentifier())
        val DYNAMIC = DartNamedType("dynamic".toDartSimpleIdentifier())
        val OBJECT = DartNamedType("Object".toDartSimpleIdentifier())
        fun list(t: DartTypeAnnotation) = DartNamedType(
            "List".toDartSimpleIdentifier(), typeArguments = DartTypeArgumentList(listOf(t))
        )
    }
}

fun DartTypeAnnotation.toNullable() = when (this) {
    is DartNamedType -> toNullable()
    else -> throw UnsupportedOperationException()
}
