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
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor

/**
 * Call [visitExpression] and [visitBody] in your own transformer.
 */
interface IrCustomElementVisitorHelper<out R, in D> {
    fun visitDartCodeExpression(expression: IrDartCodeExpression, data: D): R
    fun visitAnnotatedExpression(expression: IrAnnotatedExpression, data: D): R
    fun visitNullAwareExpression(expression: IrNullAwareExpression, data: D): R
    fun visitIfNullExpression(expression: IrIfNullExpression, data: D): R
    fun visitConjunctionExpression(expression: IrConjunctionExpression, data: D): R
    fun visitDisjunctionExpression(expression: IrDisjunctionExpression, data: D): R
    fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin, data: D): R
}

fun <R, D> IrCustomElementVisitorHelper<R, D>.visitCustomExpression(
    expression: IrExpression,
    data: D,
    fallback: () -> R
) = when (expression) {
    is IrDartCodeExpression -> visitDartCodeExpression(expression, data)
    is IrAnnotatedExpression -> visitAnnotatedExpression(expression, data)
    is IrNullAwareExpression -> visitNullAwareExpression(expression, data)
    is IrIfNullExpression -> visitIfNullExpression(expression, data)
    is IrConjunctionExpression -> visitConjunctionExpression(expression, data)
    is IrDisjunctionExpression -> visitDisjunctionExpression(expression, data)
    else -> fallback()
}

fun <R, D> IrCustomElementVisitorHelper<R, D>.visitCustomBody(
    body: IrBody,
    data: D,
    fallback: () -> R
) = when (body) {
    is IrExpressionBodyWithOrigin -> visitExpressionBodyWithOrigin(body, data)
    else -> fallback()
}

interface IrCustomElementVisitor<out R, in D> : IrElementVisitor<R, D>, IrCustomElementVisitorHelper<R, D> {
    override fun visitExpression(expression: IrExpression, data: D) =
        visitCustomExpression(expression, data, fallback = { super.visitExpression(expression, data) })

    override fun visitBody(body: IrBody, data: D) =
        visitCustomBody(body, data, fallback = { super.visitBody(body, data) })

    override fun visitDartCodeExpression(expression: IrDartCodeExpression, data: D) = visitExpression(expression, data)
    override fun visitAnnotatedExpression(expression: IrAnnotatedExpression, data: D) =
        visitExpression(expression, data)

    override fun visitNullAwareExpression(expression: IrNullAwareExpression, data: D) =
        visitExpression(expression, data)

    override fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin, data: D) = visitBody(body, data)

    fun visitBinaryInfixExpression(expression: IrBinaryInfixExpression, data: D) = visitExpression(expression, data)
    override fun visitIfNullExpression(expression: IrIfNullExpression, data: D) =
        visitBinaryInfixExpression(expression, data)

    override fun visitConjunctionExpression(expression: IrConjunctionExpression, data: D) =
        visitBinaryInfixExpression(expression, data)

    override fun visitDisjunctionExpression(expression: IrDisjunctionExpression, data: D) =
        visitBinaryInfixExpression(expression, data)
}

/**
 * Call [visitExpression] and [visitBody] in your own transformer.
 */
interface IrCustomElementTransformerHelper<in D> : IrElementTransformer<D>, IrCustomElementVisitorHelper<IrElement, D> {
    override fun visitDartCodeExpression(expression: IrDartCodeExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitAnnotatedExpression(expression: IrAnnotatedExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitNullAwareExpression(expression: IrNullAwareExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitIfNullExpression(expression: IrIfNullExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitConjunctionExpression(expression: IrConjunctionExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitDisjunctionExpression(expression: IrDisjunctionExpression, data: D) =
        super.visitExpression(expression, data)

    override fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin, data: D) = super.visitBody(body, data)
}

interface IrCustomElementTransformerHelperVoid : IrCustomElementTransformerHelper<Nothing?> {
    fun visitDartCodeExpression(expression: IrDartCodeExpression): IrExpression =
        super.visitDartCodeExpression(expression, null)

    override fun visitAnnotatedExpression(expression: IrAnnotatedExpression, data: Nothing?) =
        visitAnnotatedExpression(expression)

    fun visitAnnotatedExpression(expression: IrAnnotatedExpression): IrExpression =
        super.visitAnnotatedExpression(expression, null)

    override fun visitNullAwareExpression(expression: IrNullAwareExpression, data: Nothing?) =
        visitNullAwareExpression(expression)

    fun visitNullAwareExpression(expression: IrNullAwareExpression): IrExpression =
        super.visitNullAwareExpression(expression, null)

    fun visitIfNullExpression(expression: IrIfNullExpression): IrExpression =
        super.visitIfNullExpression(expression, null)

    override fun visitConjunctionExpression(expression: IrConjunctionExpression, data: Nothing?) =
        visitConjunctionExpression(expression)

    fun visitConjunctionExpression(expression: IrConjunctionExpression): IrExpression =
        super.visitConjunctionExpression(expression, null)

    override fun visitDisjunctionExpression(expression: IrDisjunctionExpression, data: Nothing?) =
        visitDisjunctionExpression(expression)

    fun visitDisjunctionExpression(expression: IrDisjunctionExpression): IrExpression =
        super.visitDisjunctionExpression(expression, null)

    override fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin, data: Nothing?) =
        visitExpressionBodyWithOrigin(body)

    fun visitExpressionBodyWithOrigin(body: IrExpressionBodyWithOrigin): IrBody =
        super.visitExpressionBodyWithOrigin(body, null)
}

abstract class IrCustomElementTransformerVoid : IrElementTransformerVoid(), IrCustomElementTransformerHelperVoid {
    override fun visitExpression(expression: IrExpression) =
        visitCustomExpression(
            expression,
            data = null,
            fallback = { super<IrElementTransformerVoid>.visitExpression(expression) }
        ) as IrExpression

    override fun visitBody(body: IrBody) = visitCustomBody(
        body,
        data = null,
        fallback = { super<IrElementTransformerVoid>.visitBody(body) }
    ) as IrBody
}