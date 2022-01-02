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

package org.dotlin.compiler.dart.ast.directive

import org.dotlin.compiler.dart.ast.DartAstNode
import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.accept
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier

sealed interface DartCombinator : DartAstNode {
    val keyword: String
    val names: List<DartSimpleIdentifier>

    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C) = visitor.visitCombinator(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {
        names.accept(visitor, data)
    }

    companion object {
        fun fromKeyword(keyword: String, names: List<DartSimpleIdentifier>) = when (keyword) {
            DartHideCombinator.KEYWORD -> DartHideCombinator(names)
            DartShowCombinator.KEYWORD -> DartShowCombinator(names)
            else -> throw UnsupportedOperationException("Not a valid keyword: $keyword")
        }
    }
}

data class DartHideCombinator(
    override val names: List<DartSimpleIdentifier>
) : DartCombinator {
    override val keyword = KEYWORD

    companion object {
        const val KEYWORD = "hide"
    }
}

data class DartShowCombinator(
    override val names: List<DartSimpleIdentifier>
) : DartCombinator {
    override val keyword = KEYWORD

    companion object {
        const val KEYWORD = "show"
    }
}