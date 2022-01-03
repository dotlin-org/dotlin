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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.irCall
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.name.Name

/**
 * Const `Float`s, e.g. `1.0F / 0.0F` are compiled to e.g. `Float.POSITIVE_INFINITY`, and then the output in Dart
 * would literally be `Infinity`. This lowering makes the unrepresentable decimals their equations again.
 */
@Suppress("UnnecessaryVariable")
class UnrepresentableDecimalConstsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    @Suppress("ConvertNaNEquality")
    override fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
        if (expression !is IrConst<*>
            || (expression.kind != IrConstKind.Float && expression.kind != IrConstKind.Double)
        ) {
            return noChange()
        }

        if (expression.value.let {
                it != Float.NaN && it != Double.NaN &&
                        it != Float.POSITIVE_INFINITY && it != Double.POSITIVE_INFINITY &&
                        it != Float.NEGATIVE_INFINITY && it != Double.NEGATIVE_INFINITY
            }) {
            return noChange()
        }

        val decimalClass: IrClass
        val decimalType: IrType
        val one: IrConst<*>
        val zero: IrConst<*>
        irBuiltIns.run {
            when (expression.kind) {
                IrConstKind.Float -> {
                    decimalClass = floatClass.owner
                    decimalType = floatType
                    one = 1.0F.toIrConst(floatType)
                    zero = 0.0F.toIrConst(floatType)
                }
                else -> {
                    decimalClass = doubleClass.owner
                    decimalType = doubleType
                    one = 1.0.toIrConst(doubleType)
                    zero = 0.0F.toIrConst(floatType)
                }
            }
        }

        val decimalMethods = decimalClass.declarations.filterIsInstance<IrSimpleFunction>()

        val divideMethod = decimalMethods.single {
            it.name == Name.identifier("div") && it.valueParameters.singleOrNull()?.type == decimalType
        }

        return replaceWith(
            buildStatement(container.symbol) {
                fun divide(left: IrExpression, right: IrExpression) =
                    irCall(divideMethod, left, right, origin = IrStatementOrigin.DIV)

                val infinity by lazy { divide(one, zero) }
                val unaryMinus by lazy { decimalMethods.single { it.name == Name.identifier("unaryMinus") } }
                fun unaryMinus(arg: IrExpression) = irCall(unaryMinus, arg, origin = IrStatementOrigin.UMINUS)

                when (expression.value) {
                    Float.NaN, Double.NaN -> unaryMinus(divide(zero, zero))
                    Float.POSITIVE_INFINITY, Double.POSITIVE_INFINITY -> infinity
                    Float.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY -> unaryMinus(infinity)
                    else -> return noChange()
                }
            }
        )
    }
}