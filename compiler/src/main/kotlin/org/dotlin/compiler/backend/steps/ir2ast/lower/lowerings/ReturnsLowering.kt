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
import org.dotlin.compiler.backend.steps.ir2ast.ir.propertyWithName
import org.dotlin.compiler.backend.steps.ir2ast.ir.transformExpressions
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.isStatementIn
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.types.Variance

/**
 * Handles returns as expressions and non-local returns.
 *
 * Return expressions are thrown in Dart as a `$Return` instance. The function body is wrapped in a try-catch, and
 * if a `$Return` is caught, the function will return the value given with the `$Return`.
 */
class ReturnsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrFunction) return noChange()

        transformReturnExpressionsIn(declaration)

        return noChange()
    }
}

class ReturnsInFunctionExpressionsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrFunctionExpression) return noChange()

        transformReturnExpressionsIn(expression.function)

        return noChange()
    }
}

private fun DartLoweringContext.transformReturnExpressionsIn(function: IrFunction) {
    val body = function.body as? IrBlockBody ?: return

    var hasReturnAsExpression = false

    val returnType = function.returnType
    val returnClass = dartBuiltIns.dotlin.returnClass.owner
    val returnClassType = returnClass.defaultType.let {
        IrSimpleTypeImpl(
            it.originalKotlinType,
            it.classifier,
            it.nullability,
            arguments = listOf(
                makeTypeProjection(returnType, variance = Variance.INVARIANT)
            ),
            annotations = emptyList(),
            abbreviation = null,
        )
    }

    val returnTargetExp by lazy {
        val fqName = function.fqNameWhenAvailable

        // For local/lambda functions, we use the source offsets to compute the hash.
        val hashCode: Int = if (fqName == null || fqName.shortName().isSpecial) {
            Pair(function.startOffset, function.endOffset).hashCode()
        } else {
            fqName.toString().hashCode()
        }

        hashCode.toIrConst(irBuiltIns.intType)
    }

    body.transformExpressions(initialParent = function) { expression, context ->
        if (expression !is IrReturn ||
            // We only handle our own returns. Nested local/lambda functions are handled later.
            expression.returnTargetSymbol != function.symbol ||
            // If the return is a statement, and it just the return target is the function it is in, it's a normal
            // return and we don't have to do anything.
            (expression.isStatementIn(context.container) && expression.returnTargetSymbol == context.container.symbol)
        ) {
            return@transformExpressions expression
        }

        hasReturnAsExpression = true

        buildStatement(function.symbol) {
            val value = when {
                returnType.isUnit() -> irNull()
                else -> expression.value
            }

            irThrow(
                irCallConstructor(
                    returnClass.primaryConstructor!!.symbol,
                    typeArguments = emptyList()
                ).apply {
                    type = returnClassType
                    putValueArgument(index = 0, value)
                    putValueArgument(
                        index = 1,
                        returnTargetExp
                    )
                }
            )
        }
    }

    if (hasReturnAsExpression) {
        body.statements.apply {
            val allStatements = toList()
            clear()

            add(
                buildStatement(function.symbol) {
                    IrTryImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        type = irBuiltIns.unitType,
                        tryResult = IrBlockImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            type = irBuiltIns.unitType,
                            origin = null,
                            statements = allStatements,
                        ),
                        catches = buildList {
                            val catchVar = scope.createTemporaryVariableDeclaration(
                                irType = returnClassType,
                                nameHint = "return",
                                isMutable = false,
                                startOffset = SYNTHETIC_OFFSET,
                                endOffset = SYNTHETIC_OFFSET
                            )

                            fun irGetCatchVarProperty(name: String) = irCall(
                                returnClass.propertyWithName(name).getter!!,
                                receiver = irGet(catchVar),
                                origin = IrStatementOrigin.GET_PROPERTY
                            )

                            this += irCatch(
                                catchVar,
                                result = irIfThenElse(
                                    returnClassType,
                                    condition = irEquals(
                                        irGetCatchVarProperty("target"),
                                        returnTargetExp,
                                    ),
                                    thenPart = irReturn(
                                        when {
                                            returnType.isUnit() -> irGetObject(irBuiltIns.unitClass)
                                            else -> irGetCatchVarProperty("value")
                                        }
                                    ),
                                    elsePart = irThrow(irGet(catchVar)) // TODO: Rethrow
                                )
                            )
                        },
                        finallyExpression = null
                    )
                }
            )
        }
    }
}

