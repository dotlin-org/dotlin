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


import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.defaultType
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

        val body = declaration.body as? IrBlockBody ?: return noChange()

        var hasReturnAsExpression = false

        val returnType = declaration.returnType
        val returnClass = dartBuiltIns.dotlin.returnClass.owner
        val returnClassType = returnClass.defaultType.let {
            IrSimpleTypeImpl(
                kotlinType = it.originalKotlinType,
                classifier = it.classifier,
                hasQuestionMark = it.hasQuestionMark,
                arguments = listOf(
                    makeTypeProjection(returnType, variance = Variance.INVARIANT)
                ),
                annotations = emptyList(),
                abbreviation = null,
            )
        }

        body.transformExpressions(initialParent = declaration) { expression, context ->
            expression.transformChildren(context)
            if (expression !is IrReturn || expression.isStatementIn(context.container)) return@transformExpressions expression

            hasReturnAsExpression = true

            buildStatement(declaration.symbol) {
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
                    }
                )
            }
        }

        if (hasReturnAsExpression) {
            body.statements.apply {
                val allStatements = toList()
                clear()

                add(
                    buildStatement(declaration.symbol) {
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

                                this += irCatch(
                                    catchVar,
                                    result = irReturn(
                                        when {
                                            returnType.isUnit() -> irGetObject(irBuiltIns.unitClass)
                                            else -> irCall(
                                                returnClass.propertyWithName("value").getter!!,
                                                receiver = irGet(catchVar),
                                                origin = IrStatementOrigin.GET_PROPERTY
                                            )
                                        }
                                    )
                                )
                            },
                            finallyExpression = null
                        )
                    }
                )
            }
        }

        return noChange()
    }
}

