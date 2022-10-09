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
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.referenceFunction
import org.jetbrains.kotlin.name.FqName

class DartIrBuiltIns(context: DartLoweringContext) {
    private val builtInsModule = context.irModuleFragment.descriptor.builtIns.builtInsModule
    private val symbolTable = context.symbolTable

    val dotlin = Dotlin(this)

    val identical = functionSymbolAt(dart.core.identical)
    val iterator = classSymbolAt(dart.core.Iterator)

    val unsupportedError = classSymbolAt(dart.core.UnsupportedError)

    class Dotlin(builtIns: DartIrBuiltIns) {
        val const = builtIns.classSymbolAt(dotlin.const)
        val dart = builtIns.functionSymbolAt(dotlin.dart)
        val returnClass = builtIns.classSymbolAt(dotlin.`$Return`)

        // Reflect
        val kProperty0Impl = builtIns.classSymbolAt(dotlin.reflect.KProperty0Impl)
        val kMutableProperty0Impl = builtIns.classSymbolAt(dotlin.reflect.KMutableProperty0Impl)
        val kProperty1Impl = builtIns.classSymbolAt(dotlin.reflect.KProperty1Impl)
        val kMutableProperty1Impl = builtIns.classSymbolAt(dotlin.reflect.KMutableProperty1Impl)
        val kProperty2Impl = builtIns.classSymbolAt(dotlin.reflect.KProperty2Impl)
        val kMutableProperty2Impl = builtIns.classSymbolAt(dotlin.reflect.KMutableProperty2Impl)
    }

    private inline fun <reified S : IrSymbol> symbolAt(name: FqName): S {
        val packageFqName = name.parent()
        val memberIdentifier = name.shortName()
        val descriptor = builtInsModule.getPackage(packageFqName)
            .memberScope
            .getContributedDescriptors {
                it == memberIdentifier
            }.firstOrNull() ?: error("Classifier not found: $name")

        return symbolTable.run {
            when (S::class) {
                IrClassSymbol::class -> referenceClass(descriptor as ClassDescriptor)
                IrSimpleFunctionSymbol::class -> referenceFunction(descriptor as FunctionDescriptor)
                else -> error("Unsupported symbol type: ${S::class.simpleName}")
            } as S
        }
    }

    private fun functionSymbolAt(name: FqName): IrSimpleFunctionSymbol = symbolAt(name)

    private fun classSymbolAt(name: FqName): IrClassSymbol = symbolAt(name)
}