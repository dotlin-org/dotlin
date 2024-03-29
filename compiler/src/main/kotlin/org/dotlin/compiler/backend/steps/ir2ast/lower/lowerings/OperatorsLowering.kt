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
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.primitiveOp2
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.util.isMethodOfAny
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addIfNotNull

@Suppress("UnnecessaryVariable")
class OperatorsLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrSimpleFunction || !declaration.isOperator) return noChange()

        val irFunction = declaration
        val irIdentifier = irFunction.name.identifier
        val operatorOrigin = IrDotlinDeclarationOrigin.SYNTHETIC_OPERATOR

        // Invoke can be translated as-is, just change the name to 'call'.
        if (irIdentifier == "invoke") {
            return just {
                replaceWith(
                    irFunction.deepCopyWith {
                        name = Name.identifier("call")
                        isOperator = false
                        origin = operatorOrigin
                    }
                )
            }
        }

        val operators = mutableListOf<IrFunction>()

        operators += when {
            // For compareTo, we add 4 operators ('<', '>', '<=' and '>=') in Dart.
            irIdentifier == "compareTo" && !irFunction.isOverride -> {
                listOf("<", ">", "<=", ">=").map { operatorIdentifier ->
                    irFunction.deepCopyWith(remapReferences = false) {
                        name = Name.identifier(operatorIdentifier)
                        isOperator = true
                        returnType = irBuiltIns.booleanType
                        origin = operatorOrigin
                    }.apply {
                        val intClassifier = irBuiltIns.intType.classifierOrFail
                        val (operatorStatementOrigin, operatorSymbol) = irBuiltIns.run {
                            when (operatorIdentifier) {
                                "<" -> IrStatementOrigin.LT to lessFunByOperandType[intClassifier]!!
                                ">" -> IrStatementOrigin.GT to greaterFunByOperandType[intClassifier]!!
                                "<=" -> IrStatementOrigin.LTEQ to lessOrEqualFunByOperandType[intClassifier]!!
                                ">=" -> IrStatementOrigin.GTEQ to greaterOrEqualFunByOperandType[intClassifier]!!
                                else -> throw IllegalStateException()
                            }
                        }

                        val otherParam = irFunction.valueParameters.first().copy(parent = this)

                        body = irFactory.createExpressionBody(
                            buildStatement(symbol) {
                                primitiveOp2(
                                    UNDEFINED_OFFSET,
                                    UNDEFINED_OFFSET,
                                    primitiveOpSymbol = operatorSymbol,
                                    primitiveOpReturnType = irBuiltIns.booleanType,
                                    origin = operatorStatementOrigin,
                                    argument1 = irCall(irFunction).apply {
                                        setReceiverFrom(irFunction)
                                        putValueArgument(index = 0, irGet(otherParam))
                                    },
                                    argument2 = irInt(0)
                                )
                            }
                        )

                        valueParameters = listOf(otherParam)
                    }
                }
            }
            else -> emptyList()
        }

        // Since we can't have callable references to operators in Dart, we add an extra operator method, which calls
        // the original method.
        operators.addIfNotNull(
            when (irIdentifier) {
                "plus" -> "+"
                "minus" -> "-"
                "times" -> "*"
                "div" -> "/"
                "rem" -> "%"
                "equals" -> "=="
                "get" -> when (irFunction.valueParameters.size) {
                    // Dart supports only 1 parameter for the indexing operator.
                    1 -> "[]"
                    else -> null
                }
                "set" -> when (irFunction.valueParameters.size) {
                    // Dart supports only 2 parameters for the indexing set operator.
                    2 -> "[]="
                    else -> null
                }
                else -> null
            }?.let { operatorIdentifier ->
                val parameters: List<IrValueParameter>

                val redirectOrigin = IrDotlinStatementOrigin.OPERATOR_REDIRECT

                val body: (DeclarationIrBuilder) -> IrExpression = when (irIdentifier) {
                    "plus", "minus", "times", "div", "rem", "equals", "get" -> {
                        val otherParam = irFunction.valueParameters.first().copy()
                        parameters = listOf(otherParam)

                        fun(irBuilder: DeclarationIrBuilder) = irBuilder.buildStatement {
                            irCall(irFunction, origin = redirectOrigin).apply {
                                setReceiverFrom(irFunction)
                                putValueArgument(index = 0, irGet(otherParam))
                            }
                        }

                    }
                    "set" -> {
                        // Key & value parameters.
                        parameters = irFunction.valueParameters.take(2).map { it.copy() }

                        fun(irBuilder: DeclarationIrBuilder) = irBuilder.buildStatement {
                            irCall(irFunction, origin = redirectOrigin).apply {
                                setReceiverFrom(irFunction)
                                parameters.forEach { putValueArgument(it.index, irGet(it)) }
                            }
                        }
                    }
                    else -> throw UnsupportedOperationException()
                }

                irFunction.deepCopyWith {
                    name = Name.identifier(operatorIdentifier)
                    isOperator = true
                    origin = operatorOrigin

                    if (operatorIdentifier == "[]=") {
                        returnType = irBuiltIns.unitType
                    }
                }.apply {
                    val irOperatorFunction = this

                    this.body = IrExpressionBodyImpl(body(createIrBuilder(symbol)))
                    valueParameters = parameters.onEach {
                        it.parent = irOperatorFunction
                    }
                }
            }
        )

        return replaceWith(
            irFunction.deepCopyWith {
                // This function should not be marked as an operator anymore, since only the newly added operator method
                // will be the actual Dart operator, if it's added.
                isOperator = false
                origin = IrDotlinDeclarationOrigin.WAS_OPERATOR
            }.apply {
                // The equals method itself is not an override in Dart, only the actual '==' operator method is.
                if (irIdentifier == "equals" && irFunction.isMethodOfAny()) {
                    overriddenSymbols = emptyList()
                }
            }
        ) and operators.map { add(it) }
    }

    private fun <S : IrSymbol> IrMemberAccessExpression<S>.setReceiverFrom(irFunction: IrFunction) {
        fun irGet(symbol: IrValueSymbol) = IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, symbol)

        if (irFunction.dispatchReceiverParameter != null) {
            dispatchReceiver = irGet(irFunction.dispatchReceiverParameter!!.symbol)
        } else {
            extensionReceiver = irGet(irFunction.extensionReceiverParameter!!.symbol)
        }
    }

    private fun IrValueParameter.copy(parent: IrFunction): IrValueParameter = copy(
        parent = parent,
        origin = IrDotlinDeclarationOrigin.SYNTHETIC_OPERATOR
    )
}
