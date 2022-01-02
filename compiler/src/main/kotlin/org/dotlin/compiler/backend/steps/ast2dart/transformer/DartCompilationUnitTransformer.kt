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

package org.dotlin.compiler.backend.steps.ast2dart.transformer

import org.dotlin.compiler.backend.steps.ast2dart.DartGenerationContext
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import org.dotlin.compiler.dart.ast.directive.DartCombinator
import org.dotlin.compiler.dart.ast.directive.DartDirective
import org.dotlin.compiler.dart.ast.directive.DartImportDirective

object DartCompilationUnitTransformer : DartAstNodeTransformer {
    override fun visitCompilationUnit(unit: DartCompilationUnit, context: DartGenerationContext): String {
        val directives = unit.directives.joinToString("\n") { it.accept(context) }
        val declarations = unit.declarations.joinToString(separator = "\n") { it.accept(context) }

        return "$directives\n$declarations"
    }

    override fun visitImportDirective(directive: DartImportDirective, context: DartGenerationContext): String {
        val annotations = directive.annotations.accept(context)
        val import = "import"
        val library = directive.name.accept(context)
        val alias = directive.alias?.accept(context)?.let { " as $it" } ?: ""
        val combinators = directive.combinators.let { combinators ->
            when {
                combinators.isEmpty() -> ""
                else -> combinators.joinToString(" ", prefix = " ") { it.accept(context) }
            }
        }

        return "$annotations$import $library$alias$combinators;"
    }

    override fun visitCombinator(combinator: DartCombinator, context: DartGenerationContext): String {
        val keyword = combinator.keyword
        val names = combinator.names.joinToString(", ") { it.accept(context) }

        return "$keyword $names"
    }
}

fun DartCompilationUnit.accept(context: DartGenerationContext) = accept(DartCompilationUnitTransformer, context)
fun DartDirective.accept(context: DartGenerationContext) = accept(DartCompilationUnitTransformer, context)
fun DartCombinator.accept(context: DartGenerationContext) = accept(DartCompilationUnitTransformer, context)