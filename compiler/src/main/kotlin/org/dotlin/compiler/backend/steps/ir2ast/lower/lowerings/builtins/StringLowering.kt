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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.builtins

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.name.Name

// TODO: Just rewrite at stdlib source and remove this lowering
class StringLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || !declaration.defaultType.isString()) return noChange()

        val stringType = context.irBuiltIns.stringType

        declaration.also { stringClass ->
            stringClass.createExtensionMethodFrom("plus", isOperator = false) { method, thisReceiver ->
                val otherParam = method.valueParameters.first()

                irCallOp(
                    callee = stringClass.methodWithName("plus").symbol,
                    type = stringType,
                    dispatchReceiver = irGet(thisReceiver),
                    argument = irIfNull(
                        type = stringType,
                        subject = irGet(otherParam),
                        thenPart = IrConstImpl(
                            UNDEFINED_OFFSET,
                            UNDEFINED_OFFSET,
                            type = stringType,
                            kind = IrConstKind.String,
                            value = "null"
                        ),
                        elsePart = irCall(
                            context.irBuiltIns.anyClass.owner.methodWithName("toString")
                        ).apply {
                            dispatchReceiver = irGet(otherParam)
                        }
                    ),
                    origin = IrStatementOrigin.PLUS
                )
            }

            stringClass.createExtensionMethodFrom("subSequence") { method, thisReceiver ->
                val (startIndexParam, endIndexParam) = method.valueParameters

                method.returnType = stringType

                // TODO: Reference actual Dart extension "substring" if ever added to our stdlib

                // Create stub for referencing "substring" method.
                val substringStub = context.irFactory.buildFun {
                    origin = IrDartDeclarationOrigin.EXTERNAL_DART_REFERENCE
                    name = Name.identifier("substring")
                    visibility = DescriptorVisibilities.PUBLIC
                    modality = Modality.FINAL
                    returnType = stringType
                }.apply {
                    dispatchReceiverParameter = stringClass.thisReceiver!!

                    valueParameters = method.valueParameters.copy()
                }

                irCall(
                    substringStub,
                    receiver = irGet(thisReceiver),
                    valueArguments = arrayOf(irGet(startIndexParam), irGet(endIndexParam))
                )
            }

            stringClass.createExtensionMethodFrom("compareTo") { method, thisReceiver ->
                val otherParam = method.valueParameters.first()

                fun irGetLength(variable: IrValueDeclaration) =
                    irGet(context.irBuiltIns.intType, irGet(variable), stringClass.getterWithName("length").symbol)

                irCallOp(
                    callee = context.irBuiltIns.intClass.owner.methodWithName("minus").symbol,
                    type = context.irBuiltIns.intType,
                    dispatchReceiver = irGetLength(thisReceiver),
                    argument = irGetLength(otherParam),
                    origin = IrStatementOrigin.MINUS
                )
            }
        }

        // Remove the original class.
        return just { remove() }
    }

    private fun IrDeclarationContainer.createExtensionMethodFrom(
        methodName: String,
        isOperator: Boolean? = null,
        builder: IrSingleStatementBuilder.(method: IrSimpleFunction, thisReceiver: IrValueParameter) -> IrExpression,
    ) {
        val method = methodWithName(methodName)

        val newMethod = context.irFactory.buildFunFrom(method) {
            isOperator?.let { this@buildFunFrom.isOperator = isOperator }
        }.apply {
            extensionReceiverParameter = method.dispatchReceiverParameter?.copy()

            overriddenSymbols = emptyList()

            val irBuilder = context.createIrBuilder(symbol)

            body = irBuilder.irExprBody(irBuilder.buildStatement {
                builder(this, this@apply, extensionReceiverParameter!!)
            })
        }

        method.file.apply {
            remap(method to newMethod)
            addChild(newMethod)
        }
    }
}