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

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.parameter.DartFormalParameterList
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList

data class DartFunctionType(
    val returnType: DartTypeAnnotation,
    val typeParameters: DartTypeParameterList = DartTypeParameterList(),
    val parameters: DartFormalParameterList = DartFormalParameterList(),
    override val isNullable: Boolean = false,
) : DartTypeAnnotation {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R = visitor.visitFunctionType(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        returnType.accept(visitor, data)
        parameters.accept(visitor, data)
        typeParameters.accept(visitor, data)
    }
}