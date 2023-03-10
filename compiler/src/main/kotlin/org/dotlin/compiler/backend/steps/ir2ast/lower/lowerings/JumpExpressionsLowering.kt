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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrExpressionTransformDirection.OUTSIDE_IN
import org.dotlin.compiler.backend.steps.ir2ast.ir.irCall
import org.dotlin.compiler.backend.steps.ir2ast.ir.propertyWithName
import org.dotlin.compiler.backend.steps.ir2ast.ir.transformExpressions
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.isStatementIn
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

/**
 * Handles jumps as expressions and non-local returns.
 *
 * Return/continue/break expressions are thrown in Dart as `$Return`/`$Continue`/`$Break` instances, respectively.
 * The function body is wrapped in a try-catch, and if a `$Return`/`$Continue`/`$Break` is caught,
 * the function will either return the value given with the `$Return`, or `continue` or `break`.
 */
class JumpExpressionsLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrFunction) return noChange()

        transformReturnExpressionsIn(declaration)
        transformBreakContinueExpressionsIn(declaration)

        return noChange()
    }
}

class JumpExpressionsInFunctionExpressionsLowering(override val context: DotlinLoweringContext) : IrExpressionLowering {
    override fun DotlinLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
        if (expression !is IrFunctionExpression) return noChange()

        transformReturnExpressionsIn(expression.function)
        transformBreakContinueExpressionsIn(expression.function)

        return noChange()
    }
}

private fun DotlinLoweringContext.transformReturnExpressionsIn(function: IrFunction) {
    val body = function.body as? IrBlockBody ?: return

    var hasReturnAsExpression = false

    val returnType = function.returnType
    val returnClass = dotlinIrBuiltIns.returnClass.owner
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

    val irJumpTarget by lazy { function.irJumpTarget() }

    body.transformExpressions(initialParent = function) { expression, context ->
        if (expression !is IrReturn ||
            // We only handle our own returns. Nested local/lambda functions are handled later.
            expression.returnTargetSymbol != function.symbol ||
            // If the return is a statement, and it just the return target is the function it is in, it's a normal
            // return, and we don't have to do anything.
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

            function.irThrowJump(returnClass, returnClassType, irJumpTarget, value)
        }
    }

    if (hasReturnAsExpression) {
        body.wrapInTry(function, returnClass, returnClassType, irJumpTarget) { irGetJumpClassProperty ->
            irReturn(
                when {
                    function.returnType.isUnit() -> irGetObject(irBuiltIns.unitClass)
                    else -> irGetJumpClassProperty("value")
                }
            )
        }
    }
}

private fun DotlinLoweringContext.transformBreakContinueExpressionsIn(function: IrFunction) {
    val body = function.body as? IrBlockBody ?: return

    val continueClass by lazy { dotlinIrBuiltIns.continueClass.owner }
    val breakClass by lazy { dotlinIrBuiltIns.breakClass.owner }

    val irJumpTargetsAndIrJumpsByLoop = mutableMapOf<IrLoop, Pair<IrConst<*>, MutableList<IrBreakContinue>>>()

    lateinit var currentBlock: IrBlock
    body.transformExpressions(initialParent = function, direction = OUTSIDE_IN) trans@{ expression, context ->
        when (expression) {
            is IrBlock -> {
                currentBlock = expression
                expression
            }

            is IrBreakContinue -> when {
                !expression.isStatementIn(context.container) || expression.loop.body != currentBlock -> {
                    val (irJumpTarget, loopJumps) = irJumpTargetsAndIrJumpsByLoop.computeIfAbsent(expression.loop) {
                        it.irJumpTarget(function) to mutableListOf()
                    }
                    loopJumps.add(expression)

                    val jumpClass = when (expression) {
                        is IrContinue -> continueClass
                        is IrBreak -> breakClass
                        else -> throw UnsupportedOperationException("Unexpected jump: $expression")
                    }

                    function.irThrowJump(jumpClass, jumpClass.defaultType, irJumpTarget)
                }
                else -> expression
            }
            else -> expression
        }
    }

    irJumpTargetsAndIrJumpsByLoop.forEach { loop, (irJumpTarget, irJumps) ->
        // We use the `loop` property from these instances, we don't use these ourselves, so it doesn't matter
        // what we pass to the newly created jump expression.
        val anyIrContinue = irJumps.firstIsInstanceOrNull<IrContinue>()
        val anyIrBreak = irJumps.firstIsInstanceOrNull<IrBreak>()

        fun wrapInTry(jumpClass: IrClass, jump: IrSingleStatementBuilder.() -> IrExpression) =
            loop.body.cast<IrBlock>().wrapInTry(function, jumpClass, jumpClass.defaultType, irJumpTarget) { jump() }

        if (anyIrContinue != null) {
            wrapInTry(continueClass) { irContinue(anyIrContinue.loop) }
        }

        if (anyIrBreak != null) {
            wrapInTry(breakClass) { irBreak(anyIrBreak.loop) }
        }
    }
}

context(DotlinLoweringContext)
private fun IrFunction.irThrowJump(
    jumpClass: IrClass,
    jumpClassType: IrType,
    jumpTarget: IrExpression,
    value: IrExpression? = null
) = buildStatement(symbol) {
    irThrow(
        irCallConstructor(
            jumpClass.primaryConstructor!!.symbol,
            typeArguments = emptyList()
        ).apply {
            this.type = jumpClassType

            if (value != null) {
                putValueArgument(index = 0, value)
            }

            putValueArgument(
                index = if (value != null) 1 else 0,
                jumpTarget
            )
        }
    )
}

context(DotlinLoweringContext)
private fun IrStatementContainer.wrapInTry(
    function: IrFunction,
    jumpClass: IrClass,
    jumpClassType: IrType,
    jumpTarget: IrExpression,
    jump: IrSingleStatementBuilder.(irGetJumpClassProperty: (name: String) -> IrCall) -> IrExpression
) {
    statements.apply {
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
                            irType = jumpClassType,
                            nameHint = jumpClass.name.asString().drop(1).lowercase(),
                            isMutable = false,
                            startOffset = SYNTHETIC_OFFSET,
                            endOffset = SYNTHETIC_OFFSET
                        )

                        fun irGetJumpClassProperty(name: String) = irCall(
                            jumpClass.propertyWithName(name).getter!!,
                            receiver = irGet(catchVar),
                            origin = IrStatementOrigin.GET_PROPERTY
                        )

                        this += irCatch(
                            catchVar,
                            result = irIfThenElse(
                                jumpClassType,
                                condition = irEquals(
                                    irGetJumpClassProperty("target"),
                                    jumpTarget,
                                ),
                                thenPart = jump(::irGetJumpClassProperty),
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

context(DotlinLoweringContext)
private fun IrElement.irJumpTarget(function: IrFunction): IrConst<*> {
    val props = when (val fqName = function.fqNameWhenAvailable) {
        null -> when (val startAndEnd = startOffset to endOffset) {
            UNDEFINED_OFFSET to UNDEFINED_OFFSET, SYNTHETIC_OFFSET to SYNTHETIC_OFFSET -> {
                throw UnsupportedOperationException("Could not compute jump target for $this in $function")
            }

            else -> startAndEnd
        }

        else -> Triple(startOffset, endOffset, fqName)
    }

    return props.hashCode().toIrConst(irBuiltIns.intType)
}

context(DotlinLoweringContext)
private fun IrFunction.irJumpTarget(): IrConst<*> = irJumpTarget(this)