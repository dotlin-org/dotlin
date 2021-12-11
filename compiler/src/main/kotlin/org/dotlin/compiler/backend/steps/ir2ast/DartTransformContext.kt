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

package org.dotlin.compiler.backend.steps.ir2ast

import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrAnnotatedExpression
import org.jetbrains.kotlin.ir.expressions.IrExpression

data class DartTransformContext(val annotatedExpressions: Map<IrExpression, IrAnnotatedExpression> = mapOf()) {
    fun <T> withAnnotatedExpression(from: IrAnnotatedExpression, block: (DartTransformContext) -> T): T {
        val newContext = copy(
            annotatedExpressions = annotatedExpressions + mapOf(from.expression to from)
        )
        return block(newContext)
    }
}
