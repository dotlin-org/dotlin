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

import org.dotlin.compiler.backend.attributes.CollectionLiteralKind.*
import org.dotlin.compiler.backend.kotlin
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.isDartConstInlineFunction
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irFalse
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.primaryConstructor

/**
 * Transforms `listOf`, `setOf`, etc. Some variants are not lowered, but instead handled
 * in [IrToDartExpressionTransformer].
 */
@Suppress("UnnecessaryVariable")
class CollectionFactoryCallsLowering(override val context: DotlinLoweringContext) : IrExpressionLowering {
    override fun DotlinLoweringContext.transform(
        expression: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? = context.run {
        if (expression !is IrCall) return noChange()

        val function = expression.symbol.owner

        val fqName = function.fqNameWhenAvailable ?: return noChange()
        if (fqName !in loweredFactories) return noChange()

        val isDartConst by lazy {
            expression.isDartConst(
                initializedIn = context.initializerContainer?.declaration,
                constInlineContainer = when {
                    context.container.isDartConstInlineFunction() -> context.container
                    else -> null
                }
            )
        }

        val mutableListCompanion by lazy {
            irBuiltIns.mutableListClass.owner.companionObject()!!
        }

        val arrayCompanion by lazy {
            irBuiltIns.arrayClass.owner.companionObject()!!
        }

        val irVarargOrEmpty by lazy {
            val result = expression.valueArguments.elementAtOrNull(0) as? IrVararg
                ?: buildStatement(container.symbol) { irVararg(expression.typeArguments.first()!!, emptyList()) }

            result.apply {
                literalKind = when (fqName) {
                    in loweredListFactories, in loweredArrayFactories -> LIST
                    in loweredSetFactories -> SET
                    in loweredMapFactories -> MAP
                    else -> throw UnsupportedOperationException("Unsupported factory: $fqName")
                }
            }
        }

        fun IrVararg.makeDartConst() = also {
            // Copy the source so that later isDartConst checks are true on the irVararg.
            // TODO?: Use IrAttributes for this
            it.ktExpression = expression.ktExpression
        }

        // Const calls are always just literals.
        if (isDartConst) {
            return replaceWith(
                irVarargOrEmpty.makeDartConst()
            )
        }

        fun IrFunctionAccessExpression.callType() = IrSimpleTypeImpl(
            classifier = symbol.owner.returnType.classifierOrFail,
            hasQuestionMark = false,
            arguments = (expression.type as IrSimpleType).arguments,
            annotations = emptyList(),
        )

        return replaceWith(
            buildStatement(container.symbol) {
                val emptyArrayCall by lazy {
                    irCall(
                        arrayCompanion.methodWithName("empty"),
                        receiver = null,
                        irFalse(), /* growable */
                    ).apply {
                        type = callType()
                    }
                }

                with(kotlin) {
                    with(kotlin.collections) {
                        when (fqName) {
                            listOf, emptyList, setOf, emptySet -> {
                                val immutableView = when (fqName) {
                                    listOf, emptyList -> dotlinIrBuiltIns.dart.immutableListView.owner
                                    setOf, emptySet -> dotlinIrBuiltIns.dart.immutableSetView.owner
                                    else -> throw UnsupportedOperationException("Unsupported: $fqName")
                                }

                                irCallConstructor(
                                    immutableView.primaryConstructor!!.symbol,
                                    typeArguments = emptyList()
                                ).apply {
                                    type = callType()
                                    putValueArgument(index = 0, irVarargOrEmpty)
                                }
                            }
                            mutableListOf, mutableSetOf -> irVarargOrEmpty
                            arrayOf -> when (expression.valueArgumentsCount) {
                                0 -> emptyArrayCall
                                else -> irCall(
                                    arrayCompanion.methodWithName("of"),
                                    receiver = null,
                                    irVarargOrEmpty, /* elements */
                                    irFalse() /* growable */
                                ).apply {
                                    type = callType()
                                }
                            }
                            emptyArray -> emptyArrayCall
                            else -> throw UnsupportedOperationException("Unknown collection creation call: $fqName")
                        }
                    }
                }
            }
        )
    }

    private val loweredListFactories = with(kotlin.collections) {
        listOf(listOf, emptyList, mutableListOf)
    }

    private val loweredArrayFactories = with(kotlin) {
        listOf(arrayOf, emptyArray)
    }

    private val loweredSetFactories = with(kotlin.collections) {
        listOf(setOf, mutableSetOf, emptySet)
    }

    private val loweredMapFactories = with(kotlin.collections) {
        listOf(mapOf, mutableMapOf, emptyMap)
    }

    private val loweredFactories =
        loweredListFactories + loweredArrayFactories + loweredSetFactories + loweredMapFactories
}