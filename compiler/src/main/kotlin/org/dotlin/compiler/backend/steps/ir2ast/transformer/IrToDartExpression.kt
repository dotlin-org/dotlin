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
import org.dotlin.compiler.backend.isDartStatic
import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.ObjectLowering
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.createDartAssignment
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartInt
import org.dotlin.compiler.backend.util.component6
import org.dotlin.compiler.backend.util.component7
import org.dotlin.compiler.backend.util.runWith
import org.dotlin.compiler.backend.util.toPair
import org.dotlin.compiler.dart.ast.collection.DartCollectionElementList
import org.dotlin.compiler.dart.ast.expression.*
import org.dotlin.compiler.dart.ast.expression.DartAssignmentOperator.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.toDartIdentifier
import org.dotlin.compiler.dart.ast.expression.invocation.DartFunctionExpressionInvocation
import org.dotlin.compiler.dart.ast.expression.invocation.DartMethodInvocation
import org.dotlin.compiler.dart.ast.expression.literal.*
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.*
import org.jetbrains.kotlin.ir.types.isBoolean
import org.jetbrains.kotlin.ir.types.isChar
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.Name

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartExpressionTransformer : IrDartAstTransformer<DartExpression>() {
    override fun DartTransformContext.visitExpressionBody(irBody: IrExpressionBody, data: DartTransformContext) =
        irBody.expression.accept(data)

    override fun DartTransformContext.visitFunctionAccess(
        irCallLike: IrFunctionAccessExpression,
        context: DartTransformContext,
    ) = context.run {
        val irReceiver = irCallLike.extensionReceiver ?: irCallLike.dispatchReceiver
        val irSingleArgument by lazy { irCallLike.getValueArgument(0)!! }
        val optionalReceiver by lazy {
            when {
                irCallLike is IrCall && irCallLike.isSuperCall() -> DartSuperExpression
                else -> irReceiver.acceptAsReceiverOf(irCallLike, context)
            }
        }
        val receiver by lazy { optionalReceiver!! }
        val singleArgument by lazy { irSingleArgument.accept(context) }

        val optionalInfixReceiver by lazy { optionalReceiver?.possiblyParenthesize(inBinaryInfix = true) }
        val infixReceiver by lazy { optionalInfixReceiver!! }
        val infixSingleArgument by lazy { singleArgument.possiblyParenthesize(inBinaryInfix = true) }

        fun methodInvocation(methodName: DartSimpleIdentifier): DartMethodInvocation {
            return DartMethodInvocation(
                target = receiver,
                methodName = methodName,
                arguments = DartArgumentList(singleArgument)
            )
        }

        when (val origin = irCallLike.origin) {
            PLUS -> {
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
            MINUS -> when {
                // Built-in: String - int will use the Dart extension.
                irReceiver!!.type.isChar() && irSingleArgument.type.isInt() -> {
                    methodInvocation(irCallLike.symbol.owner.dartNameAsSimple)
                }
                else -> DartMinusExpression(infixReceiver, infixSingleArgument)
            }
            MUL -> DartMultiplyExpression(infixReceiver, infixSingleArgument)
            DIV -> {
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
            PERC -> DartModuloExpression(infixReceiver, infixSingleArgument)
            GT, GTEQ, LT, LTEQ -> {
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
                        GT -> DartComparisonExpression.Operators.GREATER
                        LT -> DartComparisonExpression.Operators.LESS
                        GTEQ -> DartComparisonExpression.Operators.GREATER_OR_EQUAL
                        LTEQ -> DartComparisonExpression.Operators.LESS_OR_EQUAL
                        else -> throw UnsupportedOperationException()
                    },
                    right = actualRight,
                )
            }
            EQEQ, EXCLEQ -> {
                val isNegated = origin == EXCLEQ

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
            EXCLEXCL -> DartNotNullAssertionExpression(
                singleArgument.possiblyParenthesize(isReceiver = true)
            )
            UMINUS -> DartUnaryMinusExpression(
                receiver.possiblyParenthesize(isReceiver = true)
            )
            else -> {
                val irFunction = irCallLike.symbol.owner
                val hasDartGetterAnnotation = irFunction.hasDartGetterAnnotation()

                val primitiveNumberOperatorNames = listOf("shl", "shr", "ushr", "and", "or", "xor", "inv")
                val (shl, shr, ushr, and, or, xor, inv) = primitiveNumberOperatorNames

                when {
                    origin == EXCL && irReceiver!!.type.isBoolean() -> {
                        DartNegatedExpression(receiver.possiblyParenthesize(isReceiver = true))
                    }
                    origin == GET_PROPERTY || origin == GET_LOCAL_PROPERTY
                            || hasDartGetterAnnotation -> {
                        val irSimpleFunction = irFunction as IrSimpleFunction
                        val irAccessed = when {
                            hasDartGetterAnnotation -> irSimpleFunction
                            else -> irSimpleFunction.correspondingProperty!!
                        }

                        when (irReceiver) {
                            null -> irAccessed.dartName
                            else -> DartPropertyAccessExpression(infixReceiver, irAccessed.dartNameAsSimple)
                        }
                    }
                    irCallLike.isDartIndexedGet() -> DartIndexExpression(receiver, singleArgument)
                    irCallLike.isDartIndexedSet() -> DartAssignmentExpression(
                        left = DartIndexExpression(
                            target = receiver,
                            index = irCallLike.valueArguments.first()!!.accept(context)
                        ),
                        right = irCallLike.valueArguments.last()!!.accept(context)
                    )
                    origin == EQ && !irCallLike.isSetOperator() -> run {
                        val propertyName = (irCallLike.symbol.owner as IrSimpleFunction).dartNameAsSimple

                        DartAssignmentExpression(
                            left = when (optionalInfixReceiver) {
                                null -> propertyName
                                else -> DartPropertyAccessExpression(
                                    target = infixReceiver,
                                    propertyName
                                )
                            },
                            right = singleArgument,
                        )
                    }
                    // Some non-operator methods on primitive integers (Int, Long) are operators in Dart,
                    // such as `xor` or `ushr`.
                    irCallLike.symbol.owner.parentClassOrNull?.defaultType?.isPrimitiveInteger() == true &&
                            irFunction.name.identifier in primitiveNumberOperatorNames -> {
                        val left by lazy { infixReceiver }
                        val right by lazy { infixSingleArgument }

                        when (irFunction.name.identifier) {
                            shl -> DartBitwiseShiftLeftExpression(left, right)
                            shr -> DartBitwiseShiftRightExpression(left, right)
                            ushr -> DartBitwiseUnsignedShiftRightExpression(left, right)
                            and -> DartBitwiseAndExpression(left, right)
                            or -> DartBitwiseOrExpression(left, right)
                            xor -> DartBitwiseExclusiveOrExpression(left, right)
                            inv -> DartBitwiseNegationExpression(left)
                            else -> throw IllegalStateException("Impossible identifier")
                        }
                    }
                    else -> {
                        val arguments = irCallLike.accept(IrToDartArgumentListTransformer, context)

                        when (irCallLike) {
                            is IrConstructorCall, is IrEnumConstructorCall -> {
                                val type = irCallLike.type.accept(context, isConstructorType = true) as DartNamedType
                                val name = irCallLike.symbol.owner.simpleDartNameOrNull

                                DartInstanceCreationExpression(
                                    type = type,
                                    constructorName = name,
                                    arguments = arguments,
                                    isConst = irCallLike.isDartConst()
                                )
                            }
                            else -> {
                                val functionName = irCallLike.symbol.owner.dartName

                                when (optionalReceiver) {
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

    override fun <T> DartTransformContext.visitConst(irConst: IrConst<T>, data: DartTransformContext): DartExpression {
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

    override fun DartTransformContext.visitWhen(irWhen: IrWhen, context: DartTransformContext) =
        irWhen.branches.reversed().toList().run {
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

    override fun DartTransformContext.visitGetValue(
        irGetValue: IrGetValue,
        context: DartTransformContext
    ): DartExpression {
        if (irGetValue.isThisReference()) {
            return DartThisExpression
        }

        return irGetValue.symbol.owner.dartName
    }

    override fun DartTransformContext.visitGetField(
        irGetField: IrGetField,
        context: DartTransformContext
    ): DartExpression {
        val receiver = when (irGetField.symbol.owner.parent) {
            is IrFile -> null
            else -> irGetField.receiver.acceptAsReceiverOf(irGetField, context)
        }
        val name = relevantDartNameOf(irGetField.symbol.owner)

        return when (receiver) {
            null -> name
            else -> DartPropertyAccessExpression(
                target = receiver.possiblyParenthesize(isReceiver = true),
                propertyName = name,
            )
        }
    }

    override fun DartTransformContext.visitGetObjectValue(
        irGetObjectValue: IrGetObjectValue,
        data: DartTransformContext
    ) = DartPropertyAccessExpression(
        target = irGetObjectValue.symbol.owner.dartName,
        propertyName = ObjectLowering.INSTANCE_FIELD_NAME.toDartIdentifier()
    )

    override fun DartTransformContext.visitSetValue(
        irSetValue: IrSetValue,
        context: DartTransformContext
    ): DartExpression {
        return createDartAssignment(
            irSetValue.origin,
            receiver = irSetValue.symbol.owner.dartName,
            irReceiverType = irSetValue.symbol.owner.type,
            irValue = irSetValue.value
        )
    }

    override fun DartTransformContext.visitSetField(
        irSetField: IrSetField,
        context: DartTransformContext
    ): DartExpression {
        val receiver = irSetField.receiver.acceptAsReceiverOf(irSetField, context)
        val name = relevantDartNameOf(irSetField.symbol.owner)
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

    override fun DartTransformContext.visitTypeOperator(
        irTypeOperatorCall: IrTypeOperatorCall,
        context: DartTransformContext
    ): DartExpression {
        val expression = irTypeOperatorCall.argument.accept(context)
        val type = irTypeOperatorCall.typeOperand.accept(
            context,
            useFunctionInterface = irTypeOperatorCall.isFunctionTypeCheck
        )

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

    override fun DartTransformContext.visitThrow(irThrow: IrThrow, context: DartTransformContext): DartExpression {
        // TODO: Rethrow

        return DartThrowExpression(irThrow.value.accept(context))
    }

    override fun DartTransformContext.visitVararg(irVararg: IrVararg, context: DartTransformContext): DartExpression {
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

    override fun DartTransformContext.visitFunctionExpression(
        expression: IrFunctionExpression,
        context: DartTransformContext
    ): DartExpression = expression.function.let {
        DartFunctionExpression(
            typeParameters = it.typeParameters.accept(context),
            parameters = it.valueParameters.accept(context),
            body = it.body.accept(context)
        )
    }

    override fun DartTransformContext.visitFunctionReference(
        expression: IrFunctionReference,
        context: DartTransformContext
    ): DartExpression = expression.let {
        val irFunction = it.symbol.owner
        when (val receiver = it.receiver) {
            null -> irFunction.dartName
            else -> DartPropertyAccessExpression(
                target = receiver.accept(context),
                propertyName = irFunction.dartNameAsSimple
            )
        }
    }

    override fun DartTransformContext.visitNullAwareExpression(
        irNullAware: IrNullAwareExpression,
        context: DartTransformContext
    ): DartExpression = (irNullAware.expression.accept(context) as DartPossiblyNullAwareExpression).asNullAware()

    override fun DartTransformContext.visitStringConcatenation(
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

    override fun DartTransformContext.visitBinaryInfixExpression(
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

    override fun DartTransformContext.visitDartCodeExpression(
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

    override fun DartTransformContext.visitBlock(expression: IrBlock, context: DartTransformContext) =
        when (val origin = expression.origin) {
            PREFIX_INCR, PREFIX_DECR -> {
                val irSetValue = expression.statements.first() as IrSetValue
                val variable = irSetValue.symbol.owner.dartName

                when {
                    expression.type.isPrimitiveNumber() -> when (origin) {
                        PREFIX_INCR -> DartPrefixIncrementExpression(variable)
                        else -> DartPrefixDecrementExpression(variable)
                    }
                    // If it's not called on a primitive number, we invoke inc() or dec() directly.
                    else -> DartAssignmentExpression(
                        left = variable,
                        right = irSetValue.value.accept(context)
                    )
                }
            }
            POSTFIX_INCR, POSTFIX_DECR -> {
                // Non-primitive number types are handled in a lowering.
                require(expression.type.isPrimitiveNumber())
                val receiver = (expression.statements.first() as IrVariable).initializer!!.accept(context)

                when (origin) {
                    POSTFIX_INCR -> DartPostfixIncrementExpression(receiver)
                    else -> DartPostfixDecrementExpression(receiver)
                }
            }
            else -> todo(expression)
        }

    private fun DartTransformContext.relevantDartNameOf(irField: IrField): DartSimpleIdentifier = irField.let {
        when {
            !it.isExplicitBackingField -> it.correspondingProperty?.simpleDartName ?: it.dartName
            else -> it.dartName
        }
    }
}

fun IrExpression.accept(context: DartTransformContext) = accept(IrToDartExpressionTransformer, context).let {
    val exp = this
    with(context) {
        when {
            exp.isParenthesized -> it.parenthesize()
            else -> it
        }
    }
}

fun IrExpressionBody.accept(context: DartTransformContext) = accept(IrToDartExpressionTransformer, context)

private fun IrExpression?.acceptAsReceiverOf(of: IrExpression, context: DartTransformContext) =
    context.runWith(this) accept@{
        when (of) {
            is IrDeclarationReference -> {
                val owner = of.symbol.owner
                when (it) {
                    null, is IrGetObjectValue -> when (owner) {
                        is IrDeclaration -> {
                            val parentObj = owner.parentClassOrNull
                            if (parentObj != null && (owner.isDartStatic || parentObj.isEnumClass)) {
                                return@accept when {
                                    parentObj.isCompanion -> parentObj.parentClassOrNull!!.dartName
                                    else -> parentObj.dartName
                                }
                            }
                        }
                    }
                }
            }
        }

        it?.accept(context)
    }

private fun IrFunctionAccessExpression.isDartIndexed(get: Boolean): Boolean {
    val (name, syntheticName, parametersCount) = when (get) {
        true -> Triple("get", "[]", 1)
        false -> Triple("set", "[]=", 2)
    }

    val owner = symbol.owner as? IrSimpleFunction ?: return false

    return owner.let {
        origin != IrDartStatementOrigin.OPERATOR_REDIRECT &&
                (it.name == Name.identifier(name) ||
                        (it.name == Name.identifier(syntheticName) && it.valueParameters.size == parametersCount))
                && it.isOperator
    }
}

private fun IrFunctionAccessExpression.isDartIndexedGet() = isDartIndexed(get = true)
private fun IrFunctionAccessExpression.isDartIndexedSet() = isDartIndexed(get = false)

private fun IrFunctionAccessExpression.isSetOperator() =
    (symbol.owner as IrSimpleFunction?)?.let {
        (it.isOperator || it.origin == IrDartDeclarationOrigin.WAS_OPERATOR) &&
                it.name == Name.identifier("set")
    } == true