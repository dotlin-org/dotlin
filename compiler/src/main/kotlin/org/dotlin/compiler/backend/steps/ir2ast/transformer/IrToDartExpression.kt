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

import org.dotlin.compiler.backend.hasDartGetterAnnotation
import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.ObjectLowering
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.*
import org.dotlin.compiler.backend.util.toPair
import org.dotlin.compiler.dart.ast.collection.DartCollectionElementList
import org.dotlin.compiler.dart.ast.expression.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.toDartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.invocation.DartFunctionExpressionInvocation
import org.dotlin.compiler.dart.ast.expression.invocation.DartMethodInvocation
import org.dotlin.compiler.dart.ast.expression.literal.*
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.*
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isChar
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isString

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartExpressionTransformer : IrDartAstTransformer<DartExpression> {
    override fun visitExpressionBody(irBody: IrExpressionBody, data: DartTransformContext) =
        irBody.expression.accept(data)

    override fun visitFunctionAccess(
        irCallLike: IrFunctionAccessExpression,
        context: DartTransformContext,
    ): DartExpression {
        val irReceiver = irCallLike.extensionReceiver ?: irCallLike.dispatchReceiver
        val irSingleArgument by lazy { irCallLike.getValueArgument(0)!! }
        val optionalReceiver by lazy {
            when {
                irCallLike is IrCall && irCallLike.isSuperCall() -> DartSuperExpression
                else -> irReceiver?.accept(context)
            }
        }
        val receiver by lazy { optionalReceiver!! }
        val singleArgument by lazy { irSingleArgument.accept(context) }

        val infixReceiver by lazy { receiver.possiblyParenthesize(inBinaryInfix = true) }
        val infixSingleArgument by lazy { singleArgument.possiblyParenthesize(inBinaryInfix = true) }

        fun methodInvocation(methodName: DartSimpleIdentifier): DartMethodInvocation {
            return DartMethodInvocation(
                target = receiver,
                methodName = methodName,
                arguments = DartArgumentList(singleArgument)
            )
        }

        return when (val origin = irCallLike.origin) {
            IrStatementOrigin.PLUS -> {
                val irLeftType = irReceiver!!.type
                val irRightType = irSingleArgument.type

                when {
                    // Built-in: String + Any? (or null) will use the Dart extension.
                    irLeftType.isString() && !irRightType.isString() -> {
                        methodInvocation(irCallLike.symbol.owner.dartNameAsSimple)
                    }
                    // Built-in: Char + Int will use the Dart extension.
                    irLeftType.isChar() && irRightType.isInt() -> {
                        methodInvocation(irCallLike.symbol.owner.dartNameAsSimple)
                    }
                    else -> DartPlusExpression(infixReceiver, infixSingleArgument)
                }
            }
            IrStatementOrigin.MINUS -> when {
                // Built-in: String - int will use the Dart extension.
                irReceiver!!.type.isChar() && irSingleArgument.type.isInt() -> {
                    methodInvocation(irCallLike.symbol.owner.dartNameAsSimple)
                }
                else -> DartMinusExpression(infixReceiver, infixSingleArgument)
            }
            IrStatementOrigin.MUL -> DartMultiplyExpression(infixReceiver, infixSingleArgument)
            IrStatementOrigin.DIV -> {
                when {
                    // Dart's int divide operator returns a double, while Kotlin's Int divide operator returns an
                    // Int. So, we use the ~/ Dart operator, which returns an int.
                    irReceiver!!.type.isDartInt() && irSingleArgument.type.isDartInt() -> DartIntegerDivideExpression(
                        infixReceiver,
                        infixSingleArgument
                    )
                    else -> DartDivideExpression(infixReceiver, infixSingleArgument)
                }
            }
            IrStatementOrigin.PERC -> DartModuloExpression(infixReceiver, infixSingleArgument)
            IrStatementOrigin.GT, IrStatementOrigin.GTEQ, IrStatementOrigin.LT, IrStatementOrigin.LTEQ -> {
                val (actualLeft, actualRight) = if (irCallLike.valueArgumentsCount == 1) {
                    infixReceiver to infixSingleArgument
                } else {
                    irCallLike.valueArguments
                        .mapNotNull { it?.accept(context)?.possiblyParenthesize(inBinaryInfix = true) }
                        .toPair()
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
            IrStatementOrigin.EQEQ, IrStatementOrigin.EXCLEQ -> {
                val isNegated = origin == IrStatementOrigin.EXCLEQ

                fun IrExpression.accept() = accept(context).possiblyParenthesize(inEquals = true)

                val (left, right) = when {
                    isNegated -> irReceiver as IrCall
                    else -> irCallLike
                }.let {
                    it.getValueArgument(0)!!.accept() to it.getValueArgument(1)!!.accept()
                }

                when {
                    isNegated -> DartNotEqualsExpression(left, right)
                    else -> DartEqualsExpression(left, right)
                }
            }
            IrStatementOrigin.EQ ->
                DartAssignmentExpression(
                    left = DartPropertyAccessExpression(
                        target = infixReceiver,
                        propertyName = (irCallLike.symbol.owner as IrSimpleFunction)
                            .correspondingProperty!!.dartNameAsSimple
                    ),
                    right = singleArgument,
                )
            IrStatementOrigin.EXCLEXCL -> DartNotNullAssertionExpression(
                singleArgument.possiblyParenthesize(isReceiver = true)
            )
            IrStatementOrigin.UMINUS -> DartUnaryMinusExpression(
                receiver.possiblyParenthesize(isReceiver = true)
            )
            else -> {
                val hasDartGetterAnnotation = irCallLike.symbol.owner.hasDartGetterAnnotation()

                when {
                    irCallLike.origin == IrStatementOrigin.EXCL && irReceiver!!.type.isBoolean() -> {
                        DartNegatedExpression(receiver.possiblyParenthesize(isReceiver = true))
                    }
                    origin == IrStatementOrigin.GET_PROPERTY || origin == IrStatementOrigin.GET_LOCAL_PROPERTY
                            || hasDartGetterAnnotation -> {
                        val irSimpleFunction = irCallLike.symbol.owner as IrSimpleFunction
                        val irAccessed = when {
                            hasDartGetterAnnotation -> irSimpleFunction
                            else -> irSimpleFunction.correspondingProperty!!
                        }

                        when (irReceiver) {
                            null -> irAccessed.dartName
                            else -> DartPropertyAccessExpression(infixReceiver, irAccessed.dartNameAsSimple)
                        }
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
                                when (val receiver = optionalReceiver) {
                                    null -> DartFunctionExpressionInvocation(
                                        function = functionName,
                                        arguments = arguments
                                    )
                                    else -> DartMethodInvocation(
                                        target = receiver.possiblyParenthesize(isReceiver = true),
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
        val receiver = when (irGetField.symbol.owner.parent) {
            is IrFile -> null
            else -> irGetField.receiver?.accept(context) ?: irGetField.type.owner.dartName
        }
        val name = irGetField.symbol.owner.relevantDartName

        return when (receiver) {
            null -> name
            else -> DartPropertyAccessExpression(
                target = receiver.possiblyParenthesize(isReceiver = true),
                propertyName = name,
            )
        }
    }

    override fun visitGetObjectValue(irGetObjectValue: IrGetObjectValue, data: DartTransformContext) =
        DartPropertyAccessExpression(
            target = irGetObjectValue.symbol.owner.dartName,
            propertyName = ObjectLowering.INSTANCE_FIELD_NAME.toDartSimpleIdentifier()
        )

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
                target = receiver.possiblyParenthesize(isReceiver = true),
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

        fun DartExpression.parenthesize() = possiblyParenthesize(isReceiver = true, inBinaryInfix = true)

        return when (val operator = irTypeOperatorCall.operator) {
            CAST, IMPLICIT_CAST -> DartAsExpression(expression.parenthesize(), type)
            IMPLICIT_NOTNULL -> TODO()
            IMPLICIT_COERCION_TO_UNIT -> expression
            IMPLICIT_INTEGER_COERCION -> TODO()
            SAFE_CAST -> TODO()
            INSTANCEOF, NOT_INSTANCEOF -> DartIsExpression(
                expression.parenthesize(),
                type,
                isNegated = operator == NOT_INSTANCEOF
            )
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

    override fun visitAnnotatedExpression(
        irAnnotated: IrAnnotatedExpression,
        context: DartTransformContext
    ): DartExpression =
        // Dart doesn't support annotated expressions, so the annotations are not outputted. But they are passed
        // down so annotations can still be checked in child expressions.
        context.withAnnotatedExpression(from = irAnnotated) {
            irAnnotated.expression.accept(context)
        }

    override fun visitNullAwareExpression(
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

    override fun visitBinaryInfixExpression(
        binaryInfix: IrBinaryInfixExpression,
        context: DartTransformContext
    ): DartExpression {
        val left = binaryInfix.left.accept(context).possiblyParenthesize(inBinaryInfix = true)
        val right = binaryInfix.right.accept(context).possiblyParenthesize(inBinaryInfix = true)

        return when (binaryInfix) {
            is IrConjunctionExpression -> DartConjunctionExpression(left, right)
            is IrDisjunctionExpression -> DartDisjunctionExpression(left, right)
            is IrIfNullExpression -> DartIfNullExpression(left, right)
        }
    }

    override fun visitDartCodeExpression(
        irCode: IrDartCodeExpression,
        context: DartTransformContext
    ): DartExpression = DartCode(irCode.code)

    private fun DartExpression.possiblyParenthesize(
        isReceiver: Boolean = false,
        inEquals: Boolean = false,
        inBinaryInfix: Boolean = inEquals,
    ): DartExpression {
        require(listOf(isReceiver, inEquals, inBinaryInfix).any { it == true }) { "One parameter must be true" }

        return when {
            isReceiver || inBinaryInfix -> when (this) {
                is DartConditionalExpression, is DartAsExpression, is DartThrowExpression -> parenthesize()
                else -> when {
                    inEquals -> when (this) {
                        is DartEqualsExpression -> parenthesize()
                        else -> this
                    }
                    isReceiver -> when (this) {
                        is DartBinaryInfixExpression -> parenthesize()
                        else -> this
                    }
                    else -> this
                }
            }
            else -> this
        }
    }
}

fun IrExpression.accept(context: DartTransformContext) = accept(IrToDartExpressionTransformer, context)
fun IrExpressionBody.accept(context: DartTransformContext) = accept(IrToDartExpressionTransformer, context)