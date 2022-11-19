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

import org.dotlin.compiler.backend.dart
import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeAliasSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.referenceFunction
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter

// TODO: Make lazy
@OptIn(ObsoleteDescriptorBasedAPI::class)
class DotlinIrBuiltIns(private val context: DotlinLoweringContext) {
    private val builtInsModule = context.irModuleFragment.descriptor.builtIns.builtInsModule
    private val symbolTable = context.symbolTable

    val dart = Dart(this)

    private val operatorsPackage = IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(
        builtInsModule,
        org.dotlin.compiler.backend.dotlin.intrinsics.operators.self
    )

    // Fake functions for certain operators.
    fun ifNull(type: IrType) = context.irFactory.buildFun {
        origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        name = Name.identifier("IF_NULL")
        returnType = type
    }.apply {
        val ifNull = this

        valueParameters = listOf(
            context.irFactory.createValueParameter(
                SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                origin = IrDeclarationOrigin.IR_BUILTINS_STUB,
                symbol = IrValueParameterSymbolImpl(),
                name = Name.identifier("p$0"),
                index = 0,
                type = context.irBuiltIns.anyType.makeNullable(),
                varargElementType = null,
                isCrossinline = false,
                isNoinline = false,
                isHidden = false,
                isAssignable = false
            ).apply {
                parent = ifNull
            }
        )

        parent = operatorsPackage
    }

    val const = classSymbolAt(dotlin.const)
    val dartFun = functionSymbolAt(dotlin.dart)

    val dartGetter = classSymbolAt(dotlin.DartGetter)
    val dartExtension = classSymbolAt(dotlin.DartExtension)

    // Reflect
    val kProperty0Impl = classSymbolAt(dotlin.reflect.KProperty0Impl)
    val kMutableProperty0Impl = classSymbolAt(dotlin.reflect.KMutableProperty0Impl)
    val kProperty1Impl = classSymbolAt(dotlin.reflect.KProperty1Impl)
    val kMutableProperty1Impl = classSymbolAt(dotlin.reflect.KMutableProperty1Impl)
    val kProperty2Impl = classSymbolAt(dotlin.reflect.KProperty2Impl)
    val kMutableProperty2Impl = classSymbolAt(dotlin.reflect.KMutableProperty2Impl)

    // Interop
    val returnClass = classSymbolAt(dotlin.intrinsics.`$Return`)
    val dotlinExternal = classSymbolAt(dotlin.intrinsics.DotlinExternal)
    val specialInheritedType = classSymbolAt(dotlin.intrinsics.SpecialInheritedType)

    val anyCollection = typeAliasSymbolAt(dotlin.intrinsics.AnyCollection)

    val isCollection = functionSymbolAt(dotlin.intrinsics.isCollection)
    val isMutableCollection = functionSymbolAt(dotlin.intrinsics.isMutableCollection)

    val isImmutableList = functionSymbolAt(dotlin.intrinsics.isImmutableList)
    val isWriteableList = functionSymbolAt(dotlin.intrinsics.isWriteableList)
    val isFixedSizeList = functionSymbolAt(dotlin.intrinsics.isFixedSizeList)
    val isMutableList = functionSymbolAt(dotlin.intrinsics.isMutableList)

    val isImmutableSet = functionSymbolAt(dotlin.intrinsics.isImmutableSet)
    val isMutableSet = functionSymbolAt(dotlin.intrinsics.isMutableSet)

    val isImmutableMap = functionSymbolAt(dotlin.intrinsics.isImmutableMap)
    val isMutableMap = functionSymbolAt(dotlin.intrinsics.isMutableMap)

    val immutableListMarker = classSymbolAt(dotlin.intrinsics.ImmutableListMarker)
    val writeableListMarker = classSymbolAt(dotlin.intrinsics.WriteableListMarker)
    val mutableListMarker = classSymbolAt(dotlin.intrinsics.MutableListMarker)
    val fixedSizeListMarker = classSymbolAt(dotlin.intrinsics.FixedSizeListMarker)

    val immutableSetMarker = classSymbolAt(dotlin.intrinsics.ImmutableSetMarker)
    val mutableSetMarker = classSymbolAt(dotlin.intrinsics.MutableSetMarker)

    val immutableMapMarker = classSymbolAt(dotlin.intrinsics.ImmutableMapMarker)
    val mutableMapMarker = classSymbolAt(dotlin.intrinsics.MutableMapMarker)

    class Dart(builtIns: DotlinIrBuiltIns) {
        val identical = builtIns.functionSymbolAt(dart.core.identical)

        val unsupportedError = builtIns.classSymbolAt(dart.core.UnsupportedError)

        val immutableListView = builtIns.classSymbolAt(dart.collection.ImmutableListView)
        val immutableSetView = builtIns.classSymbolAt(dart.collection.ImmutableSetView)
    }

    private inline fun <reified S : IrSymbol> symbolAt(name: FqName): S =
        symbolsAt<S>(name).firstOrNull() ?: error("Symbol not found: $name")

    private inline fun <reified S : IrSymbol> symbolsAt(name: FqName): List<S> {
        val packageFqName = name.parent()
        val memberIdentifier = name.shortName()
        val descriptors = builtInsModule.getPackage(packageFqName)
            .memberScope
            .getContributedDescriptors(
                kindFilter = when (S::class) {
                    IrClassSymbol::class -> DescriptorKindFilter.CLASSIFIERS
                    IrSimpleFunctionSymbol::class -> DescriptorKindFilter.FUNCTIONS
                    IrTypeAliasSymbol::class -> DescriptorKindFilter.TYPE_ALIASES
                    else -> error("Unsupported symbol type: ${S::class.simpleName}")
                },
                nameFilter = { it == memberIdentifier }
            )

        return symbolTable.run {
            when (S::class) {
                IrClassSymbol::class -> descriptors.map { referenceClass(it as ClassDescriptor) }
                IrSimpleFunctionSymbol::class -> descriptors.map { referenceFunction(it as FunctionDescriptor) }
                IrTypeAliasSymbol::class -> descriptors.map { referenceTypeAlias(it as TypeAliasDescriptor) }
                else -> error("Unsupported symbol type: ${S::class.simpleName}")
            } as List<S>
        }
    }

    private fun classSymbolsAt(name: FqName): List<IrClassSymbol> = symbolsAt(name)
    private fun classSymbolAt(name: FqName): IrClassSymbol = symbolAt(name)

    private fun functionSymbolsAt(name: FqName): List<IrSimpleFunctionSymbol> = symbolsAt(name)
    private fun functionSymbolAt(name: FqName): IrSimpleFunctionSymbol = symbolAt(name)

    private fun typeAliasSymbolsAt(name: FqName): List<IrTypeAliasSymbol> = symbolsAt(name)
    private fun typeAliasSymbolAt(name: FqName): IrTypeAliasSymbol = symbolAt(name)
}