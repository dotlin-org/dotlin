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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDartStatementOrigin.*
import org.dotlin.compiler.backend.steps.ir2ast.ir.extensionReceiverOrNull
import org.dotlin.compiler.backend.steps.ir2ast.ir.isPrimitiveInteger
import org.dotlin.compiler.backend.steps.ir2ast.ir.valueArguments
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.createDartAssignment
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList
import org.dotlin.compiler.dart.ast.expression.*
import org.dotlin.compiler.dart.ast.expression.DartComparisonExpression.*
import org.dotlin.compiler.dart.ast.expression.literal.DartIntegerLiteral
import org.dotlin.compiler.dart.ast.statement.*
import org.dotlin.compiler.dart.ast.statement.trycatch.DartCatchClause
import org.dotlin.compiler.dart.ast.statement.trycatch.DartTryStatement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.superTypes
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.cast
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartStatementTransformer : IrDartAstTransformer<DartStatement>() {
    override fun DartTransformContext.visitReturn(expression: IrReturn, context: DartTransformContext) =
        DartReturnStatement(
            expression = expression.value.let {
                when {
                    it is IrGetObjectValue && it.type.isUnit() -> null
                    else -> it.accept(context)
                }
            }
        )

    override fun DartTransformContext.visitWhen(irWhen: IrWhen, context: DartTransformContext) =
        irWhen.branches.reversed().toList().run {
            drop(1).fold(
                initial = first().let {
                    val thenStatement = it.result.acceptAsStatement(context).wrapInBlock()

                    when (it) {
                        is IrElseBranch -> thenStatement
                        else -> DartIfStatement(
                            condition = it.condition.accept(context),
                            thenStatement = thenStatement
                        )
                    }
                },
                operation = { statement, irBranch ->
                    DartIfStatement(
                        condition = irBranch.condition.accept(context),
                        thenStatement = irBranch.result.acceptAsStatement(context).wrapInBlock(),
                        elseStatement = statement
                    )
                }
            )
        }

    override fun DartTransformContext.visitVariable(irVariable: IrVariable, context: DartTransformContext) =
        irVariable.let {
            DartVariableDeclarationStatement(
                variables = DartVariableDeclarationList(
                    DartVariableDeclaration(
                        name = it.dartName,
                        expression = it.initializer?.accept(context)
                    ),
                    type = irVariable.type.accept(context),
                    isConst = it.isConst,
                    isFinal = !it.isVar,
                    isLate = false
                )
            )
        }

    override fun DartTransformContext.visitTry(irTry: IrTry, context: DartTransformContext) = irTry.let {
        fun IrExpression.acceptAsBlock() = DartBlock(
            statements = (this as IrBlock).statements.accept(context)
        )

        DartTryStatement(
            body = it.tryResult.acceptAsBlock(),
            catchClauses = it.catches.map { catch ->
                DartCatchClause(
                    body = catch.result.acceptAsBlock(),
                    exceptionType = catch.catchParameter.type.accept(context),
                    exceptionParameter = catch.catchParameter.dartName,
                )
            },
            finallyBlock = it.finallyExpression?.acceptAsBlock()
        )
    }

    override fun DartTransformContext.visitLoop(loop: IrLoop, context: DartTransformContext): DartStatement {
        val condition = loop.condition.accept(context)
        val body = loop.body!!.acceptAsStatement(context)

        return when (loop) {
            is IrWhileLoop -> DartSimpleWhileStatement(condition, body)
            is IrDoWhileLoop -> DartDoWhileStatement(condition, body)
            else -> throw UnsupportedOperationException("Unsupported loop: ${loop::class.simpleName}")
        }
    }

    override fun DartTransformContext.visitBlock(irBlock: IrBlock, context: DartTransformContext): DartStatement {
        when (irBlock.origin) {
            FOR_LOOP -> {
                val irPossibleSubject =
                    ((irBlock.statements.first() as IrVariable).initializer as IrCall).dispatchReceiver

                val irWhileLoop = irBlock.statements.last() as IrWhileLoop
                val irBody = irWhileLoop.body as IrBlock

                /**
                 * Note: Removes the variable from the loop block.
                 */
                /**
                 * Note: Removes the variable from the loop block.
                 */
                /**
                 * Note: Removes the variable from the loop block.
                 */
                val loopVariables by lazy {
                    (irBody.statements.first() as IrVariable)
                        .acceptAsStatement(context)
                        .variables.copy(isFinal = false)
                        .also {
                            irBody.statements.removeAt(0)
                        }
                }

                /**
                 * Should be accessed after [loopVariables].
                 */
                val body by lazy { irBody.acceptAsStatement(context) }

                when {
                    // `x until y`, `x..y` and `x downTo y` calls are translated as traditional for-loops.
                    irPossibleSubject.hasOrIsUntilCall() ||
                            irPossibleSubject.hasOrIsPrimitiveNumberRangeToCall() ||
                            irPossibleSubject.hasOrIsDownToCall() -> {
                        irPossibleSubject as IrCall

                        val irSubject: IrCall
                        val step: DartExpression

                        if (irPossibleSubject.isStepCall()) {
                            irSubject = irPossibleSubject.let {
                                it.untilCall() ?: it.primitiveNumberRangeToCall() ?: it.downToCall()
                            }!!
                            step = irPossibleSubject.valueArguments[0]!!.accept(context)
                        } else {
                            irSubject = irPossibleSubject
                            step = DartIntegerLiteral(1)
                        }

                        val from = (irSubject.dispatchReceiver ?: irSubject.extensionReceiverOrNull)!!.let {
                            when {
                                it is IrConstructorCall && it.origin == EXTENSION_CONSTRUCTOR_CALL -> {
                                    it.valueArguments[0]!!
                                }
                                else -> it
                            }.accept(context)
                        }
                        val to = irSubject.valueArguments[0]!!.accept(context)

                        val isInclusive = !irSubject.hasOrIsUntilCall()
                        val isReversed = irSubject.hasOrIsDownToCall()

                        val variables = loopVariables.let {
                            it.copy(variables = listOf(it[0].copy(expression = from)))
                        }
                        val variable = variables[0]

                        return DartForStatement(
                            loopParts = DartForPartsWithDeclarations(
                                variables = variables,
                                condition = DartComparisonExpression(
                                    left = variable.name,
                                    operator = when {
                                        isReversed -> when {
                                            isInclusive -> Operators.GREATER_OR_EQUAL
                                            else -> Operators.GREATER
                                        }
                                        else -> when {
                                            isInclusive -> Operators.LESS_OR_EQUAL
                                            else -> Operators.LESS
                                        }
                                    },
                                    right = to,
                                ),
                                updaters = listOf(
                                    DartAssignmentExpression(
                                        left = variable.name,
                                        operator = when {
                                            isReversed -> DartAssignmentOperator.SUBTRACT
                                            else -> DartAssignmentOperator.ADD
                                        },
                                        right = step
                                    )
                                )
                            ),
                            body = body
                        )
                    }
                    irPossibleSubject?.type?.isDartIterable() == true -> {
                        val subject = irPossibleSubject.accept(context)

                        return DartForStatement(
                            loopParts = DartForEachPartsWithDeclarations(
                                variables = loopVariables.let {
                                    it.copy(variables = listOf(it[0].copy(expression = null)))
                                },
                                iterable = subject
                            ),
                            body = body
                        )
                    }
                }
            }
            PLUSEQ, MINUSEQ, MULTEQ, DIVEQ -> {
                val irOriginalReceiver = irBlock.statements.first().cast<IrVariable>().initializer!!
                val irSetField = irBlock.statements.last().cast<IrSetField>()
                val irReceiver = IrGetFieldImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    symbol = irSetField.symbol,
                    type = irSetField.type,
                ).apply {
                    receiver = irOriginalReceiver
                }

                return createDartAssignment(
                    irBlock.origin,
                    receiver = irReceiver.accept(context),
                    irReceiverType = irReceiver.type,
                    irValue = irSetField.value
                ).asStatement()
            }
        }

        // A regular block. If there's only a single statement in the block, we remove the block.
        return irBlock.run {
            statements.singleOrNull()?.accept(context) ?: DartBlock(
                statements = statements.accept(context)
            )
        }
    }

    override fun DartTransformContext.visitExpression(expression: IrExpression, context: DartTransformContext) =
        expression.accept(context).asStatement()

    private fun IrExpression?.findCallInReceivers(block: (IrExpression?) -> IrCall?): IrCall? =
        block(this).let {
            when {
                it != null -> return it
                isStepCall() -> return (this as? IrCall)?.let { thisCall ->
                    thisCall.dispatchReceiver ?: thisCall.extensionReceiverOrNull
                }.findCallInReceivers(block)
                else -> null
            }
        }

    private fun IrExpression?.findCallWithNameInReceivers(fqName: String): IrCall? =
        findCallInReceivers { if (it.callsWithName(fqName)) it as IrCall else null }

    @OptIn(ExperimentalContracts::class)
    private fun IrExpression?.callsWithName(fqName: String): Boolean {
        contract {
            returns(true) implies (this@callsWithName is IrCall)
        }

        return (this as? IrCall)?.symbol?.owner?.fqNameWhenAvailable == FqName(fqName)
    }

    private fun IrType.isDartIterable(): Boolean =
        classFqName == FqName("dart.core.Iterable") ||
                isArray() || isByteArray() || isShortArray() || isIntArray() || isLongArray() ||
                superTypes().any { it.isDartIterable() }

    private fun IrExpression?.isStepCall() =
        callsWithName("kotlin.ranges.step")

    private fun IrExpression?.untilCall(): IrCall? =
        findCallWithNameInReceivers("kotlin.ranges.until")

    private fun IrExpression?.hasOrIsUntilCall(): Boolean = untilCall() != null

    private fun IrExpression?.downToCall(): IrCall? =
        findCallWithNameInReceivers("kotlin.ranges.downTo")

    private fun IrExpression?.hasOrIsDownToCall(): Boolean = downToCall() != null

    private fun IrExpression?.primitiveNumberRangeToCall() =
        findCallInReceivers {
            when {
                it !is IrCall -> null
                it.dispatchReceiver?.type?.isPrimitiveInteger() == true && it.symbol.owner.name == Name.identifier("rangeTo") -> it
                else -> null
            }
        }

    private fun IrExpression?.hasOrIsPrimitiveNumberRangeToCall() = primitiveNumberRangeToCall() != null
}

fun IrStatement.accept(context: DartTransformContext) = accept(IrToDartStatementTransformer, context)
fun Iterable<IrStatement>.accept(context: DartTransformContext) = map { it.accept(context) }
fun IrExpression.acceptAsStatement(context: DartTransformContext) = accept(IrToDartStatementTransformer, context)
fun IrVariable.acceptAsStatement(context: DartTransformContext) =
    accept(IrToDartStatementTransformer, context) as DartVariableDeclarationStatement





