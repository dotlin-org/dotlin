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

package org.dotlin.compiler.backend.steps.ir2ast.lower

import org.dotlin.compiler.backend.steps.ir2ast.ir.buildStatement
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.DefaultMapping
import org.jetbrains.kotlin.backend.common.ir.Ir
import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.serialization.signature.IdSignatureDescriptor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.AbstractJsDescriptorMangler
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol
import org.jetbrains.kotlin.ir.types.IrDynamicType
import org.jetbrains.kotlin.ir.types.IrTypeSystemContext
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.types.impl.IrDynamicTypeImpl
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.types.Variance

// TODO: JS reference
object DartManglerDesc : AbstractJsDescriptorMangler()

class DartLoweringContext(
    override val configuration: CompilerConfiguration,
    irModuleFragment: IrModuleFragment
) : CommonBackendContext {
    override val builtIns = irModuleFragment.descriptor.builtIns
    override var inVerbosePhase = false
    override val internalPackageFqn = FqName("kotlin.dart")
    override val irBuiltIns = irModuleFragment.irBuiltins
    override val irFactory = IrFactoryImpl
    override val mapping = DefaultMapping()
    override val scriptMode = false

    val dynamicType: IrDynamicType = IrDynamicTypeImpl(null, emptyList(), Variance.INVARIANT)

    override val ir: Ir<CommonBackendContext> = object : Ir<DartLoweringContext>(this, irModuleFragment) {
        override val symbols = object : Symbols<DartLoweringContext>(
            this@DartLoweringContext,
            this@DartLoweringContext.irBuiltIns,
            SymbolTable(IdSignatureDescriptor(DartManglerDesc), irFactory)
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

        override fun getSharedValue(sharedVariableSymbol: IrVariableSymbol, originalGet: IrGetValue): IrExpression {
            TODO("Not yet implemented")
        }

        override fun setSharedValue(sharedVariableSymbol: IrVariableSymbol, originalSet: IrSetValue): IrExpression {
            TODO("Not yet implemented")
        }

    }

    override val typeSystem: IrTypeSystemContext = IrTypeSystemContextImpl(irBuiltIns)

    override fun log(message: () -> String) = print(message())

    override fun report(element: IrElement?, irFile: IrFile?, message: String, isError: Boolean) {
        print("[$irFile] $element: $message")
    }

    inline fun <T : IrElement> buildStatement(
        symbol: IrSymbol,
        origin: IrStatementOrigin? = null,
        builder: IrSingleStatementBuilder.() -> T
    ) = createIrBuilder(symbol, UNDEFINED_OFFSET, UNDEFINED_OFFSET).buildStatement(origin, builder)
}