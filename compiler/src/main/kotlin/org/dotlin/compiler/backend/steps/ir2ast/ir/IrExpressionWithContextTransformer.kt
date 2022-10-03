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

package org.dotlin.compiler.backend.steps.ir2ast.ir

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression

sealed class IrInitializable {
    class Field(val field: IrField) : IrInitializable()
    class Variable(val variable: IrVariable) : IrInitializable()

    val declaration: IrDeclaration
        get() = when (this) {
            is Field -> this.field
            is Variable -> this.variable
        }

    val initializer: IrExpression?
        get() = when (this) {
            is Field -> this.field.initializer?.expression
            is Variable -> this.variable.initializer
        }
}

data class IrExpressionContext(
    val container: IrDeclaration, // TODO: Use `IrDeclaration & IrDeclarationParent` if intersection types are supported
    val initializerContainer: IrInitializable? = null
)

open class IrExpressionWithContextTransformer : IrCustomElementTransformer<IrExpressionContext?>() {
    final override fun visitDeclaration(
        declaration: IrDeclarationBase,
        context: IrExpressionContext?
    ): IrStatement {
        val newContext = context.run {
            val newContainer = when (declaration) {
                is IrDeclarationParent -> declaration
                else -> this?.container
            }

            val newInitializerContainer = when (declaration) {
                is IrField -> IrInitializable.Field(declaration)
                is IrVariable -> IrInitializable.Variable(declaration)
                else -> this?.initializerContainer
            }

            when {
                this?.container != newContainer || this?.initializerContainer != newInitializerContainer ->
                    newContainer?.let {
                        IrExpressionContext(it, newInitializerContainer)
                    }
                else -> context
            }
        }

        return super.visitDeclaration(declaration, newContext)
    }

    final override fun visitExpression(expression: IrExpression, context: IrExpressionContext?): IrExpression {
        if (context == null || context.container !is IrDeclarationParent) {
            throw IllegalStateException("Expected parent but was ${context?.container}")
        }

        return visitExpressionWithContext(expression, context)
    }

    open fun visitExpressionWithContext(
        expression: IrExpression,
        context: IrExpressionContext
    ): IrExpression{
        expression.transformChildren(context)
        return expression
    }

    fun IrExpression.transformChildren(context: IrExpressionContext) =
        transformChildren(this@IrExpressionWithContextTransformer, context)
}

typealias Transform =
        IrExpressionWithContextTransformer.(expression: IrExpression, context: IrExpressionContext) -> IrExpression

private fun IrElement.transformExpressionsWithOptionalParent(
    initialParent: IrDeclaration?,
    transform: Transform
) = transformChildren(
    object : IrExpressionWithContextTransformer() {
        override fun visitExpressionWithContext(
            expression: IrExpression,
            context: IrExpressionContext
        ): IrExpression {
            expression.transformChildren(context)
            return transform(this, expression, context)
        }
    },
    when (initialParent) {
        null -> null
        else -> {
            initialParent as IrDeclarationParent
            IrExpressionContext(initialParent)
        }
    }
)

fun IrElement.transformExpressions(
    initialParent: IrDeclaration,
    transform: Transform
) = transformExpressionsWithOptionalParent(initialParent, transform)

fun IrElement.transformExpressions(
    transform: Transform
) = transformExpressionsWithOptionalParent(initialParent = null, transform)