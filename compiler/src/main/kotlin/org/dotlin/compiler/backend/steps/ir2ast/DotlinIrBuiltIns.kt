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
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.declarations.IrAnnotationContainer
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class DotlinIrBuiltIns(
    private val builtInsModule: ModuleDescriptor,
    private val symbolTable: SymbolTable
) {
    private inline fun <reified S : IrSymbol> symbolAt(fqName: String): S {
        val (packageName, memberName) = fqName.split(".").run {
            filter { it[0].isLowerCase() }.joinToString(".") to filter { it[0].isUpperCase() }.joinToString(".")
        }

        val descriptor = builtInsModule.getPackage(FqName(packageName))
            .memberScope
            .getContributedDescriptors {
                it == Name.identifier(memberName)
            }.firstOrNull() ?: error("Classifier not found: $fqName")

        return symbolTable.let {
            when (S::class) {
                IrClassSymbol::class -> it.referenceClass(descriptor as ClassDescriptor) as S
                else -> error("Unsupported symbol type: ${S::class.simpleName}")
            }
        }
    }

    val dartName = symbolAt<IrClassSymbol>("dotlin.DartName")
}
