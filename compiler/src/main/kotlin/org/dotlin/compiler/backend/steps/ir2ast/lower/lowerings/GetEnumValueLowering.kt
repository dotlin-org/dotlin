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

import org.dotlin.compiler.backend.steps.ir2ast.ir.fieldWithName
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentAsClass

@Suppress("UnnecessaryVariable")
class GetEnumValueLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrGetEnumValue) return noChange()

        val enumClass = expression.symbol.owner.parentAsClass

        val field = enumClass.declarations.fieldWithName(expression.symbol.owner.name.identifier)

        return replaceWith(
            IrGetFieldImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                symbol = field.symbol,
                type = enumClass.defaultType,
            )
        )
    }
}