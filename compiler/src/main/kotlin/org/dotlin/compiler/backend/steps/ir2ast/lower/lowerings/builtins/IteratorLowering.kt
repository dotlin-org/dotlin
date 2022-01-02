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

import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.ir.propertyWithName
import org.dotlin.compiler.backend.steps.ir2ast.ir.resolveOverride
import org.dotlin.compiler.backend.steps.ir2ast.ir.type
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.wasm.ir2wasm.allSuperInterfaces
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.types.getPublicSignature
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.Name

class IteratorLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || !declaration.isIterator()) return noChange()

        declaration.apply {
            declaration.propertyWithName("current").apply {
                getter = null

                setter = setter!!.deepCopyWith {
                    modality = Modality.ABSTRACT
                }.apply {
                    body = null
                }
            }
        }

        return noChange()
    }
}

class IteratorSubtypeBackingFieldsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass ||
            declaration.isInterface ||
            !declaration.allSuperInterfaces().any { it.isIterator() }
        ) {
            return noChange()
        }

        declaration.apply {
            declaration.propertyWithName("current").apply {
                val property = this

                backingField = irFactory.buildField {
                    name = Name.identifier("current")
                    type = property.type
                    isFinal = false
                    origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
                    visibility = DescriptorVisibilities.PRIVATE
                }.apply {
                    overriddenSymbols = emptyList()
                    parent = declaration
                    correspondingPropertySymbol = property.symbol
                }

                getter!!.body = IrExpressionBodyImpl(
                    expression = buildStatement(symbol) {
                        irGetField(
                            receiver = irGet(declaration.thisReceiver!!),
                            field = backingField!!
                        )
                    }
                )

                setter!!.apply {
                    body = IrExpressionBodyImpl(
                        expression = buildStatement(symbol) {
                            irSetField(
                                receiver = irGet(declaration.thisReceiver!!),
                                field = backingField!!,
                                value = irGet(valueParameters[0])
                            )
                        }
                    )
                }
            }
        }

        return noChange()
    }
}

/**
 * All `return`s are updated in `next()` (or `previous()` of subtypes of `Iterator` to update `current`.
 */
class IteratorSubtypeReturnsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
        if (container.parentClassOrNull?.allSuperInterfaces()?.any { it.isIterator() } != true) {
            return noChange()
        }

        if (container !is IrSimpleFunction) return noChange()

        val overrideParent = container.resolveOverride()?.parentClassOrNull ?: return noChange()
        if (!(overrideParent.isIterator() || overrideParent.isListIterator()) ||
            !(container.name == Name.identifier("next") || container.name == Name.identifier("previous"))
        ) {
            return noChange()
        }

        if (expression !is IrReturn) return noChange()

        val iteratorClass = irBuiltIns.iteratorClass.owner
        val property = iteratorClass.propertyWithName("current")

        return replaceWith(
            IrReturnImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                type = expression.type,
                returnTargetSymbol = expression.returnTargetSymbol,
                value = buildStatement(container.symbol) {
                    irCall(
                        property.setter!!.symbol,
                        type = property.type,
                        origin = IrStatementOrigin.EQ,
                    ).apply {
                        dispatchReceiver = irGet(property.parentAsClass.thisReceiver!!)

                        putValueArgument(
                            index = 0,
                            valueArgument = expression.value
                        )
                    }
                }
            )
        )
    }
}

private fun IrClass.isIterator() = defaultType.classifier.signature ==
        getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "Iterator")

private fun IrClass.isListIterator() = defaultType.classifier.signature ==
        getPublicSignature(StandardNames.COLLECTIONS_PACKAGE_FQ_NAME, "ListIterator")