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

package org.dotlin.compiler.backend.steps.ir2ast.ir

import org.dotlin.compiler.backend.steps.ir2ast.ir.element.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

/**
 * Call [visitExpression] and [visitBody] in your own transformer. If null, return your original super.
 */
interface IrCustomElementHelper {
    fun visitExpression(expression: IrExpression): IrExpression? {
        return when (expression) {
            is IrAnnotatedExpression -> visitAnnotatedExpression(expression)
            is IrNullAwareExpression -> visitNullAwareExpression(expression)
            is IrConjunctionExpression -> visitConjunctionExpression(expression)
            is IrDisjunctionExpression -> visitDisjunctionExpression(expression)
            else -> null
        }

    }

    fun visitAnnotatedExpression(expression: IrAnnotatedExpression): IrAnnotatedExpression
    fun visitNullAwareExpression(expression: IrNullAwareExpression): IrNullAwareExpression
    fun visitConjunctionExpression(expression: IrConjunctionExpression): IrConjunctionExpression
    fun visitDisjunctionExpression(expression: IrDisjunctionExpression): IrDisjunctionExpression
    fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin): IrExpressionBodyWithOrigin

    fun visitBody(body: IrBody): IrBody? {
        return when (body) {
            is IrExpressionBodyWithOrigin -> visitExpressionBodyWithOrigin(body)
            else -> null
        }
    }
}

fun IrCustomElementHelper.visitExpression(
    expression: IrExpression,
    helperVisitExpression: (IrExpression) -> IrExpression?,
    superVisitExpression: (IrExpression) -> IrExpression
) = helperVisitExpression(expression) ?: superVisitExpression(expression)

fun IrCustomElementHelper.visitBody(
    body: IrBody,
    helperVisitBody: (IrBody) -> IrBody?,
    superVisitBody: (IrBody) -> IrBody
) =
    helperVisitBody(body) ?: superVisitBody(body)

abstract class IrCustomElementTransformerVoid : IrElementTransformerVoid(), IrCustomElementHelper {
    override fun visitExpression(expression: IrExpression) = visitExpression(
        expression,
        helperVisitExpression = { super<IrCustomElementHelper>.visitExpression(it) },
        superVisitExpression = { super<IrElementTransformerVoid>.visitExpression(it) }
    )

    override fun visitBody(body: IrBody) = visitBody(
        body,
        helperVisitBody = { super<IrCustomElementHelper>.visitBody(it) },
        superVisitBody = { super<IrElementTransformerVoid>.visitBody(it) }
    )

    override fun visitAnnotatedExpression(expression: IrAnnotatedExpression) = expression
    override fun visitNullAwareExpression(expression: IrNullAwareExpression) = expression
    override fun visitConjunctionExpression(expression: IrConjunctionExpression) = expression
    override fun visitDisjunctionExpression(expression: IrDisjunctionExpression) = expression
    override fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin): IrExpressionBodyWithOrigin {
        body.transformChildrenVoid()
        return body
    }
}