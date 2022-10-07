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
import org.dotlin.compiler.backend.util.isAccessibleInDartConstLambda
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildProperty
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

/**
 * Convert [IrPropertyReference]s into instances of `KProperty0Impl`, `KProperty1Impl`, etc.
 */
class PropertyReferencesLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(
        reference: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? {
        if (reference !is IrPropertyReference) return noChange()

        val property = reference.symbol.owner
        val propertyContainer = property.containerParent ?: return noChange()

        val receiver1 = reference.receiver ?: when (propertyContainer) {
            // It's possible that the receiver is null when the receiver should've been `this`.
            is IrClass -> buildStatement(propertyContainer.symbol) {
                irGet(propertyContainer.thisReceiver!!)
            }
            else -> null
        }
        val receiver2: IrExpression? = null // TODO

        val kPropertyConstructorCall by lazy {
            createKPropertyConstructorCall(
                context,
                containerSymbol = context.container.symbol,
                propertyReference = reference,
                propertyGetter = reference.getter,
                propertySetter = reference.setter,
                propertyType = property.type,
                receiver1,
                receiver2
            )
        }

        val kPropertyPropertyName = reference.referencedName.kPropertyVarName

        val kPropertyProperty =
            propertyContainer.propertyWithNameOrNull(kPropertyPropertyName) ?: irFactory.buildProperty {
                name = Name.identifier(kPropertyPropertyName)
                visibility = property.visibility
                isVar = false
                isLateinit = true
                isConst = kPropertyConstructorCall.isConst
            }.apply {
                val kPropertyProp = this

                propertyContainer.addChild(this)

                backingField = irFactory.buildField {
                    updateFrom(property)
                    name = kPropertyProp.name
                    visibility = kPropertyProp.visibility

                    type = kPropertyConstructorCall.constructorCall.type
                }.apply {
                    parent = kPropertyProp.parent
                    initializer = IrExpressionBodyImpl(kPropertyConstructorCall.constructorCall)
                }

                getter = createDefaultGetter(type)
            }

        return replaceWith(
            buildStatement(context.container.symbol) {
                irCall(kPropertyProperty.getter!!, receiver1, origin = IrStatementOrigin.GET_PROPERTY)
            }
        )
    }

    class Local(override val context: DartLoweringContext) : IrDeclarationLowering {
        override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
            if (declaration !is IrFunction) return noChange()

            // TODO: Support IrExpressionBody
            val body = declaration.body as? IrBlockBody ?: return noChange()

            var kPropertyVarsCount = 0

            fun getOrAddKPropertyVar(
                context: IrExpressionContext,
                reference: IrLocalDelegatedPropertyReference
            ): IrVariable {
                val kPropertyVarName = reference.referencedName.kPropertyVarName

                val kPropertyConstructorCall by lazy {
                    fun IrSingleStatementBuilder.irThrowUnsupported(message: String) =
                        irThrow(
                            irCallConstructor(
                                dartBuiltIns.unsupportedError.owner.primaryConstructor!!.symbol,
                                typeArguments = emptyList()
                            ).apply {
                                putValueArgument(0, message.toIrConst(irBuiltIns.stringType))
                            }
                        )

                    createKPropertyConstructorCall(
                        context,
                        declaration.symbol,
                        propertyReference = reference,
                        propertyGetter = reference.getter,
                        propertySetter = reference.setter,
                        propertyType = reference.getter.owner.returnType,
                        receiver1 = reference.receiver,
                        receiver2 = null, // TODO
                        createGetterLambdaBody = { irThrowUnsupported("Cannot call getter for this declaration") },
                        createSetterLambdaBody = { irThrowUnsupported("Cannot call setter for this declaration") }
                    )
                }

                return body
                    .statements
                    .filterIsInstance<IrDeclaration>()
                    .variableWithNameOrNull(kPropertyVarName)
                    ?: IrVariableImpl(
                        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                        origin = IrDartDeclarationOrigin.PROPERTY_REFERENCE,
                        symbol = IrVariableSymbolImpl(),
                        name = Name.identifier(kPropertyVarName),
                        type = kPropertyConstructorCall.constructorCall.type,
                        isVar = false,
                        isConst = kPropertyConstructorCall.isConst,
                        isLateinit = true
                    ).apply {
                        parent = declaration

                        initializer = kPropertyConstructorCall.constructorCall

                        body.statements.add(index = kPropertyVarsCount, this).also {
                            kPropertyVarsCount++
                        }
                    }
            }

            // We don't call transformExpressions on the body directly, because we only want to transform
            // the expressions in the statements. Any nested declarations will be handled by the next lowering pass.
            body.statements.toList().forEach {
                it.transformExpressions(initialParent = declaration) { expression, context ->
                    when (expression) {
                        is IrLocalDelegatedPropertyReference -> buildStatement(declaration.symbol) {
                            irGet(getOrAddKPropertyVar(context, expression))
                        }
                        else -> expression
                    }
                }
            }

            return noChange()
        }
    }
}


private val Name.kPropertyVarName: String
    get() = "$this\$kProperty"


