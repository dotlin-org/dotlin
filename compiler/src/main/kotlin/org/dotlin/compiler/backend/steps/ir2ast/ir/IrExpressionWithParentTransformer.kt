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
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrExpression

open class IrExpressionWithParentTransformer : IrCustomElementTransformer<IrDeclaration?>() {
    final override fun visitDeclaration(
        declaration: IrDeclarationBase,
        parent: IrDeclaration?
    ): IrStatement {
        val newParent = if (declaration is IrDeclarationParent) declaration else parent
        return super.visitDeclaration(declaration, newParent)
    }

    final override fun visitExpression(expression: IrExpression, parent: IrDeclaration?): IrExpression {
        if (parent == null || parent !is IrDeclarationParent) {
            throw IllegalStateException("Expected parent but was $parent")
        }

        return visitExpressionWithParent(expression, parent)
    }

    open fun <P> visitExpressionWithParent(
        expression: IrExpression,
        parent: P
    ): IrExpression where P : IrDeclaration, P : IrDeclarationParent {
        expression.transformChildren(this, parent)
        return expression
    }

    fun IrExpression.transformChildren(parent: IrDeclaration) =
        transformChildren(this@IrExpressionWithParentTransformer, parent)
}

typealias Transform =
        IrExpressionWithParentTransformer.(expression: IrExpression, parent: IrDeclaration) -> IrExpression

fun IrElement.transformExpressionChildren(
    transformer: IrExpressionWithParentTransformer,
    initialParent: IrDeclaration
) = transformChildren(transformer, initialParent)

private fun IrElement.transformExpressionsWithOptionalParent(
    initialParent: IrDeclaration?,
    transform: Transform
) = transformChildren(
    object : IrExpressionWithParentTransformer() {
        @Suppress("UNCHECKED_CAST")
        override fun <P> visitExpressionWithParent(
            expression: IrExpression,
            parent: P
        ): IrExpression where P : IrDeclaration, P : IrDeclarationParent =
            transform(this, expression, parent)
    },
    initialParent
)

fun IrElement.transformExpressions(
    initialParent: IrDeclaration,
    transform: Transform
) = transformExpressionsWithOptionalParent(initialParent, transform)

fun IrFile.transformExpressions(
    transform: Transform
) = transformExpressionsWithOptionalParent(initialParent = null, transform)