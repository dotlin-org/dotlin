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

package org.dotlin.compiler.backend.steps.ast2dart.transformer

import org.dotlin.compiler.backend.steps.ast2dart.DartGenerationContext
import org.dotlin.compiler.dart.ast.collection.DartCollectionElement
import org.dotlin.compiler.dart.ast.collection.DartCollectionElementList
import org.dotlin.compiler.dart.ast.expression.DartExpression

object DartCollectionElementTransformer : DartAstNodeTransformer {
    override fun visitCollectionElementList(
        collectionElementList: DartCollectionElementList,
        context: DartGenerationContext
    ) = collectionElementList.let { list ->
        list.joinToString(separator = ", ") { it.accept(context) }
    }
}

fun DartCollectionElement.accept(context: DartGenerationContext) = when (this) {
    is DartExpression -> accept(DartExpressionTransformer, context)
    else -> accept(DartCollectionElementTransformer, context)
}

fun DartCollectionElementList.accept(context: DartGenerationContext) = accept(DartCollectionElementTransformer, context)