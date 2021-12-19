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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.hasDartGetterAnnotation
import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.*
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.*
import org.dotlin.compiler.backend.util.toPair
import org.dotlin.compiler.dart.ast.collection.DartCollectionElementList
import org.dotlin.compiler.dart.ast.expression.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.invocation.DartFunctionExpressionInvocation
import org.dotlin.compiler.dart.ast.expression.invocation.DartMethodInvocation
import org.dotlin.compiler.dart.ast.expression.literal.*
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.*
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isChar
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.util.irCall

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartExpressionTransformer : IrDartAstTransformer<DartExpression> {
    override fun visitExpression(expression: IrExpression, context: DartTransformContext): DartExpression {
        return when (expression) {
            is IrDartCodeExpression -> visitCode(expression, context)
            is IrAnnotatedExpression -> visitAnnotatedExpression(expression, context)
            is IrNullAwareExpression -> visitNullAwareExpression(expression, context)
            is IrBinaryInfixExpression -> visitBinaryInfixExpression(expression, context)
            else -> super.visitExpression(expression, context)
        }
    }

    override fun visitExpressionBody(irBody: IrExpressionBody, data: DartTransformContext) =
        irBody.expression.accept(data)

    override fun visitFunctionAccess(
        irCallLike: IrFunctionAccessExpression,
        context: DartTransformContext,
    ): DartExpression {
        val irLeft = irCallLike.extensionReceiver ?: irCallLike.dispatchReceiver
        val irRight by lazy { irCallLike.getValueArgument(0)!! }
        val optionalLeft by lazy { irLeft?.accept(context) }
        val left by lazy { optionalLeft!! }
        val right by lazy { irRight.accept(context) }

        val infixLeft by lazy { left.possiblyParenthesize() }
        val infixRight by lazy { right.possiblyParenthesize() }

        fun methodInvocation(methodName: DartSimpleIdentifier): DartMethodInvocation {
            return DartMethodInvocation(
                target = left,
                methodName = methodName,
                arguments = DartArgumentList(right)
            )
        }

        return when (val origin = irCallLike.origin) {
            IrStatementOrigin.PLUS -> {
                val irLeftType = irLeft!!.type
                val irRightType = irRight.type

                when {
                    // Built-in: String + Any? (or null) will use the Dart extension.
                    irLeftType.isString() && !irRightType.isString() -> {
                        methodInvocation(irCallLike.symbol.owner.dartNameAsSimple)
                    }
                    // Built-in: Char + Int will use the Dart extension.
                    irLeftType.isChar() && irRightType.isInt() -> {
                        methodInvocation(irCallLike.symbol.owner.dartNameAsSimple)
                    }
                    else -> DartPlusExpression(infixLeft, infixRight)
                }
            }
            IrStatementOrigin.MINUS -> when {
                // Built-in: String - int will use the Dart extension.
                irLeft!!.type.isChar() && irRight.type.isInt() -> {
                    methodInvocation(irCallLike.symbol.owner.dartNameAsSimple)
                }
                else -> DartMinusExpression(infixLeft, infixRight)
            }
            IrStatementOrigin.MUL -> DartMultiplyExpression(infixLeft, infixRight)
            IrStatementOrigin.DIV -> {
                when {
                    // Dart's int divide operator returns a double, while Kotlin's Int divide operator returns an
                    // Int. So, we use the ~/ Dart operator, which returns an int.
                    irLeft!!.type.isInt() && irRight.type.isInt() -> DartIntegerDivideExpression(infixLeft, infixRight)
                    else -> DartDivideExpression(infixLeft, infixRight)
                }
            }
            IrStatementOrigin.PERC -> DartModuloExpression(left, right)
            IrStatementOrigin.GT, IrStatementOrigin.GTEQ, IrStatementOrigin.LT, IrStatementOrigin.LTEQ -> {
                val (actualLeft, actualRight) = if (irCallLike.valueArgumentsCount == 1) {
                    infixLeft to infixRight
                } else {
                    irCallLike.valueArguments.map { it.accept(context).possiblyParenthesize() }.toPair()
                }

                return DartComparisonExpression(
                    left = actualLeft,
                    operator = when (origin) {
                        IrStatementOrigin.GT -> DartComparisonExpression.Operators.GREATER
                        IrStatementOrigin.LT -> DartComparisonExpression.Operators.LESS
                        IrStatementOrigin.GTEQ -> DartComparisonExpression.Operators.GREATER_OR_EQUAL
                        IrStatementOrigin.LTEQ -> DartComparisonExpression.Operators.LESS_OR_EQUAL
                        else -> throw UnsupportedOperationException()
                    },
                    right = actualRight,
                )
            }
            IrStatementOrigin.EQEQ ->
                DartEqualityExpression(
                    left = irCallLike.getValueArgument(0)!!.accept(context).possiblyParenthesize(),
                    right = irCallLike.getValueArgument(1)!!.accept(context).possiblyParenthesize(),
                )
            IrStatementOrigin.EQ ->
                DartAssignmentExpression(
                    left = DartPropertyAccessExpression(
                        target = infixLeft,
                        propertyName = (irCallLike.symbol.owner as IrSimpleFunction)
                            .correspondingProperty!!.dartNameAsSimple
                    ),
                    right = right,
                )
            else -> {


                val hasDartGetterAnnotation = irCallLike.symbol.owner.hasDartGetterAnnotation()

                when {
                    irCallLike.origin == IrStatementOrigin.EXCL && irLeft!!.type.isBoolean() -> {
                        DartNegatedExpression(left.possiblyParenthesize())
                    }
                    origin == IrStatementOrigin.GET_PROPERTY || origin == IrStatementOrigin.GET_LOCAL_PROPERTY
                            || hasDartGetterAnnotation -> {
                        val irSimpleFunction = irCallLike.symbol.owner as IrSimpleFunction
                        val propertyName = when {
                            hasDartGetterAnnotation -> irSimpleFunction.dartNameAsSimple
                            else -> irSimpleFunction.correspondingProperty!!.dartNameAsSimple
                        }

                        DartPropertyAccessExpression(infixLeft, propertyName)
                    }
                    else -> {
                        val arguments = irCallLike.accept(IrToDartArgumentListTransformer, context)

                        when (irCallLike) {
                            is IrConstructorCall, is IrEnumConstructorCall -> {
                                val type = irCallLike.type.accept(context) as DartNamedType
                                val name = irCallLike.symbol.owner.simpleDartNameOrNull

                                DartInstanceCreationExpression(
                                    type = type,
                                    constructorName = name,
                                    arguments = arguments,
                                    isConst = irCallLike.isDartConst(context)
                                )
                            }
                            else -> {
                                val functionName = irCallLike.symbol.owner.dartName

                                @Suppress("UnnecessaryVariable")
                                when (val receiver = optionalLeft) {
                                    null -> DartFunctionExpressionInvocation(
                                        function = functionName,
                                        arguments = arguments
                                    )
                                    else -> DartMethodInvocation(
                                        target = receiver.possiblyParenthesize(),
                                        methodName = functionName as DartSimpleIdentifier,
                                        arguments = arguments
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun <T> visitConst(irConst: IrConst<T>, data: DartTransformContext): DartExpression {
        return when (irConst.kind) {
            is IrConstKind.Null -> DartNullLiteral
            is IrConstKind.Boolean -> DartBooleanLiteral(irConst.value as Boolean)
            is IrConstKind.Byte -> DartIntegerLiteral(irConst.value as Byte)
            is IrConstKind.Short -> DartIntegerLiteral(irConst.value as Short)
            is IrConstKind.Int -> DartIntegerLiteral(irConst.value as Int)
            is IrConstKind.Long -> DartIntegerLiteral(irConst.value as Long)
            is IrConstKind.Float -> DartDoubleLiteral(irConst.value as Float)
            is IrConstKind.Double -> DartDoubleLiteral(irConst.value as Double)
            is IrConstKind.Char -> {
                val char = irConst.value as Char
                val charValue = char.code

                // If the code point is outside of ASCII range, the Dart string literal will be a
                // Unicode literal.
                val string = when {
                    charValue >= 128 || charValue == 0 -> "\\u{${charValue.toString(radix = 16)}}"
                    else -> char.toString()
                }

                DartSimpleStringLiteral(string)
            }
            is IrConstKind.String -> DartSimpleStringLiteral(
                value = irConst.value as String
                // TODO: isMultiline, isRaw
            )
            else -> todo(irConst)
        }
    }

    override fun visitWhen(irWhen: IrWhen, context: DartTransformContext) = irWhen.branches.reversed().toList().run {
        drop(1).fold(
            initial = first().let {
                require(it is IrElseBranch) { "Last branch in if/when expression must be else" }

                it.result.accept(context)
            },
            operation = { expression, irBranch ->
                DartConditionalExpression(
                    condition = irBranch.condition.accept(context),
                    thenExpression = irBranch.result.accept(context),
                    elseExpression = expression
                )
            }
        )
    }

    override fun visitGetValue(irGetValue: IrGetValue, context: DartTransformContext): DartExpression {
        if (irGetValue.isThisReference()) {
            return DartThisExpression
        }

        return irGetValue.symbol.owner.dartName
    }

    override fun visitGetField(irGetField: IrGetField, context: DartTransformContext): DartExpression {
        val receiver = irGetField.receiver?.accept(context) ?: irGetField.type.owner.dartName
        val name = irGetField.symbol.owner.relevantDartName

        return DartPropertyAccessExpression(
            target = receiver.possiblyParenthesize(),
            propertyName = name,
        )
    }

    override fun visitGetObjectValue(irGetObjectValue: IrGetObjectValue, data: DartTransformContext) =
        irGetObjectValue.symbol.owner.dartName

    override fun visitSetValue(irSetValue: IrSetValue, context: DartTransformContext): DartExpression {
        return DartAssignmentExpression(
            left = irSetValue.symbol.owner.dartName,
            right = irSetValue.value.accept(context)
        )
    }

    override fun visitSetField(irSetField: IrSetField, context: DartTransformContext): DartExpression {
        val receiver = irSetField.receiver?.accept(context)
        val name = irSetField.symbol.owner.relevantDartName

        val assignee = if (receiver != null)
            DartPropertyAccessExpression(
                target = receiver.possiblyParenthesize(),
                propertyName = name,
            )
        else
            name

        return DartAssignmentExpression(
            left = assignee,
            right = irSetField.value.accept(context)
        )
    }

    override fun visitTypeOperator(
        irTypeOperatorCall: IrTypeOperatorCall,
        context: DartTransformContext
    ): DartExpression {
        val expression = irTypeOperatorCall.argument.accept(context)
        val type = irTypeOperatorCall.typeOperand.accept(context)

        return when (val operator = irTypeOperatorCall.operator) {
            CAST, IMPLICIT_CAST -> DartAsExpression(expression, type)
            IMPLICIT_NOTNULL -> TODO()
            IMPLICIT_COERCION_TO_UNIT -> expression
            IMPLICIT_INTEGER_COERCION -> TODO()
            SAFE_CAST -> TODO()
            INSTANCEOF, NOT_INSTANCEOF -> DartIsExpression(expression, type, isNegated = operator == NOT_INSTANCEOF)
            SAM_CONVERSION -> TODO()
            IMPLICIT_DYNAMIC_CAST -> expression
            REINTERPRET_CAST -> TODO()
        }
    }

    override fun visitThrow(irThrow: IrThrow, context: DartTransformContext): DartExpression {
        // TODO: Rethrow

        return DartThrowExpression(irThrow.value.accept(context))
    }

    override fun visitVararg(irVararg: IrVararg, context: DartTransformContext): DartExpression {
        return DartListLiteral(
            elements = DartCollectionElementList(
                irVararg.elements.map {
                    when (it) {
                        is IrExpression -> it.accept(context)
                        else -> throw UnsupportedOperationException("Spread elements are not supported yet.")
                    }
                }
            ),
            typeArguments = DartTypeArgumentList(irVararg.varargElementType.accept(context))
        )
    }

    private val IrField.relevantDartName: DartSimpleIdentifier
        get() = when {
            !isExplicitBackingField -> correspondingProperty?.simpleDartName ?: dartName
            else -> dartName
        }

    override fun visitFunctionExpression(
        expression: IrFunctionExpression,
        context: DartTransformContext
    ): DartExpression =
        expression.function.transformBy(context) {
            DartFunctionExpression(
                typeParameters = typeParameters,
                parameters = parameters,
                body = expression.function.body.accept(context)
            )
        }

    private fun visitAnnotatedExpression(
        irAnnotated: IrAnnotatedExpression,
        context: DartTransformContext
    ): DartExpression =
        // Dart doesn't support annotated expressions, so the annotations are not outputted. But they are passed
        // down so annotations can still be checked in child expressions.
        context.withAnnotatedExpression(from = irAnnotated) {
            irAnnotated.expression.accept(it)
        }

    private fun visitNullAwareExpression(
        irNullAware: IrNullAwareExpression,
        context: DartTransformContext
    ): DartExpression = (irNullAware.expression.accept(context) as DartPossiblyNullAwareExpression).asNullAware()

    override fun visitStringConcatenation(
        expression: IrStringConcatenation,
        context: DartTransformContext
    ): DartExpression {
        return DartStringInterpolation(
            elements = expression.arguments.map {
                when {
                    it is IrConst<*> && it.kind == IrConstKind.String -> DartInterpolationString(it.value as String)
                    else -> DartInterpolationExpression(it.accept(context))
                }
            }
        )
    }

    private fun visitBinaryInfixExpression(
        binaryInfix: IrBinaryInfixExpression,
        context: DartTransformContext
    ): DartExpression {
        val left = binaryInfix.left.accept(context)
        val right = binaryInfix.right.accept(context)

        return when (binaryInfix) {
            is IrConjunctionExpression -> DartConjunctionExpression(left, right)
            is IrDisjunctionExpression -> DartDisjunctionExpression(left, right)
        }
    }

    private fun visitCode(
        irCode: IrDartCodeExpression,
        context: DartTransformContext
    ): DartExpression = DartCode(irCode.code)

    private fun DartExpression.possiblyParenthesize(): DartExpression = when (this) {
        is DartConditionalExpression, is DartAsExpression, is DartBinaryInfixExpression -> parenthesize()
        else -> this
    }
}

fun IrExpression.accept(context: DartTransformContext) = accept(IrToDartExpressionTransformer, context)
fun IrExpressionBody.accept(context: DartTransformContext) = accept(IrToDartExpressionTransformer, context)