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
import org.dotlin.compiler.dart.ast.directive.DartExportDirective
import org.dotlin.compiler.dart.ast.directive.DartImportDirective
import org.dotlin.compiler.dart.ast.directive.DartNamespaceDirective

object DartCompilationUnitTransformer : DartAstNodeTransformer() {
    override fun DartGenerationContext.visitCompilationUnit(unit: DartCompilationUnit): String {
        val directives = unit.acceptChild(separator = "\n") { directives }
        val declarations = unit.acceptChild(separator = "\n") { declarations }

        return "$directives\n$declarations"
    }

    override fun DartGenerationContext.visitNamespaceDirective(directive: DartNamespaceDirective) =
        directive.let {
            val annotations = it.acceptChildAnnotations()
            val keyword = when (it) {
                is DartImportDirective -> "import"
                is DartExportDirective -> "export"
            }
            val library = it.acceptChild { uri }
            val alias = when (it) {
                is DartImportDirective -> it.acceptChildOrNull { alias }?.let { alias -> " as $alias" } ?: ""
                else -> ""
            }
            val combinators = it.acceptChild(separator = " ", prefix = " ") { combinators }

            "$annotations$keyword $library$alias$combinators;"
        }

    override fun DartGenerationContext.visitCombinator(combinator: DartCombinator): String {
        val keyword = combinator.keyword
        val names = combinator.acceptChild(separator = ", ") { names }

        return "$keyword $names"
    }
}

fun DartCompilationUnit.accept(context: DartGenerationContext) = accept(DartCompilationUnitTransformer, context)