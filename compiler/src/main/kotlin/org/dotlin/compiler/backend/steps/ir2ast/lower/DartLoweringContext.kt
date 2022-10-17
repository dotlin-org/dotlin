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

package org.dotlin.compiler.backend.steps.ir2ast.lower

import org.dotlin.compiler.backend.*
import org.dotlin.compiler.backend.steps.ir2ast.DartIrBuiltIns
import org.dotlin.compiler.backend.steps.ir2ast.attributes.IrAttributes
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartNumberPrimitive
import org.dotlin.compiler.backend.util.sentenceCase
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.DefaultMapping
import org.jetbrains.kotlin.backend.common.ir.*
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildConstructor
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import java.nio.file.Path

class DartLoweringContext(
    override val configuration: CompilerConfiguration,
    override val symbolTable: SymbolTable,
    override val bindingContext: BindingContext,
    val irModuleFragment: IrModuleFragment,
    override val dartNameGenerator: DartNameGenerator,
    private val irAttributes: IrAttributes,
    override val sourceRoot: Path,
    override val dartPackage: DartPackage
) : IrContext(), CommonBackendContext, IrAttributes by irAttributes {
    override val builtIns = irModuleFragment.descriptor.builtIns
    override var inVerbosePhase = false
    override val internalPackageFqn = FqName("kotlin.dart")
    override val irBuiltIns = irModuleFragment.irBuiltins
    override val irFactory = IrFactoryImpl
    override val mapping = DefaultMapping()
    override val scriptMode = false
    override val typeSystem: IrTypeSystemContext = IrTypeSystemContextImpl(irBuiltIns)

    @PublishedApi
    internal val irBuilders = mutableMapOf<IrSymbol, IrBuilderWithScope>()

    val dartBuiltIns = DartIrBuiltIns(this)

    override val ir: Ir<CommonBackendContext> = object : Ir<DartLoweringContext>(this, irModuleFragment) {
        override val symbols = object : Symbols<DartLoweringContext>(
            this@DartLoweringContext,
            irBuiltIns,
            symbolTable,
        ) {
            override val coroutineContextGetter: IrSimpleFunctionSymbol
                get() = TODO("Not yet implemented")
            override val coroutineGetContext: IrSimpleFunctionSymbol
                get() = TODO("Not yet implemented")
            override val coroutineImpl: IrClassSymbol
                get() = TODO("Not yet implemented")
            override val coroutineSuspendedGetter: IrSimpleFunctionSymbol
                get() = TODO("Not yet implemented")
            override val defaultConstructorMarker: IrClassSymbol
                get() = TODO("Not yet implemented")
            override val functionAdapter: IrClassSymbol
                get() = TODO("Not yet implemented")
            override val getContinuation: IrSimpleFunctionSymbol
                get() = TODO("Not yet implemented")
            override val returnIfSuspended: IrSimpleFunctionSymbol
                get() = TODO("Not yet implemented")
            override val stringBuilder: IrClassSymbol
                get() = TODO("Not yet implemented")
            override val suspendCoroutineUninterceptedOrReturn: IrSimpleFunctionSymbol
                get() = TODO("Not yet implemented")
            override val throwKotlinNothingValueException: IrSimpleFunctionSymbol
                get() = TODO("Not yet implemented")
            override val throwNullPointerException: IrSimpleFunctionSymbol
                get() = TODO("Not yet implemented")
            override val throwTypeCastException: IrSimpleFunctionSymbol
                get() = TODO("Not yet implemented")
            override val throwUninitializedPropertyAccessException: IrSimpleFunctionSymbol
                get() = TODO("Not yet implemented")
        }
    }

    override val sharedVariablesManager = object : SharedVariablesManager {
        override fun declareSharedVariable(originalDeclaration: IrVariable) = TODO()

        override fun defineSharedValue(
            originalDeclaration: IrVariable,
            sharedVariableDeclaration: IrVariable
        ): IrStatement {
            TODO("Not yet implemented")
        }

        override fun getSharedValue(sharedVariableSymbol: IrValueSymbol, originalGet: IrGetValue): IrExpression {
            TODO("Not yet implemented")
        }

        override fun setSharedValue(sharedVariableSymbol: IrValueSymbol, originalSet: IrSetValue): IrExpression {
            TODO("Not yet implemented")
        }

    }

    override fun log(message: () -> String) = print(message())

    override fun report(element: IrElement?, irFile: IrFile?, message: String, isError: Boolean) {
        print("[$irFile] $element: $message")
    }

    inline fun <T : IrElement> buildStatement(
        symbol: IrSymbol,
        origin: IrStatementOrigin? = null,
        builder: IrSingleStatementBuilder.() -> T
    ) = irBuilders.computeIfAbsent(symbol) { createIrBuilder(symbol, UNDEFINED_OFFSET, UNDEFINED_OFFSET) }
        .buildStatement(origin, builder)

    private val extensionContainers = mutableMapOf<IrFile, MutableMap<String, IrClass>>()

    /**
     * Returns the container this extension should go to. The container lives in the file of the extension. If no
     * container exists, one is created. The created container is _not_ added to the file, the [ExtensionsLowering]
     * should do that.
     *
     * Returns `null` if the declaration is not an extension.
     */
    val IrDeclaration.extensionContainer: IrClass?
        get() {
            val receiver = extensionReceiverParameterOrNull ?: return null
            val receiverType = receiver.type
            val receiverTypeParameters = receiverType.typeParametersOrSelf

            val dartExtensionName = dartExtensionName
            val containerName = dartExtensionName ?: run {
                val (file, mainName) = when (val classifier = receiverType.classifierOrNull?.owner) {
                    is IrClass -> classifier.file to classifier.defaultType.let {
                        // TODO: Not necessary anymore: Only Int and Double can be used.
                        when {
                            it.isDartNumberPrimitive() -> classifier.name.identifier
                            else -> classifier.dartName.escapedValue()
                        }
                    }
                    is IrTypeParameter -> classifier.file to classifier.dartNameValueWith(superTypes = true)
                    else -> throw UnsupportedOperationException("Cannot handle extension for $this yet")
                }
                val prefix = '$'
                val packagePrefix = when {
                    file != this.file -> file.fqName.pathSegments()
                        .map { it.identifier }
                        .joinToString("") { it.sentenceCase() }
                    else -> ""
                }
                val typeArguments = when (receiverType) {
                    is IrSimpleType -> receiverType.arguments
                        .mapNotNull { it.typeOrNull?.classOrNull?.owner?.dartName?.escapedValue() }
                        .joinToString("") { it.sentenceCase() }
                    else -> ""
                }
                val suffix = "Extensions"

                "$prefix$packagePrefix$mainName$typeArguments$suffix"
            }

            val file = file

            return extensionContainers
                .getOrPut(file) { mutableMapOf() }
                .getOrPut(containerName) {
                    irFactory.buildClass {
                        origin = IrDartDeclarationOrigin.EXTENSION(hasGeneratedName = dartExtensionName == null)
                        name = Name.identifier(containerName)
                    }.apply {
                        parent = file

                        copyTypeParameters(receiverTypeParameters)
                        createParameterDeclarations()

                        addChild(
                            irFactory.buildConstructor {
                                visibility = DescriptorVisibilities.PUBLIC
                                returnType = defaultType
                                isPrimary = true
                            }.apply {
                                val constructor = this

                                valueParameters = listOf(
                                    irFactory.createValueParameter(
                                        UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                                        origin = IrDeclarationOrigin.DEFINED,
                                        symbol = IrValueParameterSymbolImpl(),
                                        name = Name.identifier("value"),
                                        index = 0,
                                        type = receiverType,
                                        varargElementType = null,
                                        isCrossinline = false,
                                        isNoinline = false,
                                        isHidden = false,
                                        isAssignable = false
                                    ).apply {
                                        parent = constructor
                                    }
                                )
                            }
                        )
                    }
                }
        }

    private fun IrProperty.createDefaultGetterOrSetter(type: IrType, isGetter: Boolean): IrSimpleFunction {
        val property = this
        return irFactory.buildFun {
            name = when {
                isGetter -> Name.special("<get-${property.name}>")
                else -> Name.special("<set-${property.name}>")
            }
            returnType = when {
                isGetter -> type
                else -> irBuiltIns.unitType
            }
            origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
            visibility = property.visibility
        }.apply {
            correspondingPropertySymbol = property.symbol

            if (!isGetter) {
                valueParameters = listOf(
                    buildValueParameter(this) {
                        name = Name.identifier("value")
                        this.type = type
                    }
                )
            }

            val parentClass = property.parentClassOrNull
            val thisReceiver = parentClass?.thisReceiver

            parent = parentClass ?: property.parent

            if (parentClass != null) {
                createDispatchReceiverParameter()
            }

            body = IrExpressionBodyImpl(
                buildStatement(symbol) {
                    val getThis = thisReceiver?.let { irGet(it) }

                    when {
                        isGetter -> irGetField(
                            receiver = getThis,
                            field = backingField!!
                        )
                        else -> irSetField(
                            receiver = getThis,
                            field = backingField!!,
                            value = irGet(valueParameters[0])
                        )
                    }
                }
            )
        }.also {
            when {
                isGetter -> getter = it
                else -> setter = it
            }
        }
    }

    /**
     * Create backing field first.
     */
    fun IrProperty.createDefaultGetter(type: IrType) = createDefaultGetterOrSetter(type, isGetter = true)

    /**
     * Create backing field first.
     */
    fun IrProperty.createDefaultSetter(type: IrType) = createDefaultGetterOrSetter(type, isGetter = false)

    fun wrapInAnonymousFunctionInvocation(
        exp: IrExpression,
        container: IrDeclaration,
        statements: (IrSimpleFunction) -> List<IrStatement> = { listOf(exp) }
    ): IrCall {
        val anonymousFunction = irFactory.buildFun {
            name = Name.special("<anonymous>")
            returnType = exp.type
        }.apply {
            parent = container as IrDeclarationParent

            body = IrBlockBodyImpl(
                UNDEFINED_OFFSET,
                UNDEFINED_OFFSET,
                statements(this)
            )
        }

        return IrFunctionExpressionImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            type = exp.type,
            function = anonymousFunction,
            origin = IrStatementOrigin.INVOKE
        ).irCall()
    }

    fun IrFunctionExpression.irCall(vararg arguments: IrExpression): IrCall {
        val invokeMethod = irFactory.buildFun {
            name = Name.identifier("invoke")
            isOperator = true
            returnType = function.returnType
        }.apply {
            parent = function
            dispatchReceiverParameter = irBuiltIns.functionN(0).thisReceiver

            valueParameters = arguments.indices.map { i ->
                buildValueParameter(this) {
                    name = Name.identifier("p$i")
                    type = arguments[i].type
                    index = i
                }
            }
        }

        return IrCallImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            type = function.returnType,
            symbol = invokeMethod.symbol,
            typeArgumentsCount = 0,
            valueArgumentsCount = arguments.size,
            origin = IrStatementOrigin.INVOKE,
        ).apply {
            dispatchReceiver = this@irCall

            arguments.forEachIndexed { i, arg -> putValueArgument(i, arg) }
        }
    }

    fun IrFunction.remapLocalPropertyAccessors(
        getter: IrSingleStatementBuilder.(IrCall) -> IrExpression,
        setter: IrSingleStatementBuilder.(IrCall) -> IrExpression
    ) {
        transformExpressions(initialParent = this) { exp, _ ->
            when (exp) {
                is IrCall -> when (exp.origin) {
                    IrStatementOrigin.GET_LOCAL_PROPERTY -> buildStatement(symbol) {
                        getter(this, exp)
                    }
                    else -> when (exp.symbol.owner.origin) {
                        // Must be a 'set' in this case.
                        IrDeclarationOrigin.DELEGATED_PROPERTY_ACCESSOR -> buildStatement(symbol) {
                            setter(this, exp)
                        }
                        else -> exp
                    }
                }
                else -> exp
            }
        }
    }

    private fun DartIdentifier.escapedValue() = value.replace(".", "").sentenceCase()
}