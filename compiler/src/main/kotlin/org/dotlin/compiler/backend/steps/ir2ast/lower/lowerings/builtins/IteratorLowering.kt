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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.builtins

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.common.ir.createDispatchReceiverParameter
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildProperty
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.addToStdlib.cast

class IteratorSubtypeImplementationsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass ||
            declaration.isExternalIterator() ||
            !declaration.allSuperInterfaces().any { it.isIterator() }
        ) {
            return noChange()
        }

        val isBidirectional = declaration.allSuperInterfaces().any { it.isBidirectionalIterator() }

        // If there are no `next` or `previous` implementations, we don't have to do anything.
        if (!declaration.methodWithName("next").isImplemented() &&
            (!isBidirectional || !declaration.methodWithName("previous").isImplemented())
        ) {
            return noChange()
        }

        val elementType = declaration.resolveConcreteIteratorElementType() ?: return noChange()

        val dartIteratorClass = when {
            isBidirectional -> dartBuiltIns.bidirectionalIterator.owner
            else -> dartBuiltIns.iterator.owner
        }
        val dartIteratorCurrentProp = dartIteratorClass.propertyWithName("current")

        declaration.apply {
            val thisReceiver = thisReceiver!!

            superTypes = superTypes + IrSimpleTypeImpl(
                dartIteratorClass.symbol,
                hasQuestionMark = false,
                arguments = listOf(
                    makeTypeProjection(
                        elementType,
                        variance = Variance.INVARIANT
                    )
                ),
                annotations = emptyList()
            )

            declarations.apply {
                val currentProp = irFactory.buildProperty {
                    name = Name.identifier("current")
                    isVar = true
                    isLateinit = true
                }.apply {
                    val property = this

                    parent = declaration
                    overriddenSymbols = listOf(dartIteratorCurrentProp.symbol)

                    backingField = irFactory.buildField {
                        name = property.name
                        type = elementType
                        isFinal = false
                        origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
                        visibility = DescriptorVisibilities.PRIVATE
                    }.apply {
                        parent = declaration
                        correspondingPropertySymbol = property.symbol
                    }

                    createDefaultGetter(elementType).apply {
                        overriddenSymbols = listOf(dartIteratorCurrentProp.getter!!.symbol)
                        body = IrExpressionBodyImpl(
                            expression = buildStatement(symbol) {
                                irGetField(
                                    receiver = irGet(thisReceiver),
                                    field = backingField!!
                                )
                            }
                        )
                    }

                    createDefaultSetter(elementType).apply {
                        visibility = DescriptorVisibilities.PRIVATE
                        body = IrExpressionBodyImpl(
                            expression = buildStatement(symbol) {
                                irSetField(
                                    receiver = irGet(thisReceiver),
                                    field = backingField!!,
                                    value = irGet(valueParameters[0])
                                )
                            }
                        )
                    }
                }.also { add(it) }

                fun buildMoveFun(isNext: Boolean = true): IrSimpleFunction {
                    val (moveName, hasName, ktName) = when {
                        isNext -> Triple("moveNext", "hasNext", "next")
                        else -> Triple("movePrevious", "hasPrevious", "previous")
                    }

                    return irFactory.buildFun {
                        name = Name.identifier(moveName)
                        returnType = irBuiltIns.booleanType
                    }.apply {
                        parent = declaration
                        overriddenSymbols = listOf(dartIteratorClass.methodWithName(moveName).symbol)
                        createDispatchReceiverParameter()

                        body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                            val hasNextTempVar = buildStatement(symbol) {
                                scope.createTemporaryVariable(
                                    irCall(
                                        methodWithName(hasName),
                                        receiver = irGet(thisReceiver)
                                    ),
                                    irType = irBuiltIns.booleanType,
                                    nameHint = hasName,
                                )
                            }

                            statements.addAll(
                                listOf(
                                    hasNextTempVar,
                                    buildStatement(symbol) {
                                        irIfThen(
                                            type = irBuiltIns.unitType,
                                            condition = irGet(hasNextTempVar),
                                            thenPart = irCallSet(
                                                currentProp,
                                                irCall(
                                                    methodWithName(ktName),
                                                    receiver = irGet(thisReceiver)
                                                )
                                            )
                                        )
                                    },
                                    buildStatement(symbol) {
                                        irReturn(irGet(hasNextTempVar))
                                    }
                                )
                            )
                        }
                    }
                }

                add(buildMoveFun())

                if (isBidirectional) {
                    add(buildMoveFun(isNext = false))
                }
            }
        }

        return noChange()
    }

    private fun IrClass.resolveConcreteIteratorElementType(): IrType? {
        val ourClass = this
        val iteratorSuperType = allSuperInterfaces().firstOrNull { it.isIterator() } as? IrSimpleType ?: return null
        val iteratorTypeArgumentType = iteratorSuperType.arguments.single().typeOrNull ?: return null

        if (!iteratorTypeArgumentType.isTypeParameter()) {
            return iteratorTypeArgumentType
        }

        fun subTypeOf(type: IrType) = allSuperTypes().firstOrNull { type in it.superTypes() } as? IrSimpleType

        fun IrSimpleType.elementType(iteratorTypeParameter: IrTypeParameter): IrType? {
            val typeClass = classOrNull?.owner ?: return null

            fun subElementType(typeParam: IrTypeParameter) =
                (subTypeOf(this) ?: ourClass.defaultType).elementType(typeParam)

            val hasIteratorTypeParameter = iteratorTypeParameter in typeClass.typeParameters
            return when {
                hasIteratorTypeParameter -> when (typeClass) {
                    ourClass -> iteratorTypeParameter.defaultType
                    else -> subElementType(iteratorTypeParameter)
                }
                else -> typeClass.superTypes
                    .first { it.classOrNull?.owner == iteratorTypeParameter.parent }
                    .cast<IrSimpleType>()
                    .arguments[iteratorTypeParameter.index]
                    .typeOrNull
                    ?.let {
                        when {
                            it.isTypeParameter() -> subElementType(it.typeParameterOrNull!!)
                            else -> it
                        }
                    }
            }
        }

        return subTypeOf(iteratorSuperType)?.elementType(iteratorTypeArgumentType.typeParameterOrNull!!)
    }

    /**
     * Only applicable to methods that are expected to return a value other than [Unit].
     */
    private fun IrSimpleFunction.isImplemented() =
        !isFakeOverride() && body != null && body?.statements?.isNotEmpty() == true
}

