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

package org.dotlin.compiler.backend.steps.ir2ast

import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.jetbrains.kotlin.backend.common.ir.createParameterDeclarations
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrTypeParameterSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.impl.IrUninitializedType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class DartIrBuiltIns(private val context: DartLoweringContext) {
    private val builtInsModule = context.irModuleFragment.descriptor.builtIns.builtInsModule
    private val symbolTable = context.symbolTable
    private val irFactory = context.irFactory
    private val irBuiltIns = context.irBuiltIns

    val identical = functionSymbolAt("dart.core", "identical") { parameters, _ ->
        returnType = irBuiltIns.booleanType

        parameters.apply {
            add {
                name = Name.identifier("a")
                type = irBuiltIns.anyType.makeNullable()
            }
            add {
                name = Name.identifier("b")
                type = irBuiltIns.anyType.makeNullable()
            }
        }
    }

    //val iterator = symbolAt<IrClassSymbol>("dart.core", "Iterator")

    val voidType = IrVoidType

    val dotlin = Dotlin()

    inner class Dotlin {
        val dart = functionSymbolAt("dotlin", "dart") { parameters, _ ->
            returnType = context.dynamicType

            parameters.add {
                name = Name.identifier("code")
                type = irBuiltIns.stringType
            }
        }

        val dartConst = classSymbolAt("dotlin", "DartConst") { _, _ ->
            kind = ClassKind.ANNOTATION_CLASS
        }

        val returnClass = classSymbolAt("dotlin", "\$Return") { typeParameters, members ->
            val typeParameter = typeParameters.add {
                name = Name.identifier("T")
            }

            members.apply {
                lateinit var parameter: IrValueParameter

                addConstructor { parameters, _ ->
                    isPrimary = true

                    parameter = parameters.add {
                        name = Name.identifier("value")
                        type = typeParameter.defaultType
                    }
                }

                add {
                    name = Name.identifier("value")
                }.apply {
                    val property = this

                    getter = irFactory.buildFun {
                        name = Name.special("<get-value>")
                        returnType = typeParameter.defaultType
                        origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
                    }.apply {
                        correspondingPropertySymbol = property.symbol
                    }

                    backingField = irFactory.buildField {
                        name = Name.special("<field-value>")
                        type = typeParameter.defaultType
                    }.apply {
                        initializer = IrExpressionBodyImpl(
                            context.buildStatement(symbol) {
                                IrGetValueImpl(
                                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                                    parameter.symbol,
                                    origin = IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    private inline fun <reified S : IrSymbol> symbolAt(
        packageName: String,
        memberName: String,
        createStub: (S, packageFqName: FqName, identifier: Name) -> IrSymbolOwner
    ): S {
        val packageFqName = FqName(packageName)
        val memberIdentifier = Name.identifier(memberName)
        val descriptor = builtInsModule.getPackage(packageFqName)
            .memberScope
            .getContributedDescriptors {
                it == memberIdentifier
            }.firstOrNull() ?: error("Classifier not found: $packageName.$memberName")

        return symbolTable.let {
            when (S::class) {
                IrClassSymbol::class -> it.referenceClass(descriptor as ClassDescriptor) as S
                IrSimpleFunctionSymbol::class -> it.referenceSimpleFunction(descriptor as FunctionDescriptor) as S
                else -> error("Unsupported symbol type: ${S::class.simpleName}")
            }
        }.also {
            if (!it.isBound) {
                createStub(it, packageFqName, memberIdentifier)
            }
        }
    }

    private fun functionSymbolAt(
        packageName: String,
        memberName: String,
        buildStub: IrFunctionStubBuilder
    ): IrSimpleFunctionSymbol = symbolAt(packageName, memberName) { symbol, packageFqName, identifier ->
        buildFunStub(
            init = { v, t ->
                name = identifier
                buildStub(this, v, t)
            },
            create = {
                irFactory.createFunction(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    origin,
                    symbol = symbol,
                    name,
                    visibility,
                    modality,
                    returnType = returnType.also { require(it !is IrUninitializedType) },
                    isInline,
                    isExternal,
                    isTailrec,
                    isSuspend,
                    isOperator,
                    isInfix,
                    isExpect,
                    isFakeOverride,
                ).apply {
                    setExternalParent(packageFqName)
                }
            }
        )
    }


    private fun MutableList<IrValueParameter>.add(block: IrValueParameterBuilder.() -> Unit): IrValueParameter {
        val builder = IrValueParameterBuilder()
            .applyDefaults()
            .also(block)

        return irFactory.createValueParameter(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            builder.origin,
            symbol = IrValueParameterSymbolImpl(),
            builder.name,
            index = size,
            builder.type,
            builder.varargElementType,
            builder.isCrossInline,
            builder.isNoinline,
            builder.isHidden,
            builder.isAssignable
        ).also {
            add(it)
        }
    }

    @JvmName("addIrTypeParameter")
    private fun MutableList<IrTypeParameter>.add(block: IrTypeParameterBuilder.() -> Unit): IrTypeParameter {
        val builder = IrTypeParameterBuilder()
            .applyDefaults()
            .also(block)

        return irFactory.createTypeParameter(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            builder.origin,
            symbol = IrTypeParameterSymbolImpl(),
            builder.name,
            index = size,
            builder.isReified,
            builder.variance
        ).also {
            add(it)
        }
    }

    private fun classSymbolAt(
        packageName: String,
        memberName: String,
        buildStub: IrClassStubBuilder
    ): IrClassSymbol = symbolAt(packageName, memberName) { symbol, packageFqName, identifier ->
        val typeParameters = mutableListOf<IrTypeParameter>()
        val members = mutableListOf<IrDeclaration>()
        val builder = IrClassBuilder().apply {
            applyDefaults()
            name = identifier
            isExternal = true
        }.also {
            buildStub(it, typeParameters, members)
        }

        irFactory.createClass(
            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            builder.origin,
            symbol = symbol,
            builder.name,
            builder.kind,
            builder.visibility,
            builder.modality,
            builder.isCompanion,
            builder.isInner,
            builder.isData,
            builder.isExternal,
            builder.isInline,
            builder.isExpect,
            builder.isFun,
            SourceElement.NO_SOURCE
        ).apply {
            setExternalParent(packageFqName)
            createParameterDeclarations()

            typeParameters.addAll(
                typeParameters.onEach {
                    it.parent = this
                }
            )

            declarations.addAll(
                members.onEach {
                    it.parent = this

                    when (it) {
                        is IrProperty -> {
                            it.backingField?.parent = this
                            it.getter?.parent = this
                            it.setter?.parent = this
                        }
                        is IrConstructor -> it.returnType = defaultType
                    }
                }
            )
        }
    }

    private fun MutableList<IrDeclaration>.addConstructor(block: IrFunctionStubBuilder): IrConstructor = buildFunStub(
        init = { v, t ->
            name = Name.special("<init>")
            block(this, v, t)
        },
        create = {
            irFactory.createConstructor(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                origin,
                symbol = IrConstructorSymbolImpl(),
                name,
                visibility,
                returnType,
                isInline,
                isExternal,
                isPrimary,
                isExpect,
                containerSource = null
            )
        }
    ).also {
        add(it)
    }

    private fun MutableList<IrDeclaration>.add(block: IrPropertyBuilder.() -> Unit): IrProperty {
        return irFactory.buildProperty {
            applyDefaults()
            block(this)
        }.also {
            add(it)
        }
    }

    private fun <T : IrDeclarationBuilder> T.applyDefaults() = apply {
        origin = IrDeclarationOrigin.IR_BUILTINS_STUB
    }

    private fun <F : IrFunction> buildFunStub(
        init: IrFunctionStubBuilder,
        create: IrFunctionBuilder.() -> F
    ): F {
        val builder = IrFunctionBuilder().apply {
            applyDefaults()
            isExternal = true
        }
        val valueParameters = mutableListOf<IrValueParameter>()
        val typeParameters = mutableListOf<IrTypeParameter>()

        init(builder, valueParameters, typeParameters)

        return create(builder).also { function ->
            function.valueParameters = valueParameters.onEach {
                it.parent = function
            }

            function.typeParameters = typeParameters.onEach {
                it.parent = function
            }
        }
    }

    private fun IrDeclarationWithName.setExternalParent(packageName: FqName) {
        parent = symbolTable
            .declareExternalPackageFragmentIfNotExists(
                builtInsModule.getPackage(packageName)
                    .fragments
                    .single {
                        name in it.getMemberScope().let { scope ->
                            when (this) {
                                is IrFunction -> scope.getFunctionNames()
                                is IrClass -> scope.getClassifierNames() ?: emptySet()
                                else -> {
                                    throw UnsupportedOperationException("Unsupported type: ${this::class.simpleName}")
                                }
                            }
                        }
                    }
            )
    }
}

private typealias IrFunctionStubBuilder = IrFunctionBuilder.(
    valueParameters: MutableList<IrValueParameter>,
    typeParameters: MutableList<IrTypeParameter>
) -> Unit

private typealias IrClassStubBuilder = IrClassBuilder.(
    typeParameters: MutableList<IrTypeParameter>,
    members: MutableList<IrDeclaration>
) -> Unit

object IrVoidType : IrType {
    override val annotations = emptyList<IrConstructorCall>()

    override fun equals(other: Any?) = this === other

    override fun hashCode() = 0
}