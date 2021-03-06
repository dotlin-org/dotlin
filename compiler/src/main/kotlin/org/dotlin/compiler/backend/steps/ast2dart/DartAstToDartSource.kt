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

package org.dotlin.compiler.backend.steps.ast2dart

import org.dotlin.compiler.backend.steps.ast2dart.transformer.accept
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import java.nio.file.Path

fun dartAstToDartSource(units: Map<Path, DartCompilationUnit>): Map<Path, String> {
    val context = DartGenerationContext()

    return units.mapValues { (_, unit) -> unit.accept(context) }
}
