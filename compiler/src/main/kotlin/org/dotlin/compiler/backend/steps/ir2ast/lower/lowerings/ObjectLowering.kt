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
import org.dotlin.compiler.backend.util.isFromObjectAndStaticallyAvailable
import org.dotlin.compiler.backend.util.isSimple
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

class ObjectLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    companion object {
        const val INSTANCE_FIELD_NAME = "\$instance"
        const val COMPANION_FIELD_NAME = "\$companion"
    }

    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || !declaration.isObject) return noChange()

        val instanceField: IrField
        val staticContainer: IrClass
        val transformations: Transformations<IrDeclaration>

        val obj = declaration.apply obj@ {
            primaryConstructor!!.visibility = DescriptorVisibilities.PRIVATE

            addChild(
                irFactory.buildField {
                    isStatic = true
                    type = defaultType
                    name = Name.identifier(INSTANCE_FIELD_NAME)
                    origin = IrDeclarationOrigin.FIELD_FOR_OBJECT_INSTANCE
                }.apply {
                    parent = this@obj

                    initializer = IrExpressionBodyImpl(
                        buildStatement(symbol) {
                            IrConstructorCallImpl(
                                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                                type = defaultType,
                                symbol = primaryConstructor!!.symbol,
                                typeArgumentsCount = 0,
                                constructorTypeArgumentsCount = 0,
                                valueArgumentsCount = 0,
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
                staticContainer = obj.parentAsClass.let {
                    when {
                        // If this companion is for an external class, we add the static methods to the companion
                        // object itself.
                        it.isEffectivelyExternal() -> obj
                        else -> it
                    }
                }

                // We don't use addChild on purpose, we want to keep parent info.
                obj.file.declarations.add(obj)

                // We don't want to add the companion field to the companion itself, only add it if the static container
                // is something else.
                if (staticContainer != obj) {
                    staticContainer.addChild(
                        irFactory.buildField {
                            isStatic = true
                            type = obj.defaultType
                            name = Name.identifier(COMPANION_FIELD_NAME)
                            origin = IrDeclarationOrigin.FIELD_FOR_OBJECT_INSTANCE
                        }.apply {
                            parent = obj

                            initializer = IrExpressionBodyImpl(
                                createIrBuilder(symbol).buildStatement {
                                    irGetObject(obj.symbol)
                                }
                            )
                        }
                    )
                }

                transformations = just { remove() }
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
                    receiver = irGetField(receiver = null, instanceField),
                    valueArguments = valueParameters.map { irGet(it) }.toTypedArray(),
                    origin = when {
                        original.isGetter -> IrStatementOrigin.GET_PROPERTY
                        original.isSetter -> IrStatementOrigin.EQ
                        else -> null
                    },
                )
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
                        origin = IrDartDeclarationOrigin.STATIC_OBJECT_MEMBER
                    }
                    is IrProperty -> original.deepCopy(remapReferences = false).apply {
                        origin = IrDartDeclarationOrigin.STATIC_OBJECT_MEMBER

                        when {
                            original.isConst -> {
                                // We copy the value as-is to keep the value const in Dart. We can't redirect it
                                // to the instance member, since using instance members as const is not allowed
                                // (even on objects created with const).
                                backingField = backingField!!.deepCopyWith {
                                    isStatic = true
                                }.apply {
                                    initializer = original.backingField!!.initializer!!
                                }
                            }
                            else -> {
                                getter = getter?.apply {
                                    dispatchReceiverParameter = null
                                    body = redirectCall(original.getter!!)
                                }
                                setter = setter?.apply {
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
                                            expression = buildStatement(symbol) {
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
                        }
                    }
                    else -> throw UnsupportedOperationException("Unsupported object member: $original")
                }
            }
            .forEach { staticContainer.addChild(it) }

        obj.declarations.transformInPlace {
            when {
                it.origin != IrDartDeclarationOrigin.STATIC_OBJECT_MEMBER && it is IrProperty && it.isConst ->
                    it.deepCopyWith {
                        // Instance members can never be const.
                        isConst = false
                        origin = IrDartDeclarationOrigin.WAS_CONST_OBJECT_MEMBER
                    }
                else -> it
            }
        }

        return transformations
    }
}