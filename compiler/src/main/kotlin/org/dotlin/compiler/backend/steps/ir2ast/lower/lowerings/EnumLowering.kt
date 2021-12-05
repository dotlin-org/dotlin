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
import org.dotlin.compiler.backend.steps.replace
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

@Suppress("UnnecessaryVariable")
class EnumLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || !declaration.isEnumClass) return noChange()

        // TODO: Apply @sealed annotation

        // TODO: Handle enum entry classes

        val enum = declaration
        val enumSuperType = enum.superTypes.first()

        val enumConstructor = enum.primaryConstructor!!

        // TODO: Manual remap
        //enum.file.remap(entriesToFields)

        // Clean up constructor.
        enumConstructor.apply {
            val nameParameter = buildValueParameter(this@apply) {
                name = Name.identifier("name")
                type = context.irBuiltIns.stringType
                origin = IrDeclarationOrigin.DEFINED // TODO: Different origin
                index = 0
            }

            val ordinalParameter = buildValueParameter(this@apply) {
                name = Name.identifier("ordinal")
                type = context.irBuiltIns.intType
                origin = IrDeclarationOrigin.DEFINED // TODO: Different origin
                index = 1
            }

            valueParameters = listOf(nameParameter, ordinalParameter)

            var parameterIndex = 2

            // Handle any extra fields.
            enum.declarations
                .filterIsInstance<IrProperty>()
                .filter { it.origin == IrDeclarationOrigin.DEFINED }
                .forEach { property ->
                    val parameter = buildValueParameter(this@apply) {
                        name = property.name
                        type = property.getter!!.returnType
                        origin = IrDeclarationOrigin.DEFINED // TODO: Different origin
                        index = parameterIndex++
                    }

                    valueParameters = valueParameters + parameter

                    property.backingField!!.initializer = IrGetValueImpl(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        symbol = parameter.symbol,
                        origin = IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER
                    ).wrap()
                }

            (body as IrBlockBody).statements.apply {
                removeIf { it is IrEnumConstructorCall }

                add(
                    context.buildStatement(symbol) {
                        irDelegatingConstructorCall(
                            callee = enumSuperType.classOrNull!!.owner.primaryConstructor!!
                        ).apply {
                            putValueArgument(0, irGet(nameParameter))
                            putValueArgument(1, irGet(ordinalParameter))
                        }
                    }
                )
            }
        }

        val entriesToFields = enum.declarations
            .filterIsInstance<IrEnumEntry>()
            .let { enumEntries ->
                enumEntries.associateWith { enumEntry ->
                    context.irFactory.buildField {
                        name = enumEntry.name
                        type = enum.defaultType
                        isStatic = true
                        origin = IrDeclarationOrigin.FIELD_FOR_ENUM_ENTRY
                    }.apply {
                        parent = enum

                        initializer = context.buildStatement(symbol) {
                            irCallConstructor(
                                enumConstructor.symbol,
                                typeArguments = emptyList()
                            ).apply {
                                putValueArgument(0, name.identifier.toIrConst(context.irBuiltIns.stringType))
                                putValueArgument(
                                    1,
                                    enumEntries.indexOf(enumEntry).toIrConst(context.irBuiltIns.intType)
                                )

                                // Add all extra arguments to constructor call.
                                (enumEntry.initializerExpression?.expression as IrEnumConstructorCall?)
                                    ?.valueArguments
                                    ?.forEachIndexed { index, arg ->
                                        putValueArgument(index + 2 /* Because we have 2 args already */, arg)
                                    }
                            }
                        }.wrap()
                    }
                }.onEach { (entry, field) ->
                    enum.declarations.replace(entry, field)
                }
            }


        // TODO: Body of valueOf & values

        // compareTo will be defined in Enum<T> itself, mark as fake override.
        enum.declarations.apply {
            methodWithName("compareTo").let { old ->
                val new = old.deepCopyWith {
                    isFakeOverride = true
                }

                replace(old, new)
            }
        }

        return noChange()
    }
}