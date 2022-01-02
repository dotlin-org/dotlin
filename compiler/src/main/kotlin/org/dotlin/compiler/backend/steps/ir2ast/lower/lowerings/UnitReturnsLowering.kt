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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.irReturnVoid
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.copyAttributes
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.IrReturn

class UnitReturnsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
        if (expression !is IrReturn) return noChange()

        val value = expression.value
        if (value !is IrGetObjectValue || value.symbol != irBuiltIns.unitClass) return noChange()

        return replaceWith(
            buildStatement(container.symbol) {
                irReturnVoid(expression.returnTargetSymbol)
                    .copyAttributes(expression)
            }
        )
    }
}

