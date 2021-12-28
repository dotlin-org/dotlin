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

@file:Suppress("DIFFERENT_NAMES_FOR_THE_SAME_PARAMETER_IN_SUPERTYPES", "PARAMETER_NAME_CHANGED_ON_OVERRIDE")

package org.dotlin.compiler.backend.steps.ir2ast.lower

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementVisitorVoid
import org.dotlin.compiler.backend.util.replace
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.Name

typealias Transformations<E> = Sequence<Transformation<E>>

interface IrLowering {
    fun lower(irFile: IrFile)
    val context: DartLoweringContext
}

interface IrTransformerLowering<E : IrElement, R> : IrLowering {
    fun DartLoweringContext.transform(element: E): R
}

interface IrSingleLowering<E : IrElement> : IrTransformerLowering<E, Transformation<E>?>

interface IrMultipleLowering<E : IrElement> : IrTransformerLowering<E, Transformations<E>>

interface IrDeclarationLowering : IrMultipleLowering<IrDeclaration> {
    @Suppress("UNCHECKED_CAST")
    private fun IrDeclarationContainer.transformDeclarations(
        transform: DartLoweringContext.(IrDeclaration) -> Transformations<IrDeclaration>
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
                }
            }
        })
    }

    override fun lower(irFile: IrFile) = irFile.transformDeclarations { context.transform(it) }

    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration>
}

interface IrFileLowering : IrLowering {
    override fun lower(irFile: IrFile) = context.transform(irFile)

    fun DartLoweringContext.transform(file: IrFile)
}

interface IrExpressionLowering : IrSingleLowering<IrExpression> {
    override fun lower(irFile: IrFile) {
        irFile.transformChildren(
            object : IrElementTransformer<IrDeclaration?> {
                override fun visitDeclaration(
                    declaration: IrDeclarationBase,
                    parent: IrDeclaration?
                ): IrStatement {
                    val newParent = if (declaration is IrDeclarationParent) declaration else parent
                    return super.visitDeclaration(declaration, newParent)
                }

                override fun visitExpression(expression: IrExpression, parent: IrDeclaration?): IrExpression {
                    if (parent == null || parent !is IrDeclarationParent) {
                        throw IllegalStateException("Expected parent but was $parent")
                    }

                    expression.transformChildren(this, parent)
                    return expression.transformBy { context.transform(it, parent) }
                }
            },
            null
        )
    }

    override fun DartLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? = noChange()
    fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent =
        transform(expression)

    fun <D> DartLoweringContext.wrapInAnonymousFunctionInvocation(
        exp: IrExpression,
        container: D,
        statements: (IrSimpleFunction) -> List<IrStatement> = { listOf(exp) }
    ): IrCall where D : IrDeclaration, D : IrDeclarationParent {
        val anonymousFunction = context.irFactory.buildFun {
            name = Name.special("<anonymous>")
            returnType = exp.type
        }.apply {
            parent = container

            body = IrBlockBodyImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                statements(this)
            )
        }

        val invokeMethod = irFactory.buildFun {
            name = Name.identifier("invoke")
            isOperator = true
            returnType = exp.type
        }.apply {
            parent = anonymousFunction
            dispatchReceiverParameter = irBuiltIns.functionN(0).thisReceiver
        }

        val functionExpression = IrFunctionExpressionImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            type = exp.type,
            function = anonymousFunction,
            origin = IrStatementOrigin.INVOKE
        )

        return IrCallImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            type = exp.type,
            symbol = invokeMethod.symbol,
            typeArgumentsCount = 0,
            valueArgumentsCount = 0,
            origin = IrStatementOrigin.INVOKE,
        ).apply {
            dispatchReceiver = functionExpression
        }
    }

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

interface IrStatementLowering : IrMultipleLowering<IrStatement> {
    override fun lower(irFile: IrFile) {
        irFile.acceptChildrenVoid(
            object : IrCustomElementVisitorVoid {
                override fun visitBlockBody(body: IrBlockBody) {
                    super.visitBlockBody(body)

                    body.statements.transformBy({ context.transform(it, body) })
                }

                override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
            }
        )
    }

    override fun DartLoweringContext.transform(statement: IrStatement) = noChange()
    fun DartLoweringContext.transform(statement: IrStatement, body: IrBlockBody): Transformations<IrStatement> =
        transform(statement)
}

interface IrBodyExpressionLowering : IrSingleLowering<IrExpression> {
    override fun lower(irFile: IrFile) {
        irFile.acceptChildrenVoid(
            object : IrCustomElementVisitorVoid {
                override fun visitExpressionBody(body: IrExpressionBody) {
                    super.visitExpressionBody(body)

                    body.expression = body.expression.transformBy { context.transform(it, body) }
                }

                override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
            }
        )
    }

    override fun DartLoweringContext.transform(expression: IrExpression) = noChange()

    fun DartLoweringContext.transform(expression: IrExpression, body: IrExpressionBody) = transform(expression)
}

interface IrStatementAndBodyExpressionLowering : IrLowering {
    val statementTransformer: IrStatementLowering
    val bodyExpressionTransformer: IrBodyExpressionLowering

    override fun lower(irFile: IrFile) {
        statementTransformer.lower(irFile)
        bodyExpressionTransformer.lower(irFile)
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
                    add(i + 1, it.element)
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
    class Add<E : IrElement>(val element: E) : Transformation<E>()
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

fun <E : IrElement> IrTransformerLowering<E, *>.add(element: E) = Transformation.Add(element)
fun <E : IrElement> IrTransformerLowering<E, *>.remove(element: E? = null) = Transformation.Remove(element)

fun <E : IrElement> IrSingleLowering<E>.noChange(): Transformation<E>? = null
fun <E : IrElement> IrMultipleLowering<E>.noChange() = emptySequence<Transformation<E>>()

