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

package org.dotlin.compiler.backend

import org.dotlin.compiler.backend.steps.src2ir.IrResult
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

private fun compileToDartAst(
    sourceToIr: () -> IrResult,
    irToDartAst: (IrModuleFragment) -> List<DartCompilationUnit>,
) = irToDartAst(sourceToIr().module)

fun compileToDartSource(
    sourceToIr: () -> IrResult,
    irToDartAst: (IrModuleFragment) -> List<DartCompilationUnit>,
    dartAstToDartSource: (List<DartCompilationUnit>) -> String
): String {
    return dartAstToDartSource(compileToDartAst(sourceToIr, irToDartAst))
}

fun compileToKlib(
    sourceToIr: () -> IrResult,
    writeToKlib: (IrResult) -> Unit
) {
    writeToKlib(sourceToIr())
}