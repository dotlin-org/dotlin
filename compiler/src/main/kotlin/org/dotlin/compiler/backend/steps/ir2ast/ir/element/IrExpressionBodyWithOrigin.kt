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
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

class IrExpressionBodyWithOrigin(
    override var expression: IrExpression,
    val origin: IrStatementOrigin
) : IrExpressionBody(), IrCustomElement {
    override val factory = IrFactoryImpl

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R =
        super<IrExpressionBody>.accept(visitor, data)

    override fun <D> transform(transformer: IrElementTransformer<D>, data: D) =
        super<IrExpressionBody>.transform(transformer, data)

    override fun transform(transformer: IrCustomElementTransformerVoid) =
        transformer.visitExpressionBodyWithOrigin(this)
}
