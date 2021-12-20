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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.isFromObjectAndStaticallyAvailable
import org.dotlin.compiler.backend.util.isSimple
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

class ObjectLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || !declaration.isObject) return noChange()

        val instanceField: IrField
        val staticContainer: IrClass
        val transformations: Transformations<IrDeclaration>

        val obj = declaration.deepCopyWith {
            name = when {
                declaration.isCompanion -> Name.identifier("$" + declaration.parentAsClass.name.identifier + "Companion")
                else -> declaration.name
            }

            origin = IrDartDeclarationOrigin.OBJECT
        }.apply {
            val newObj = this

            primaryConstructor!!.visibility = DescriptorVisibilities.PRIVATE

            addChild(
                context.irFactory.buildField {
                    isStatic = true
                    type = defaultType
                    name = Name.identifier("\$instance")
                    origin = IrDartDeclarationOrigin.OBJECT_INSTANCE_FIELD
                }.apply {
                    parent = newObj

                    initializer = IrExpressionBodyImpl(
                        context.buildStatement(symbol) {
                            IrConstructorCallImpl(
                                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                                type = defaultType,
                                symbol = primaryConstructor!!.symbol,
                                typeArgumentsCount = 0,
                                constructorTypeArgumentsCount = 0,
                                valueArgumentsCount = 0,
                                origin = IrDartStatementOrigin.OBJECT_CONSTRUCTOR
                            )
                        }
                    )
                }.also {
                    instanceField = it
                }
            )
        }

        when {
            obj.isCompanion -> {
                staticContainer = obj.parentAsClass

                declaration.file.addChild(obj)

                transformations = remove() and add(
                    context.irFactory.buildField {
                        isStatic = true
                        type = obj.defaultType
                        name = Name.identifier("\$companion")
                        origin = IrDartDeclarationOrigin.OBJECT_INSTANCE_FIELD
                    }.apply {
                        parent = obj

                        initializer = IrExpressionBodyImpl(
                            context.createIrBuilder(symbol).buildStatement {
                                irGetField(
                                    receiver = irGetObject(obj.symbol),
                                    field = obj.fieldWithName("\$instance"),
                                )
                            }
                        )
                    }
                )
            }
            else -> {
                staticContainer = obj
                transformations = just { replaceWith(obj) }
            }
        }

        fun IrSimpleFunction.redirectCall(original: IrSimpleFunction) = IrExpressionBodyImpl(
            expression = context.buildStatement(symbol) {
                irCall(
                    original,
                    origin = when {
                        original.isGetter -> IrStatementOrigin.GET_PROPERTY
                        original.isSetter -> IrStatementOrigin.EQ
                        else -> null
                    },
                ).apply {
                    dispatchReceiver = irGetField(receiver = null, instanceField)

                    valueParameters.forEach {
                        putValueArgument(
                            index = it.index,
                            valueArgument = irGet(it)
                        )
                    }
                }
            }
        )

        // Add static methods/properties.
        obj.declarations
            .filter { it.isFromObjectAndStaticallyAvailable }
            .map { original ->
                when (original) {
                    is IrSimpleFunction -> original.deepCopy(remapReferences = false).apply {
                        dispatchReceiverParameter = null
                        body = redirectCall(original)
                    }
                    is IrProperty -> original.deepCopy(remapReferences = false).apply {
                        getter = getter?.deepCopy()?.apply {
                            dispatchReceiverParameter = null
                            body = redirectCall(original.getter!!)
                        }
                        setter = setter?.deepCopy()?.apply {
                            dispatchReceiverParameter = null
                            body = redirectCall(original.setter!!)
                        }
                        // We never need a static backing field. If there's a backing field it will be an instance
                        // member of the object class.
                        //
                        // However, if the property is simple, we add the redirecting initializer here, so it will be
                        // taken over in the [PropertySimplifyingLowering].
                        backingField = when {
                            isSimple -> backingField?.deepCopyWith {
                                isStatic = true
                            }?.apply {
                                initializer = IrExpressionBodyImpl(
                                    expression = context.buildStatement(symbol) {
                                        irGetField(
                                            receiver = irGetField(receiver = null, instanceField),
                                            original.backingField!!,
                                        )
                                    }
                                )
                            }
                            else -> null
                        }
                    }
                    else -> throw UnsupportedOperationException("Unsupported object member: $original")
                }
            }
            .forEach { staticContainer.addChild(it) }

        return transformations
    }
}