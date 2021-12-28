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

import org.dotlin.compiler.backend.dartCatchAsType
import org.dotlin.compiler.backend.steps.ir2ast.ir.constructorWithName
import org.dotlin.compiler.backend.steps.ir2ast.ir.remapReferences
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.dartNameOrNull
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.primaryConstructor

/**
 * Must run after [TryExpressionsLowering].
 */
class DartCatchAsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
        if (expression !is IrTry) return noChange()
        if (expression !is IrTryImpl) throw UnsupportedOperationException("IrThrow must be IrThrowImpl")

        for (it in expression.catches) {
            val catchParameterClass = it.catchParameter.type.classOrNull?.owner ?: continue
            val catchAsType = catchParameterClass.dartCatchAsType ?: continue
            val actualType = it.catchParameter.type
            val constructor = catchParameterClass.constructors.first { it.dartNameOrNull?.baseValue == "from" }

            val tempVar = buildStatement(container.symbol) {
                scope.createTemporaryVariable(
                    irIfThenElse(
                        type = actualType,
                        condition = irNotIs(
                            argument = irGet(it.catchParameter),
                            type = actualType,
                        ),
                        thenPart = irCallConstructor(
                            constructor.symbol,
                            typeArguments = emptyList()
                        ).apply {
                            putValueArgument(0, irGet(it.catchParameter))
                        },
                        elsePart = irGet(it.catchParameter)
                    ),
                    nameHint = "catchAs"
                )
            }

            (it.result as IrBlock).apply {
                remapReferences(it.catchParameter.symbol to tempVar.symbol)
                statements.add(index = 0, tempVar)
            }

            it.catchParameter.type = catchAsType
        }

        return noChange()
    }
}