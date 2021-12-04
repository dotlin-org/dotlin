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

package org.dotlin.compiler.backend.steps.ast2dart.transformer

import org.dotlin.compiler.backend.steps.ast2dart.DartGenerationContext
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit

object DartCompilationUnitTransformer : DartAstNodeTransformer {
    override fun visitCompilationUnit(unit: DartCompilationUnit, context: DartGenerationContext): String {
        return unit.declarations.joinToString(separator = "\n") { it.accept(context) }
    }
}

fun DartCompilationUnit.accept(context: DartGenerationContext) = accept(DartCompilationUnitTransformer, context)