private fun DartLoweringContext.createKPropertyConstructorCall(
    context: IrExpressionContext,
    containerSymbol: IrSymbol,
    propertyReference: IrCallableReference<*>,
    propertyGetter: IrSimpleFunctionSymbol?,
    propertySetter: IrSimpleFunctionSymbol?,
    propertyType: IrType,
    receiver1: IrExpression?,
    receiver2: IrExpression?,
    createGetterLambdaBody: (IrSingleStatementBuilder.() -> IrExpression)? = null,
    createSetterLambdaBody: (IrSingleStatementBuilder.() -> IrExpression)? = null,
): KPropertyConstructorCall {
    val receiverCount: Int = listOf(receiver1, receiver2).map { if (it != null) 1 else 0 }.sum()

    var isMutable = false
    val kPropertyClass = with(dartBuiltIns.dotlin) {
        when (receiver1) {
            null -> when (propertySetter) {
                null -> kProperty0Impl
                else -> kMutableProperty0Impl.also { isMutable = true }
            }
            else -> when (propertySetter) {
                null -> kProperty1Impl
                else -> kMutableProperty1Impl.also { isMutable = true }
            }

        }
    }.owner

    fun buildGetterOrSetterFunc(
        block: IrSimpleFunction.(getReceiver1: IrGetValue?, getReceiver2: IrGetValue?) -> Unit
    ) = irFactory.buildFun {
        name = Name.special("<anonymous>")
        returnType = propertyType
    }.apply {
        parent = context.container as IrDeclarationParent

        fun buildReceiverParameter(receiver: IrExpression?, ordinal: Int) = when (receiver) {
            null -> null
            else -> buildValueParameter(this) {
                name = Name.identifier("\$receiver$ordinal")
                type = receiver.type
            }.let {
                valueParameters = valueParameters + it

                buildStatement(symbol) {
                    irGet(it)
                }
            }
        }

        val getReceiver1 = buildReceiverParameter(receiver1, ordinal = 1)
        val getReceiver2 = buildReceiverParameter(receiver2, ordinal = 2)

        block(this, getReceiver1, getReceiver2)
    }

    val kPropertyGetterLambda = buildGetterOrSetterFunc { getReceiver1, _ /* TODO */ ->
        body = IrExpressionBodyImpl(
            buildStatement(symbol) {
                createGetterLambdaBody?.invoke(this) ?: irCall(
                    propertyGetter!!.owner,
                    getReceiver1,
                    origin = IrStatementOrigin.GET_PROPERTY
                )
            }
        )
    }
    val kPropertyType = kPropertyClass.defaultType.buildSimpleType {
        arguments = listOfNotNull(
            when (receiver1) {
                null -> null
                else -> makeTypeProjection(
                    receiver1.type, Variance.INVARIANT
                )
            },
            makeTypeProjection(propertyType, Variance.INVARIANT)
        )
    }

    return KPropertyConstructorCall(
        constructorCall = buildStatement(containerSymbol) {
            irCallConstructor(
                kPropertyClass.primaryConstructor!!.symbol,
                typeArguments = emptyList()
            ).apply {
                type = kPropertyType

                putValueArgument(
                    index = 0,
                    propertyReference.referencedName.identifier.toIrConst(irBuiltIns.stringType)
                )

                putValueArgument(
                    index = 1,
                    IrFunctionExpressionImpl(
                        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                        type = kotlin.run {
                            val getterFuncReturnType =
                                makeTypeProjection(propertyType, Variance.OUT_VARIANCE)

                            IrSimpleTypeImpl(
                                classifier = irBuiltIns.functionN(receiverCount).symbol,
                                hasQuestionMark = false,
                                arguments = listOfNotNull(
                                    getterFuncReturnType,
                                    receiver1?.let {
                                        makeTypeProjection(
                                            it.type,
                                            variance = Variance.INVARIANT
                                        )
                                    }
                                ),
                                annotations = emptyList()
                            )
                        },
                        function = kPropertyGetterLambda,
                        origin = IrDartStatementOrigin.PROPERTY_REFERENCE
                    )
                )

                if (isMutable) {
                    putValueArgument(
                        index = 2,
                        IrFunctionExpressionImpl(
                            SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                            type = kotlin.run {
                                IrSimpleTypeImpl(
                                    classifier = irBuiltIns.functionN(receiverCount + 1).symbol,
                                    hasQuestionMark = false,
                                    arguments = listOfNotNull(
                                        makeTypeProjection(irBuiltIns.unitType, Variance.OUT_VARIANCE),
                                        receiver1?.let {
                                            makeTypeProjection(
                                                receiver1.type,
                                                variance = Variance.INVARIANT
                                            )
                                        },
                                        makeTypeProjection(propertyType, Variance.INVARIANT)
                                    ),
                                    annotations = emptyList()
                                )
                            },
                            function = buildGetterOrSetterFunc { getReceiver1, _ /* TODO */ ->
                                val valueParameter = buildValueParameter(this) {
                                    name = Name.identifier("\$value")
                                    type = propertyType
                                }

                                valueParameters = valueParameters + valueParameter

                                body = IrExpressionBodyImpl(
                                    buildStatement(symbol) {
                                        createSetterLambdaBody?.invoke(this) ?: irCall(
                                            propertySetter!!.owner,
                                            getReceiver1,
                                            irGet(valueParameter),
                                            origin = IrStatementOrigin.EQ
                                        )
                                    }
                                )
                            },
                            origin = IrDartStatementOrigin.PROPERTY_REFERENCE
                        )
                    )
                }
            }
        },
        isConst = propertyReference.isAccessibleInDartConstLambda(kPropertyGetterLambda)
    )
}

private data class KPropertyConstructorCall(val constructorCall: IrConstructorCall, val isConst: Boolean)