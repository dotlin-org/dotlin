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

@file:Suppress("DIFFERENT_NAMES_FOR_THE_SAME_PARAMETER_IN_SUPERTYPES", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package org.dotlin.compiler.backend.steps.ir2ast.lower

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrExpressionContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrExpressionWithContextTransformer
import org.dotlin.compiler.backend.util.replace
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

typealias Transformations<E> = Sequence<Transformation<E>>

interface IrLowering {
    fun lower(irFile: IrFile)
    val context: DotlinLoweringContext
}

interface IrTransformerLowering<E : IrElement, R> : IrLowering {
    fun DotlinLoweringContext.transform(element: E): R
}

// TODO?: Simplify lowerings, just make it return the transformed declaration or null, and add helper
// methods for adding/removing declarations.
// In that case, simplify ReturnsLowering (can just make it a single lowering and
// move `transformReturnExpressionsIn` into the lowering function)

interface IrSingleLowering<E : IrElement> : IrTransformerLowering<E, Transformation<E>?>

interface IrMultipleLowering<E : IrElement> : IrTransformerLowering<E, Transformations<E>>

interface IrDeclarationLowering : IrMultipleLowering<IrDeclaration> {
    @Suppress("UNCHECKED_CAST")
    private fun IrDeclarationContainer.transformDeclarations(
        transform: DotlinLoweringContext.(IrDeclaration) -> Transformations<IrDeclaration>
    ) {
        declarations.transformBy({ transform(context, it) }, also = {
            if (it is IrDeclarationContainer) {
                it.transformDeclarations(transform)
            }

            if (it is IrFunction) {
                it.apply {
                    valueParameters = valueParameters.toMutableList<IrDeclaration>()
                        .apply {
                            transformBy(transform = { p -> transform(context, p) })
                        } as List<IrValueParameter>

                    when (val body = body) {
                        is IrBlockBody -> body.statements.transformBy(
                            transform = { statement ->
                                when (statement) {
                                    is IrDeclaration -> transform(context, statement) as Transformations<IrStatement>
                                    else -> emptySequence()
                                }
                            }
                        )
                    }
                }
            }
        })
    }

    override fun lower(irFile: IrFile) = irFile.transformDeclarations { context.transform(it) }

    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration>
}

interface IrFileLowering : IrLowering {
    override fun lower(irFile: IrFile) = context.transform(irFile)

    fun DotlinLoweringContext.transform(file: IrFile)
}

interface IrExpressionLowering : IrSingleLowering<IrExpression> {
    override fun lower(irFile: IrFile) {
        irFile.transformChildrenSafe(
            object : IrExpressionWithContextTransformer() {
                override fun visitExpressionWithContext(
                    expression: IrExpression,
                    expContext: IrExpressionContext
                ): IrExpression {
                    expression.transformChildren(expContext)
                    return expression.transformBy { context.transform(it, expContext) }
                }
            },
            null
        )
    }

    override fun DotlinLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? = noChange()
    fun DotlinLoweringContext.transform(
        expression: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? =
        transform(expression)

    fun List<IrStatement>.withLastAsReturn(at: IrSimpleFunction) =
        dropLast(1) + last().let {
            it as IrExpression

            IrReturnImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                type = it.type,
                returnTargetSymbol = at.symbol,
                value = it
            )
        }
}

private fun <D> IrFile.transformChildrenSafe(transformer: IrElementTransformer<D>, data: D) {
    declarations.toList().forEachIndexed { i, irDeclaration ->
        declarations[i] = irDeclaration.transform(transformer, data) as IrDeclaration
    }
}

fun <E : IrElement> MutableList<E>.transformBy(
    transform: (E) -> Transformations<E>,
    also: (E) -> Unit = {}
) {
    var i = 0
    toList().forEach { childElement ->
        also(childElement)

        transform(childElement).forEach {
            when (it) {
                is Transformation.Add -> {
                    add(
                        index = when {
                            it.before -> i
                            else -> i + 1
                        },
                        it.element
                    )
                    i += 1
                }
                is Transformation.Replace -> when (it.old) {
                    null -> this[i] = it.new
                    else -> replace(it.old, it.new)
                }
                is Transformation.Remove -> {
                    remove(it.element ?: childElement)
                    i -= 1
                }
            }
        }

        i++
    }
}

fun <E : IrExpression> E.transformBy(transform: (E) -> Transformation<E>?): E {
    return when (val transformation = transform(this)) {
        is Transformation.Replace -> {
            if (transformation.old != null && transformation.old !== this) {
                throw UnsupportedOperationException("Cannot replace another expression")
            }

            transformation.new
        }
        is Transformation.Add -> throw UnsupportedOperationException("Cannot add an expression")
        is Transformation.Remove -> throw UnsupportedOperationException("Cannot remove an expression")
        else -> this
    }
}

sealed class Transformation<E : IrElement> {
    class Replace<E : IrElement>(val old: E?, val new: E) : Transformation<E>()
    class Add<E : IrElement>(val element: E, val before: Boolean) : Transformation<E>()
    class Remove<E : IrElement>(val element: E? = null) : Transformation<E>()
}

infix fun <E : IrElement> Transformation<E>.and(other: Transformation<E>) = sequenceOf(this, other)
infix fun <E : IrElement> Transformation<E>.and(other: Sequence<Transformation<E>>) =
    sequenceOf(sequenceOf(this), other).flatten()

infix fun <E : IrElement> Transformation<E>.and(other: Iterable<Transformation<E>>) = and(other.asSequence())

infix fun <E : IrElement> Sequence<Transformation<E>>.and(other: Transformation<E>) = this + other
infix fun <E : IrElement> Sequence<Transformation<E>>.and(other: Sequence<Transformation<E>>) = this + other
infix fun <E : IrElement> Sequence<Transformation<E>>.and(other: Iterable<Transformation<E>>) = this + other

infix fun <E : IrElement> Iterable<Transformation<E>>.and(other: Transformation<E>) = asSequence().and(other)
infix fun <E : IrElement> Iterable<Transformation<E>>.and(other: Iterable<Transformation<E>>) =
    asSequence().and(other.asSequence())

infix fun <E : IrElement> Iterable<Transformation<E>>.and(other: Sequence<Transformation<E>>) =
    this.asSequence().and(other)

fun <E : IrElement> IrTransformerLowering<E, *>.just(transformation: Transformation<E>) = sequenceOf(transformation)
fun <E : IrElement> IrTransformerLowering<E, *>.just(transformation: () -> Transformation<E>) = just(transformation())

fun <E : IrElement> IrTransformerLowering<E, *>.replace(element: E, with: E) =
    Transformation.Replace(old = element, new = with)

fun <E : IrElement> IrTransformerLowering<E, *>.replaceWith(element: E) =
    Transformation.Replace(old = null, new = element)

fun <E : IrElement> IrTransformerLowering<E, *>.add(element: E, before: Boolean = false) =
    Transformation.Add(element, before)

fun <E : IrElement> IrTransformerLowering<E, *>.remove(element: E? = null) = Transformation.Remove(element)

fun <E : IrElement> IrSingleLowering<E>.noChange(): Transformation<E>? = null
fun <E : IrElement> IrMultipleLowering<E>.noChange() = emptySequence<Transformation<E>>()

