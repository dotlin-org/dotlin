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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.isEffectivelyExternal

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartCompilationUnitTransformer : IrTransformer<DartCompilationUnit> {
    override fun visitFile(irFile: IrFile, context: DartTransformContext) = DartCompilationUnit(
        declarations = irFile.declarations
            .filterNot { it.isEffectivelyExternal() }
            .map { it.accept(context) }
    )
}

fun IrFile.accept(context: DartTransformContext) = accept(IrToDartCompilationUnitTransformer, context)