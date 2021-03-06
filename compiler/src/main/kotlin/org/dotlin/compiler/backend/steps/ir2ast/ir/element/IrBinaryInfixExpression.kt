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

package org.dotlin.compiler.backend.steps.ir2ast.ir.element

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementTransformerVoid
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

sealed class IrBinaryInfixExpression(override var type: IrType) : IrCustomExpression() {
    abstract var left: IrExpression
    abstract var right: IrExpression

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        left.accept(visitor, data)
        right.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        left = left.transform(transformer, data)
        right = right.transform(transformer, data)
    }
}

class IrConjunctionExpression(
    override var left: IrExpression,
    override var right: IrExpression,
    override var type: IrType,
) : IrBinaryInfixExpression(type) {
    override fun transform(transformer: IrCustomElementTransformerVoid) = transformer.visitConjunctionExpression(this)
}

class IrDisjunctionExpression(
    override var left: IrExpression,
    override var right: IrExpression,
    override var type: IrType,
) : IrBinaryInfixExpression(type) {
    override fun transform(transformer: IrCustomElementTransformerVoid) = transformer.visitDisjunctionExpression(this)
}

class IrIfNullExpression(
    override var left: IrExpression,
    override var right: IrExpression,
    override var type: IrType,
) : IrBinaryInfixExpression(type) {
    override fun transform(transformer: IrCustomElementTransformerVoid) = transformer.visitIfNullExpression(this)
}
