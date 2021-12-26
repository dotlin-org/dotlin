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
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrDartCodeExpression
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.replace
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

// TODO: Use
private object Documentation {
    const val VALUES = """
    /**
     * Returns an array containing the constants of this enum type, in the order they're declared.
     * This method may be used to iterate over the constants.
     * @values
     */
    """

    const val VALUE_OF = """
    /**
     * Returns the enum constant of this type with the specified name. The string must match exactly an identifier used to declare an enum constant in this type. (Extraneous whitespace characters are not permitted.)
     * @throws IllegalArgumentException if this enum type has no constant with the specified name
     * @valueOf
     */
    """
}

@Suppress("UnnecessaryVariable")
class EnumClassLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || !declaration.isEnumClass) return noChange()

        // TODO: Apply @sealed annotation

        // TODO: Handle enum entry classes

        val enum = declaration
        val enumSuperType = enum.superTypes.first()

        val enumConstructor = enum.primaryConstructor!!

        // Clean up constructor.
        enumConstructor.apply {
            val nameParameter = buildValueParameter(this@apply) {
                name = Name.identifier("name")
                type = irBuiltIns.stringType
                origin = IrDeclarationOrigin.DEFINED // TODO: Different origin
                index = 0
            }

            val ordinalParameter = buildValueParameter(this@apply) {
                name = Name.identifier("ordinal")
                type = irBuiltIns.intType
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
                    buildStatement(symbol) {
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
                    irFactory.buildField {
                        name = enumEntry.name
                        type = enum.defaultType
                        isStatic = true
                        origin = IrDeclarationOrigin.FIELD_FOR_ENUM_ENTRY
                    }.apply {
                        parent = enum

                        initializer = buildStatement(symbol) {
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

        // compareTo will be defined in Enum<T> itself, mark as fake override.
        enum.declarations.apply {
            methodWithName("compareTo").let { old ->
                val new = old.deepCopyWith {
                    isFakeOverride = true
                }

                replace(old, new)
            }

            methodWithName("values").apply {
                body = IrBlockBodyImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    statements = listOf(
                        buildStatement(symbol) {
                            irReturn(
                                irVararg(
                                    enum.defaultType,
                                    entriesToFields.values.map {
                                        irGetField(
                                            receiver = null,
                                            field = it,
                                        )
                                    }
                                )
                            )
                        }
                    )
                )
            }

            methodWithName("valueOf").apply {
                body = IrBlockBodyImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    statements = listOf(
                        IrDartCodeExpression(
                            code =
                            """
                            return values().firstWhere((v) => v.name == value)
                            """.trimIndent(),
                            type = context.dynamicType
                        )
                    )
                )
            }
        }

        return noChange()
    }
}