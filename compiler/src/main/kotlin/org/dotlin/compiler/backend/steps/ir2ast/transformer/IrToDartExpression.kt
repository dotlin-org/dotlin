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

import org.dotlin.compiler.backend.attributes.CollectionLiteralKind.*
import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.hasDartGetterAnnotation
import org.dotlin.compiler.backend.isDartStatic
import org.dotlin.compiler.backend.steps.ir2ast.DartAstTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDotlinStatementOrigin.IF_NULL
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.ObjectLowering
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.createDartAssignment
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartInt
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartNumberPrimitive
import org.dotlin.compiler.backend.util.*
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
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartExpressionTransformer : IrDartAstTransformer<DartExpression>() {
    override fun DartAstTransformContext.visitExpressionBody(irBody: IrExpressionBody, data: DartAstTransformContext) =
        irBody.expression.accept(data)

    override fun DartAstTransformContext.visitFunctionAccess(
        irCallLike: IrFunctionAccessExpression,
        context: DartAstTransformContext,
    ): DartExpression {
        when (irCallLike.symbol) {
            dotlinIrBuiltIns.dartFun -> return visitDartCodeCall(irCallLike)
        }

        val irReceiver = irCallLike.extensionReceiver ?: irCallLike.dispatchReceiver
        val irSingleArgument by lazy { irCallLike.getValueArgument(0)!! }
        val optionalReceiver by lazy {
            when {
                irCallLike is IrCall && irCallLike.isSuperCall() -> DartSuperExpression
                else -> irReceiver.acceptAsReceiverOf(irCallLike, context)
            }
        }
        val receiver by lazy { optionalReceiver!!.maybeParenthesize(isReceiver = true) }
        val singleArgument by lazy { irSingleArgument.accept(context) }

        val optionalInfixReceiver by lazy { optionalReceiver?.maybeParenthesize(inBinaryInfix = true) }
        val infixReceiver by lazy { optionalInfixReceiver!! }
        val infixSingleArgument by lazy { singleArgument.maybeParenthesize(inBinaryInfix = true) }

        fun methodInvocation(methodName: DartSimpleIdentifier): DartMethodInvocation {
            return DartMethodInvocation(
                target = receiver,
                methodName = methodName,
                arguments = DartArgumentList(singleArgument),
                isNullAware = irCallLike.isNullSafe
            )
        }

        return when (val origin = irCallLike.origin) {
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
                        .mapNotNull { it?.accept(context)?.maybeParenthesize(inBinaryInfix = true) }
                        .toPair()
                }

                DartComparisonExpression(
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

                fun IrExpression.accept() = accept(context).maybeParenthesize(inEquals = true)

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
                singleArgument.maybeParenthesize(isReceiver = true)
            )

            UMINUS -> DartUnaryMinusExpression(
                receiver.maybeParenthesize(isReceiver = true)
            )

            ANDAND -> DartConjunctionExpression(infixReceiver, infixSingleArgument)
            OROR -> DartDisjunctionExpression(infixReceiver, infixSingleArgument)
            IF_NULL -> DartIfNullExpression(infixReceiver, infixSingleArgument)
            else -> {
                val irFunction = irCallLike.symbol.owner
                val hasDartGetterAnnotation = irFunction.hasDartGetterAnnotation()

                val primitiveNumberOperatorNames = listOf("shl", "shr", "ushr", "and", "or", "xor", "inv")
                val (shl, shr, ushr, and, or, xor, inv) = primitiveNumberOperatorNames

                when {
                    origin == EXCL && irReceiver!!.type.isBoolean() -> {
                        DartNegatedExpression(receiver.maybeParenthesize(isReceiver = true))
                    }

                    origin == GET_PROPERTY || origin == GET_LOCAL_PROPERTY
                            || hasDartGetterAnnotation -> {
                        val irSimpleFunction = irFunction as IrSimpleFunction
                        val irAccessed = when {
                            hasDartGetterAnnotation -> irSimpleFunction
                            else -> irSimpleFunction.correspondingProperty!!
                        }

                        when (optionalInfixReceiver) {
                            null -> irAccessed.dartName
                            else -> DartPropertyAccessExpression(
                                infixReceiver,
                                irAccessed.dartNameAsSimple,
                                isNullAware = irCallLike.isNullSafe
                            )
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
                                    propertyName,
                                    isNullAware = irCallLike.isNullSafe
                                )
                            },
                            right = singleArgument,
                        )
                    }

                    irCallLike.isTypeOfCall() -> DartTypeLiteral(
                        type = irCallLike.typeArguments.single()!!.accept(context)
                    )
                    // Some non-operator methods on primitive integers (Int, Long) are operators in Dart,
                    // such as `xor` or `ushr`.
                    irCallLike.symbol.owner.parentClassOrNull?.defaultType?.isDartInt() == true &&
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
                        val arguments = irCallLike.acceptArguments(context)

                        when {
                            irCallLike.isDartConstructorCall() -> {
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
                                val typeArguments = irCallLike.typeArguments.accept(context)

                                when (optionalReceiver) {
                                    null -> DartFunctionExpressionInvocation(
                                        function = functionName,
                                        arguments,
                                        typeArguments,
                                    )

                                    else -> DartMethodInvocation(
                                        target = receiver.maybeParenthesize(isReceiver = true),
                                        methodName = functionName as DartSimpleIdentifier,
                                        arguments,
                                        typeArguments,
                                        isNullAware = irCallLike.isNullSafe
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun DartAstTransformContext.visitConst(
        irConst: IrConst<*>,
        data: DartAstTransformContext
    ): DartExpression {
        return when (irConst.kind) {
            is IrConstKind.Null -> DartNullLiteral
            is IrConstKind.Boolean -> DartBooleanLiteral(irConst.value as Boolean)
            is IrConstKind.Byte -> DartIntegerLiteral(irConst.value as Byte)
            is IrConstKind.Short -> DartIntegerLiteral(irConst.value as Short)
            is IrConstKind.Int -> DartIntegerLiteral(irConst.value as Int)
            is IrConstKind.Long -> DartIntegerLiteral(irConst.value as Long)
            is IrConstKind.Float -> DartDoubleLiteral(irConst.value as Float)
            is IrConstKind.Double -> DartDoubleLiteral(irConst.value as Double)
            is IrConstKind.Char, is IrConstKind.String -> {
                val source = irConst.ktExpression
                val isTripleQuoted = (source as? KtStringTemplateExpression)?.isTripleQuoted() == true

                val irConstValue = irConst.value as String

                DartSimpleStringLiteral(
                    // If it's not a triple quoted string, it might contain characters such as '\n', which are parsed
                    // in irConstValue. We want them as-is in the source ('\n'). Thus, we grab the string value from
                    // the source in that case.
                    value = when {
                        isTripleQuoted -> irConstValue
                        else -> source?.text?.drop(1)?.dropLast(1) ?: irConstValue
                    },
                    // Always raw if triple quoted, because there's
                    // no string interpolation anyway.
                    isRaw = isTripleQuoted,
                    isTripleQuoted
                )
            }

            else -> todo(irConst)
        }
    }

    override fun DartAstTransformContext.visitWhen(irWhen: IrWhen, context: DartAstTransformContext) =
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

    override fun DartAstTransformContext.visitGetValue(
        irGetValue: IrGetValue,
        context: DartAstTransformContext
    ): DartExpression {
        if (irGetValue.isThisReference()) {
            return DartThisExpression
        }

        return irGetValue.symbol.owner.dartName
    }

    override fun DartAstTransformContext.visitGetField(
        irGetField: IrGetField,
        context: DartAstTransformContext
    ): DartExpression {
        val receiver = when (irGetField.symbol.owner.parent) {
            is IrFile -> null
            else -> irGetField.receiver.acceptAsReceiverOf(irGetField, context)
        }
        val name = relevantDartNameOf(irGetField.symbol.owner)

        return when (receiver) {
            null -> name
            else -> DartPropertyAccessExpression(
                target = receiver.maybeParenthesize(isReceiver = true),
                propertyName = name,
                isNullAware = false
            )
        }
    }

    override fun DartAstTransformContext.visitGetObjectValue(
        irGetObjectValue: IrGetObjectValue,
        data: DartAstTransformContext
    ) = DartPropertyAccessExpression(
        target = irGetObjectValue.symbol.owner.dartName,
        propertyName = ObjectLowering.INSTANCE_FIELD_NAME.toDartIdentifier(),
        isNullAware = false
    )

    override fun DartAstTransformContext.visitGetEnumValue(
        irGetEnumValue: IrGetEnumValue,
        context: DartAstTransformContext
    ) = DartPropertyAccessExpression(
        target = irGetEnumValue.symbol.owner.parentAsClass.dartName,
        propertyName = irGetEnumValue.symbol.owner.dartNameAsSimple,
        isNullAware = false
    )

    override fun DartAstTransformContext.visitSetValue(
        irSetValue: IrSetValue,
        context: DartAstTransformContext
    ): DartExpression {
        return createDartAssignment(
            irSetValue.origin,
            receiver = irSetValue.symbol.owner.dartName,
            irReceiverType = irSetValue.symbol.owner.type,
            irValue = irSetValue.value
        )
    }

    override fun DartAstTransformContext.visitSetField(
        irSetField: IrSetField,
        context: DartAstTransformContext
    ): DartExpression {
        val receiver = irSetField.receiver.acceptAsReceiverOf(irSetField, context)
        val name = relevantDartNameOf(irSetField.symbol.owner)
        val assignee = if (receiver != null)
            DartPropertyAccessExpression(
                target = receiver.maybeParenthesize(isReceiver = true),
                propertyName = name,
                isNullAware = false
            )
        else
            name

        return DartAssignmentExpression(
            left = assignee,
            right = irSetField.value.accept(context)
        )
    }

    override fun DartAstTransformContext.visitTypeOperator(
        irTypeOperatorCall: IrTypeOperatorCall,
        context: DartAstTransformContext
    ): DartExpression {
        val expression = irTypeOperatorCall.argument.accept(context)
        val type = irTypeOperatorCall.typeOperand.accept(
            context,
            useFunctionInterface = irTypeOperatorCall.isFunctionTypeCheck
        )

        fun DartExpression.parenthesize() = maybeParenthesize(isReceiver = true, inBinaryInfix = true)

        return when (val operator = irTypeOperatorCall.operator) {
            CAST, IMPLICIT_CAST, IMPLICIT_DYNAMIC_CAST -> DartAsExpression(expression.parenthesize(), type)
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
            REINTERPRET_CAST -> TODO()
        }
    }

    override fun DartAstTransformContext.visitThrow(
        irThrow: IrThrow,
        context: DartAstTransformContext
    ): DartExpression {
        // TODO: Rethrow

        return DartThrowExpression(irThrow.value.accept(context))
    }

    override fun DartAstTransformContext.visitVararg(
        irVararg: IrVararg,
        context: DartAstTransformContext
    ): DartExpression {
        val elements = DartCollectionElementList(
            irVararg.elements.map {
                when (it) {
                    is IrExpression -> it.accept(context)
                    else -> throw UnsupportedOperationException("Spread elements are not supported yet.")
                }
            }
        )

        val typeArguments = DartTypeArgumentList(irVararg.varargElementType.accept(context))
        val isConst = irVararg.isDartConst()

        return when (irVararg.literalKind) {
            LIST -> DartListLiteral(elements, isConst, typeArguments)
            SET -> DartSetLiteral(elements, isConst, typeArguments)
            MAP -> DartMapLiteral(elements, isConst, typeArguments)
        }
    }

    override fun DartAstTransformContext.visitFunctionExpression(
        expression: IrFunctionExpression,
        context: DartAstTransformContext
    ): DartExpression = expression.function.let {
        DartFunctionExpression(
            typeParameters = it.typeParameters.accept(context),
            parameters = it.valueParameters.accept(context),
            body = it.body.accept(context)
        )
    }

    override fun DartAstTransformContext.visitFunctionReference(
        expression: IrFunctionReference,
        context: DartAstTransformContext
    ): DartExpression = expression.let {
        val irFunction = it.symbol.owner
        when (val receiver = it.receiver) {
            null -> irFunction.dartName
            else -> DartPropertyAccessExpression(
                target = receiver.accept(context),
                propertyName = irFunction.dartNameAsSimple,
                isNullAware = expression.isNullSafe
            )
        }
    }

    override fun DartAstTransformContext.visitStringConcatenation(
        expression: IrStringConcatenation,
        context: DartAstTransformContext
    ): DartExpression {
        val isTripledQuoted = (expression.ktExpression as? KtStringTemplateExpression)?.isTripleQuoted() == true

        val expressions = expression.arguments.map {
            when (val exp = it.accept(context)) {
                //
                is DartSimpleStringLiteral -> exp.copy(isTripleQuoted = true)
                else -> exp
            }
        }

        return when {
            // For triple quoted string literals, we make it a string concatenation. This is necessary, because
            // Dart triple quoted strings cannot have interpolation (`"$x"`) in them. But, we still need to
            // use triple quotes in Dart because special characters (`\n`) should stay literal as-is, and
            // new lines should be preserved.
            isTripledQuoted -> expressions
                .map {
                    when (it) {
                        // We must make any string literals triple quoted and raw, because that enables similar
                        // features as with Kotlin triple quoted strings: No escaped characters and the literal
                        // can go over multiple lines.
                        is DartSimpleStringLiteral -> it.copy(isTripleQuoted = true, isRaw = true)
                        else -> it
                    }
                }
                .reduceRight { value, acc -> DartPlusExpression(value, acc) }

            else -> DartStringInterpolation(
                elements = expressions.map {
                    when (it) {
                        is DartSimpleStringLiteral -> DartInterpolationString(it.value)
                        else -> DartInterpolationExpression(it)
                    }
                },
            )
        }
    }

    private fun DartExpression.maybeParenthesize(
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

    override fun DartAstTransformContext.visitBlock(expression: IrBlock, context: DartAstTransformContext) =
        when (val origin = expression.origin) {
            PREFIX_INCR, PREFIX_DECR -> {
                val irSetValue = expression.statements.first() as IrSetValue
                val variable = irSetValue.symbol.owner.dartName

                when {
                    expression.type.isDartNumberPrimitive() -> when (origin) {
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
                require(expression.type.isDartNumberPrimitive())
                val receiver = (expression.statements.first() as IrVariable).initializer!!.accept(context)

                when (origin) {
                    POSTFIX_INCR -> DartPostfixIncrementExpression(receiver)
                    else -> DartPostfixDecrementExpression(receiver)
                }
            }

            else -> todo(expression)
        }

    private fun visitDartCodeCall(irCallLike: IrFunctionAccessExpression): DartExpression {
        val code = (irCallLike.valueArguments[0] as IrConst<*>).value as String

        return DartCode(code.trimIndent())
    }

    private fun DartAstTransformContext.relevantDartNameOf(irField: IrField): DartSimpleIdentifier = irField.let {
        when {
            !it.isExplicitBackingField -> it.correspondingProperty?.simpleDartName ?: it.dartName
            else -> it.dartName
        }
    }
}

fun IrExpression.accept(context: DartAstTransformContext) = accept(IrToDartExpressionTransformer, context).let {
    val exp = this
    with(context) {
        when {
            exp.isParenthesized -> it.parenthesize()
            else -> it
        }
    }
}

fun IrExpressionBody.accept(context: DartAstTransformContext) = accept(IrToDartExpressionTransformer, context)

private fun IrExpression?.acceptAsReceiverOf(of: IrExpression, context: DartAstTransformContext) =
    context.runWith(this) accept@{
        when (of) {
            is IrDeclarationReference -> {
                val owner = of.symbol.owner
                when (it) {
                    null, is IrGetObjectValue -> when (owner) {
                        is IrDeclaration -> {
                            val parentClass = owner.parentClassOrNull
                            if (parentClass != null && (owner.isDartStatic || parentClass.isEnumClass)) {
                                return@accept when {
                                    parentClass.isCompanion -> parentClass.parentClassOrNull!!.dartName
                                    else -> parentClass.dartName
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
        origin != IrDotlinStatementOrigin.OPERATOR_REDIRECT &&
                (it.name == Name.identifier(name) ||
                        (it.name == Name.identifier(syntheticName) && it.valueParameters.size == parametersCount))
                && it.isOperator
    }
}

private fun IrFunctionAccessExpression.isDartIndexedGet() = isDartIndexed(get = true)
private fun IrFunctionAccessExpression.isDartIndexedSet() = isDartIndexed(get = false)

private fun IrFunctionAccessExpression.isSetOperator() =
    (symbol.owner as IrSimpleFunction?)?.let {
        (it.isOperator || it.origin == IrDotlinDeclarationOrigin.WAS_OPERATOR) &&
                it.name == Name.identifier("set")
    } == true

private fun IrFunctionAccessExpression.isTypeOfCall() = symbol.owner.fqNameWhenAvailable == dotlin.typeOf