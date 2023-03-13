/*
 * Copyright 2023 Wilko Manger
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

@file:OptIn(ObsoleteDescriptorBasedAPI::class)

package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.descriptors.DartCodeValue
import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.descriptors.DartInteropDescriptor
import org.dotlin.compiler.backend.descriptors.DartValueParameterDescriptor
import org.dotlin.compiler.backend.steps.ir2ast.DotlinIrBuiltIns
import org.jetbrains.kotlin.backend.common.serialization.DescriptorByIdSignatureFinderImpl
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrErrorExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.linkage.IrProvider
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.util.referenceClassifier
import org.jetbrains.kotlin.psi2ir.generators.DeclarationStubGeneratorImpl
import org.jetbrains.kotlin.resolve.constants.*
import org.jetbrains.kotlin.resolve.descriptorUtil.module

/**
 * Providers IR elements from Dart descriptors.
 */
class DartIrProvider(
    private val module: ModuleDescriptor,
    symbolTable: SymbolTable,
    private val irBuiltIns: IrBuiltIns,
    private val dotlinIrBuiltIns: DotlinIrBuiltIns,
) : IrProvider {
    private val descriptorFinder = DescriptorByIdSignatureFinderImpl(
        module,
        DotlinDescriptorBasedMangler,
    )

    private val stubGenerator = DeclarationStubGeneratorImpl(
        module,
        symbolTable,
        irBuiltIns,
        descriptorFinder,
    )

    override fun getDeclaration(symbol: IrSymbol): IrDeclaration? {
        val descriptor = symbol.descriptor

        if (descriptor.module != module) return null

        if (descriptor !is DartDescriptor && descriptor !is DartInteropDescriptor) return null

        val declaration = stubGenerator.run {
            when (symbol) {
                is IrClassSymbol -> generateClassStub(descriptor as ClassDescriptor).apply {
                    // We need to reference classifiers used in super types ourselves, otherwise we still have
                    // unbound symbols that can be referenced.
                    // TODO: This might happen for all IrTypes, if so do this everywhere with a generic solution.
                    superTypes
                        .mapNotNull { it.originalKotlinType?.constructor?.declarationDescriptor }
                        .forEach { symbolTable.referenceClassifier(it) }
                }

                is IrPropertySymbol -> generatePropertyStub(descriptor as PropertyDescriptor)
                is IrConstructorSymbol -> generateConstructorStub(descriptor as ClassConstructorDescriptor)
                is IrFunctionSymbol -> generateFunctionStub(descriptor as FunctionDescriptor).apply {
                    patchDefaultValues()
                }

                is IrEnumEntrySymbol -> generateEnumEntryStub(descriptor as ClassDescriptor)
                is IrTypeAliasSymbol -> generateTypeAliasStub(descriptor as TypeAliasDescriptor)
                else -> error("Unexpected symbol type: ${symbol::class.simpleName} (sig: ${symbol.signature})")
            }
        }

        return declaration
    }

    private fun IrFunction.patchDefaultValues() {
        for (param in valueParameters) {
            val defaultValue = param.defaultValue
            if (defaultValue == null || defaultValue.expression !is IrErrorExpression) continue

            val descriptor = param.descriptor
            if (descriptor !is DartValueParameterDescriptor) continue

            val type = param.type

            param.defaultValue = IrExpressionBodyImpl(
                when (val constant = descriptor.compileTimeInitializer) {
                    // TODO: EnumValue
                    is NullValue, is BooleanValue, is IntValue, is DoubleValue, is StringValue -> {
                        constant.value.toIrConst(type)
                    }

                    is DartCodeValue -> IrCallImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                        // We purposefully set the type to that of the expression
                        // (instead of `dart(..)`s return type, `dynamic`), in case we need the more specific type.
                        type,
                        dotlinIrBuiltIns.dartFun,
                        typeArgumentsCount = 0,
                        valueArgumentsCount = 1,
                    ).apply {
                        putValueArgument(0, constant.code.toIrConst(irBuiltIns.stringType))
                    }

                    else -> error(
                        "Unexpected default value (${constant?.let { it::class.simpleName } ?: "null"}: $constant"
                    )
                }
            )
        }
    }
}