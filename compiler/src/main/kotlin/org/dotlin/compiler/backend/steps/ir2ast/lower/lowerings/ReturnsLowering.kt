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

import org.dotlin.compiler.backend.steps.ir2ast.IrVoidType
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrAnnotatedExpression
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartConst
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name
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

        val isVoidReturn = returnType is IrVoidType

        fun IrExpression.makeConstIf(condition: Boolean) = when (condition) {
            true -> IrAnnotatedExpression(
                this,
                annotations = listOf(
                    buildStatement(declaration.symbol) {
                        irCallConstructor(
                            dartBuiltIns.dotlin.dartConst.owner.primaryConstructor!!.symbol,
                            typeArguments = emptyList()
                        )

                    }
                )
            )
            false -> this
        }

        body.transformExpressions(initialParent = declaration) { expression, parent ->
            expression.transformChildren(parent)
            if (expression !is IrReturn || expression.isStatementIn(parent)) return@transformExpressions expression

            hasReturnAsExpression = true

            buildStatement(declaration.symbol) {
                val value = when (returnType) {
                    is IrVoidType -> irNull()
                    else -> expression.value
                }

                irThrow(
                    irCallConstructor(
                        returnClass.primaryConstructor!!.symbol,
                        typeArguments = emptyList()
                    ).apply {
                        type = returnClassType
                        putValueArgument(index = 0, value)
                    }.makeConstIf(value.isDartConst())
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
                                val catchVar = buildVariable(
                                    parent = null,
                                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                                    origin = IrDeclarationOrigin.DEFINED, // TODO
                                    name = Name.identifier("r"),
                                    type = returnClassType,
                                    isVar = false,
                                    isConst = false,
                                    isLateinit = false,
                                )

                                this += irCatch(
                                    catchVar,
                                    result = when {
                                        isVoidReturn -> irReturnVoid()
                                        else -> irReturn(
                                            irCall(
                                                returnClass.propertyWithName("value").getter!!,
                                                origin = IrStatementOrigin.GET_PROPERTY
                                            ).apply {
                                                type = catchVar.type
                                                dispatchReceiver = irGet(catchVar)
                                            }
                                        )
                                    }
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

