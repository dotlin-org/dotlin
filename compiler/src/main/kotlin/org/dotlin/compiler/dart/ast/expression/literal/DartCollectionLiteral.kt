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

package org.dotlin.compiler.dart.ast.expression.literal

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.collection.DartCollectionElementList
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList

sealed interface DartCollectionLiteral : DartTypedLiteral {
    val elements: DartCollectionElementList

    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C) =
        visitor.visitCollectionLiteral(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        elements.accept(visitor, data)
        typeArguments.accept(visitor, data)
    }
}

data class DartListLiteral(
    override val elements: DartCollectionElementList,
    override val isConst: Boolean = false,
    override val typeArguments: DartTypeArgumentList
) : DartCollectionLiteral

data class DartSetLiteral(
    override val elements: DartCollectionElementList,
    override val isConst: Boolean = false,
    override val typeArguments: DartTypeArgumentList
) : DartCollectionLiteral

data class DartMapLiteral(
    override val elements: DartCollectionElementList,
    override val isConst: Boolean = false,
    override val typeArguments: DartTypeArgumentList
) : DartCollectionLiteral