/**
 * All `return`s are updated in `next()` (or `previous()`) of subtypes of `Iterator` to update `current`.
 */
class IteratorSubtypeReturnsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
        if (expression !is IrReturn) return noChange()
        if (container !is IrSimpleFunction) return noChange()

        if (container.parentClassOrNull?.isExternalIterator() == true) return noChange()

        val overrideParent = container.resolveRootOverride()?.parentClassOrNull ?: return noChange()
        if (!(overrideParent.isIterator() || overrideParent.isBidirectionalIterator()) ||
            !(container.name == Name.identifier("next") || container.name == Name.identifier("previous"))
        ) {
            return noChange()
        }

        val currentProp = container.parentClassOrNull!!.propertyWithName("current")

        return replaceWith(
            IrReturnImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                type = expression.type,
                returnTargetSymbol = expression.returnTargetSymbol,
                value = buildStatement(container.symbol) {
                    irCallSet(currentProp, expression.value)
                }
            )
        )
    }
}

private fun IrType.isIterator() = (this as? IrSimpleType)?.classifier?.signature ==
        getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "Iterator")

private fun IrClass.isIterator() = defaultType.isIterator()

private fun IrType.isBidirectionalIterator() = (this as? IrSimpleType)?.classifier?.signature ==
        getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "BidirectionalIterator")

private fun IrClass.isBidirectionalIterator() = defaultType.isBidirectionalIterator()

// The ExternalIterator is not lowered since it already has a proper implementation.
private fun IrClass.isExternalIterator(): Boolean = kotlinFqName == FqName("dotlin.ExternalIterator") ||
        superTypes.any { it.classOrNull?.owner?.isExternalIterator() == true }