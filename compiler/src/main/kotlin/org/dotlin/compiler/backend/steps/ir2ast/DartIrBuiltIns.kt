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
import org.dotlin.compiler.backend.kotlin
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeAliasSymbol
import org.jetbrains.kotlin.ir.util.referenceFunction
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter

// TODO: Make lazy
class DartIrBuiltIns(context: DartLoweringContext) {
    private val builtInsModule = context.irModuleFragment.descriptor.builtIns.builtInsModule
    private val symbolTable = context.symbolTable

    val dotlin = Dotlin(this)
    val kotlin = Kotlin(this)

    val identical = functionSymbolAt(dart.core.identical)
    val iterator = classSymbolAt(dart.core.Iterator)

    val unsupportedError = classSymbolAt(dart.core.UnsupportedError)

    val immutableListView = classSymbolAt(dart.collection.ImmutableListView)
    val immutableSetView = classSymbolAt(dart.collection.ImmutableSetView)

    class Dotlin(builtIns: DartIrBuiltIns) {
        val const = builtIns.classSymbolAt(dotlin.const)
        val dart = builtIns.functionSymbolAt(dotlin.dart)

        // Reflect
        val kProperty0Impl = builtIns.classSymbolAt(dotlin.reflect.KProperty0Impl)
        val kMutableProperty0Impl = builtIns.classSymbolAt(dotlin.reflect.KMutableProperty0Impl)
        val kProperty1Impl = builtIns.classSymbolAt(dotlin.reflect.KProperty1Impl)
        val kMutableProperty1Impl = builtIns.classSymbolAt(dotlin.reflect.KMutableProperty1Impl)
        val kProperty2Impl = builtIns.classSymbolAt(dotlin.reflect.KProperty2Impl)
        val kMutableProperty2Impl = builtIns.classSymbolAt(dotlin.reflect.KMutableProperty2Impl)

        // Interop
        val returnClass = builtIns.classSymbolAt(dotlin.intrinsics.`$Return`)
        val dotlinExternal = builtIns.classSymbolAt(dotlin.intrinsics.DotlinExternal)
        val specialInheritedType = builtIns.classSymbolAt(dotlin.intrinsics.SpecialInheritedType)

        val anyCollection = builtIns.typeAliasSymbolAt(dotlin.intrinsics.AnyCollection)

        val isCollection = builtIns.functionSymbolAt(dotlin.intrinsics.isCollection)
        val isMutableCollection = builtIns.functionSymbolAt(dotlin.intrinsics.isMutableCollection)

        val isImmutableList = builtIns.functionSymbolAt(dotlin.intrinsics.isImmutableList)
        val isWriteableList = builtIns.functionSymbolAt(dotlin.intrinsics.isWriteableList)
        val isFixedSizeList = builtIns.functionSymbolAt(dotlin.intrinsics.isFixedSizeList)
        val isMutableList = builtIns.functionSymbolAt(dotlin.intrinsics.isMutableList)

        val isImmutableSet = builtIns.functionSymbolAt(dotlin.intrinsics.isImmutableSet)
        val isMutableSet = builtIns.functionSymbolAt(dotlin.intrinsics.isMutableSet)

        val isImmutableMap = builtIns.functionSymbolAt(dotlin.intrinsics.isImmutableMap)
        val isMutableMap = builtIns.functionSymbolAt(dotlin.intrinsics.isMutableMap)

        val immutableListMarker = builtIns.classSymbolAt(dotlin.intrinsics.ImmutableListMarker)
        val writeableListMarker = builtIns.classSymbolAt(dotlin.intrinsics.WriteableListMarker)
        val mutableListMarker = builtIns.classSymbolAt(dotlin.intrinsics.MutableListMarker)
        val fixedSizeListMarker = builtIns.classSymbolAt(dotlin.intrinsics.FixedSizeListMarker)

        val immutableSetMarker = builtIns.classSymbolAt(dotlin.intrinsics.ImmutableSetMarker)
        val mutableSetMarker = builtIns.classSymbolAt(dotlin.intrinsics.MutableSetMarker)

        val immutableMapMarker = builtIns.classSymbolAt(dotlin.intrinsics.ImmutableMapMarker)
        val mutableMapMarker = builtIns.classSymbolAt(dotlin.intrinsics.MutableMapMarker)
    }

    class Kotlin(builtIns: DartIrBuiltIns) {
        val writeableList = builtIns.classSymbolAt(kotlin.collections.WriteableList)
        val immutableList = builtIns.classSymbolAt(kotlin.collections.ImmutableList)
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