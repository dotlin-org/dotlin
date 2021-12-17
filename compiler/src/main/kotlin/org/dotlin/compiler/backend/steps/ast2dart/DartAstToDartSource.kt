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

package org.dotlin.compiler.backend.steps.ast2dart

import org.dotlin.compiler.backend.steps.ast2dart.transformer.accept
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.optimizeImports
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit

fun dartAstToDartSource(units: List<DartCompilationUnit>): String {
    val context = DartGenerationContext()

    // We compile to a single file (for now).
    return DartCompilationUnit(
        directives = units.map { it.directives }.flatten().optimizeImports(),
        declarations = units.map { it.declarations }.flatten()
    ).accept(context)
}
