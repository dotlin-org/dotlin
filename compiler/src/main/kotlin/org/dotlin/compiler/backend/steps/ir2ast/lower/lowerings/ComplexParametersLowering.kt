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

import org.dotlin.compiler.backend.steps.ir2ast.attributes.attributeOwner
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.dartName
import org.jetbrains.kotlin.backend.common.ir.createParameterDeclarations
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class ComplexParametersLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrValueParameter) return noChange()

        val irValueParameter = declaration

        val correspondingProperty = irValueParameter.correspondingProperty
        val isPropertyInitializer = correspondingProperty != null
        val currentIrFunction = irValueParameter.parent as IrFunction

        val originalDefaultValue = irValueParameter.defaultValue
        val hasDefaultValue = originalDefaultValue != null
        val hasComplexDefaultValue = hasDefaultValue && !originalDefaultValue!!.expression.isDartConst()

        val irBuilder = createIrBuilder(currentIrFunction.symbol)

        var newIrValueParameter: IrValueParameter? = null

        // Dart does not support non-const default values, so we need a workaround.
        // Depending on whether the default value is a literal, nullable, or not nullable, different
        // solutions are used.
        //
        // Overrides are handled in a separate lowering, which just copies the result of what's been done here.
        if (!irValueParameter.isOverride && hasComplexDefaultValue) {
            val originalType = irValueParameter.type
            newIrValueParameter = irValueParameter.asAssignable(
                origin = IrDartDeclarationOrigin.WAS_COMPLEX_PARAM(originalType)
            )

            val newDefaultValue = originalDefaultValue!!.apply {
                // Transform any IrGetValues that reference a property parameter to IrGetFields.
                replaceExpressions { exp ->
                    when (exp) {
                        is IrGetValue -> {
                            val owner = exp.symbol.owner

                            when {
                                owner is IrValueParameter && owner.correspondingProperty != null -> {
                                    irBuilder.buildStatement {
                                        val field = owner.correspondingProperty!!.backingField!!

                                        IrGetFieldImpl(
                                            UNDEFINED_OFFSET,
                                            UNDEFINED_OFFSET,
                                            field.symbol,
                                            field.type,
                                            receiver = irGet(owner.parentClassOrNull!!.thisReceiver!!),
                                        ).also {
                                            it.copyAttributes(exp)
                                            parameterPropertyReferencesInParameterDefaultValue.add(exp.attributeOwner())
                                        }
                                    }
                                }
                                else -> exp
                            }
                        }
                        else -> exp
                    }
                }
            }

            val irGetParam = irBuilder.buildStatement { irGet(newIrValueParameter) }

            val assignment: IrExpression
            val assignmentConditionRhs: IrExpression
            val assignmentEqualsElsePart: IrExpression

            val originalIsNullable = originalType.isNullable()

            if (!originalIsNullable) {
                // If the type is non-nullable on the Kotlin side, we will make the parameter nullable on the Dart side
                // and make the default value null. Then, if null is passed, the actual
                // non-const value will be used.

                val newType = originalType.makeNullable()

                val nullConst = IrConstImpl.constNull(
                    UNDEFINED_OFFSET,
                    UNDEFINED_OFFSET,
                    newType
                )

                newIrValueParameter.apply {
                    type = newType
                    defaultValue = nullConst.wrap()
                }

                assignmentEqualsElsePart = irGetParam
                assignmentConditionRhs = nullConst
            } else {
                // If the type is nullable on the Kotlin side, we will make the parameter dynamic on the Dart side, and
                // add a private _$DefaultValueMarker class with a const constructor, which will be used as the
                // default value. Then, null can be passed (and stay null).
                //
                // Depending on whether the parameter type is a Dart built-in or not, the default value class and the
                // parameter type changes. If the parameter is not a Dart built-in, a $DefaultXValue class is generated
                // for type X, implementing type X and with a const constructor and a noSuchMethod implementation.
                // This will be used as the default value for the parameter. If the type of the parameter is a
                // Dart built-in, this is not possible since these built-ins cannot be implemented. In that case the
                // type becomes dynamic with a more generic $DefaultValue class.
                val defaultValueClass = newIrValueParameter.file.declarations
                    .addIfNotExists(
                        createDefaultValueClass(
                            type = originalType,
                            file = newIrValueParameter.file
                        )
                    )

                val defaultValueConstructor = defaultValueClass.primaryConstructor!!

                newIrValueParameter.apply {
                    type = if (originalType.isDartCorePrimitive()) context.dynamicType else originalType
                    defaultValue = IrConstructorCallImpl.fromSymbolOwner(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        defaultValueConstructor.returnType,
                        defaultValueConstructor.symbol,
                        origin = IrDartStatementOrigin.COMPLEX_PARAM_INIT_DEFAULT_VALUE
                    ).wrap()
                }

                assignmentConditionRhs = irBuilder.buildStatement {
                    irCallConstructor(defaultValueConstructor.symbol, emptyList())
                }

                assignmentEqualsElsePart = when {
                    originalType.isDartCorePrimitive() -> irBuilder.buildStatement {
                        irAs(
                            argument = irGetParam,
                            type = originalType
                        )
                    }
                    // No need for casting if we use a default value with the correct type.
                    else -> irGetParam
                }
            }

            assignment = irBuilder.buildStatement {
                irSet(
                    variable = newIrValueParameter.symbol,
                    value = irIfThenElse(
                        type = newIrValueParameter.type,
                        condition = irEquals(irGetParam, assignmentConditionRhs),
                        thenPart = newDefaultValue.expression,
                        // Cast to the original default value type.
                        elsePart = assignmentEqualsElsePart
                    ),
                    origin = when {
                        originalIsNullable -> IrDartStatementOrigin.COMPLEX_PARAM_INIT_DEFAULT_VALUE
                        else -> IrDartStatementOrigin.COMPLEX_PARAM_INIT_NULLABLE
                    }
                )
            }

            if (isPropertyInitializer) {
                propertiesInitializedInFieldInitializerList.add(correspondingProperty!!.attributeOwner())
            }

            currentIrFunction.body = irBuilder.irBlockBody {
                +assignment
            }.apply {
                statements.addAll(currentIrFunction.body?.statements ?: emptyList())
            }
        }

        return newIrValueParameter?.let {
            just { replaceWith(it) }
        } ?: noChange()
    }

    private fun createDefaultValueClass(
        type: IrType,
        file: IrFile
    ): IrClass = context.irFactory.buildClass {
        name = Name.identifier(
            if (type.isDartCorePrimitive()) "\$DefaultValue" else "\$Default${type.getClass()!!.dartName}Value"
        )
        visibility = DescriptorVisibilities.PRIVATE
        origin = IrDartDeclarationOrigin.COMPLEX_PARAM_DEFAULT_VALUE
    }.apply {
        val irClass = this

        parent = file

        createParameterDeclarations()

        superTypes = if (!type.isDartCorePrimitive()) listOf(type.makeNotNull()) else emptyList()

        declarations += context.irFactory.buildConstructor {
            isPrimary = true
            returnType = defaultType
        }.apply {
            parent = irClass
            origin = irClass.origin
        }

        declarations += context.irFactory.buildFun {
            name = Name.identifier("noSuchMethod")
            returnType = context.dynamicType
        }.apply {
            parent = irClass
            origin = irClass.origin
            addDispatchReceiver {
                this.type = irClass.defaultType
            }

            addValueParameter {
                name = Name.identifier("invocation")
                // TODO: Reference type via DartBuiltIns
                this.type = context.irFactory.buildClass {
                    name = Name.identifier("Invocation")
                    origin = irClass.origin
                }.apply {
                    parent = file
                    createParameterDeclarations()
                }.defaultType
            }

            // TODO: Throw some error stating how this should never ever happen.
            body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET)
        }
    }
}