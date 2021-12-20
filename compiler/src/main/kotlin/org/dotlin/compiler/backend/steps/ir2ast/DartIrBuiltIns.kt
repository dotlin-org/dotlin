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

package org.dotlin.compiler.backend.steps.ir2ast

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class DartIrBuiltIns(
    private val builtInsModule: ModuleDescriptor,
    private val symbolTable: SymbolTable
) {
    private inline fun <reified S : IrSymbol> symbolAt(packageName: String, memberName: String): S {
        val descriptor = builtInsModule.getPackage(FqName(packageName))
            .memberScope
            .getContributedDescriptors {
                it == Name.identifier(memberName)
            }.firstOrNull() ?: error("Classifier not found: $packageName.$memberName")

        return symbolTable.let {
            when (S::class) {
                IrClassSymbol::class -> it.referenceClass(descriptor as ClassDescriptor) as S
                IrSimpleFunctionSymbol::class -> it.referenceSimpleFunction(descriptor as FunctionDescriptor) as S
                else -> error("Unsupported symbol type: ${S::class.simpleName}")
            }
        }.also {
            require(it.isBound) { "Built-in symbol is not bound: $it" }
        }
    }

    val identical = symbolAt<IrSimpleFunctionSymbol>("dart.core", "identical")

    val iterator = symbolAt<IrClassSymbol>("dart.core", "Iterator")

    val dotlin = Dotlin(this)

    class Dotlin(builtIns: DartIrBuiltIns) {
        val dart = builtIns.symbolAt<IrSimpleFunctionSymbol>("dotlin", "dart")
        val dartConst = builtIns.symbolAt<IrClassSymbol>("dotlin", "DartConst")
    }
}