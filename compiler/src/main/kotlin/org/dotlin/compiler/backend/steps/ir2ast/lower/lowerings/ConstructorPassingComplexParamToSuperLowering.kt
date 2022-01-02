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
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

@Suppress("UnnecessaryVariable")
class ConstructorPassingComplexParamToSuperLowering(override val context: DartLoweringContext) :
    IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrConstructor) return noChange()

        val originalConstructor = declaration

        val originalComplexParams = originalConstructor.valueParameters.filter { it.wasComplex }
        val originalBody = originalConstructor.body as? IrBlockBody ?: return noChange()

        val superCall = originalBody.statements
            .filterIsInstance<IrDelegatingConstructorCall>()
            .singleOrNull()

        if (superCall == null ||
            !superCall.valueArguments.any { arg -> originalComplexParams.any { arg?.hasDirectReferenceTo(it) == true } }
        ) {
            // No complex params are passed to a super constructor, nothing to see here.
            return noChange()
        }


        val originalByActualParams: Map<IrValueParameter, IrValueParameter>

        val actualConstructor = originalConstructor.deepCopyWith(remapReferences = false) {
            name = Name.identifier("$")
            visibility = DescriptorVisibilities.PRIVATE
            origin = IrDartDeclarationOrigin.FACTORY_REDIRECT
        }.apply {
            origin = IrDartDeclarationOrigin.FACTORY_REDIRECT_ACTUAL
            originalByActualParams = originalConstructor.valueParameters
                .associateWith {
                    it.deepCopyWith {
                        when (val itsOrigin = it.origin) {
                            is IrDartDeclarationOrigin.WAS_COMPLEX_PARAM -> {
                                this.origin = IrDartDeclarationOrigin.FACTORY_REDIRECT_ACTUAL_PARAM
                                type = itsOrigin.originalType
                            }
                            else -> {
                                origin = itsOrigin
                                type = it.type
                            }
                        }
                    }.apply {
                        if (it.wasComplex) {
                            defaultValue = null
                        }
                    }
                }

            valueParameters = originalByActualParams.values.toList()

            (body as IrBlockBody).apply {
                statements.apply {
                    removeAll(initializersForComplexParams)

                    replaceAll {
                        if (it is IrDelegatingConstructorCall) {
                            superCall.also {
                                transformChildrenVoid(
                                    object : IrCustomElementTransformerVoid() {
                                        override fun visitExpression(expression: IrExpression): IrExpression {
                                            expression.transformChildrenVoid()

                                            if (expression is IrGetValue) {
                                                return originalByActualParams[expression.symbol.owner]
                                                    ?.symbol
                                                    ?.let { newSymbol ->
                                                        IrGetValueImpl(
                                                            UNDEFINED_OFFSET,
                                                            UNDEFINED_OFFSET,
                                                            symbol = newSymbol,
                                                        ).copyAttributes(expression)
                                                    } ?: expression
                                            }

                                            return expression
                                        }
                                    }
                                )
                            }
                        } else {
                            it
                        }
                    }
                }
            }

            // Unmark property parameters as to be initialized in the field initializer list, since a factory
            // doesn't support that.
            valueParameters
                .filter { it.origin == IrDartDeclarationOrigin.FACTORY_REDIRECT_ACTUAL_PARAM }
                .forEach { param ->
                    param.correspondingProperty?.let { prop ->
                        if (prop.isInitializedInFieldInitializerList) {
                            propertiesInitializedInFieldInitializerList.remove(prop.attributeOwner())
                        }
                    }
                }
        }


        val factoryConstructor = originalConstructor.deepCopyWith {
            origin = IrDartDeclarationOrigin.FACTORY_REDIRECT
        }.apply {
            valueParameters = originalConstructor.valueParameters.copy(parent = this@apply)

            body = irFactory.createBlockBody(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                body!!.initializersForComplexParams +
                        buildStatement(symbol) {
                            irReturn(
                                irCallConstructor(actualConstructor.symbol, emptyList()).apply {
                                    actualConstructor.valueParameters.onEach {
                                        putArgument(it, irGet(it))
                                    }
                                }
                            )
                        }
            ).apply {
                statements.removeIf { it is IrDelegatingConstructorCall }

                // Setters for complex property parameters reference the property now instead of the parameter, reverse
                // that.
                statements.forEach { statement ->
                    if (statement.isInitializerForComplexParameter) {
                        statement.replaceExpressions { exp ->
                            if (exp !is IrGetField) return@replaceExpressions exp

                            val originalParameterReference = parameterPropertyReferencesInParameterDefaultValue
                                .firstOrNull { it.attributeOwnerId == exp.attributeOwnerId }
                                ?: return@replaceExpressions exp

                            IrGetValueImpl(
                                UNDEFINED_OFFSET,
                                UNDEFINED_OFFSET,
                                originalParameterReference.symbol,
                            ).copyAttributes(originalParameterReference)
                        }
                    }
                }
            }
        }

        return replaceWith(actualConstructor) and add(factoryConstructor)
    }

    private val IrBody.initializersForComplexParams: List<IrStatement>
        get() = statements.filter { it.isInitializerForComplexParameter }
}