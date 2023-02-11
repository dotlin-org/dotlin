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

package org.dotlin.compiler.backend.steps.ir2ast.ir

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.TypeRemapper
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.ir.util.remapTypes
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

private fun IrFile?.remapAtRelevantParents(block: (IrElement) -> Unit) =
    this?.module?.files?.forEach { block(it) } ?: this?.let { block(it.getPackageFragment()!!) }

private fun IrDeclaration.remapAtRelevantParents(block: (IrElement) -> Unit) =
    fileOrNull.remapAtRelevantParents(block)

fun IrDeclaration.remapReferencesEverywhere(mapping: Pair<IrSymbol, IrSymbol>) =
    remapAtRelevantParents { it.remapReferences(mapOf(mapping)) }

fun IrElement.remapReferences(mapping: Map<IrSymbol, IrSymbol>) =
    transformChildrenVoid(DeclarationReferenceRemapper(mapping))

fun IrDeclaration.remapTypesEverywhere(typeRemapper: TypeRemapper) =
    remapAtRelevantParents { it.remapTypes(typeRemapper) }