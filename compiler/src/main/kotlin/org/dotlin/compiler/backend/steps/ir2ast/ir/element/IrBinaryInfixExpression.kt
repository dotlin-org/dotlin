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

package org.dotlin.compiler.backend.steps.ir2ast.ir.element

import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

sealed class IrBinaryInfixExpression(override var type: IrType) : IrExpression() {
    abstract val left: IrExpression
    abstract val right: IrExpression

    override val endOffset = UNDEFINED_OFFSET
    override val startOffset = UNDEFINED_OFFSET

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D) = visitor.visitExpression(this, data)

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        left.accept(visitor, data)
        right.accept(visitor, data)
    }
}

class IrConjunctionExpression(
    override val left: IrExpression,
    override val right: IrExpression,
    override var type: IrType,
) : IrBinaryInfixExpression(type)

class IrDisjunctionExpression(
    override val left: IrExpression,
    override val right: IrExpression,
    override var type: IrType,
) : IrBinaryInfixExpression(type)
