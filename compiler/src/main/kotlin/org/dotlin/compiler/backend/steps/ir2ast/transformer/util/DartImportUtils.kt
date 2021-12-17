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

package org.dotlin.compiler.backend.steps.ir2ast.transformer.util

import org.dotlin.compiler.dart.ast.directive.DartCombinator
import org.dotlin.compiler.dart.ast.directive.DartDirective
import org.dotlin.compiler.dart.ast.directive.DartImportDirective

/**
 * Combine combinators, annotations, etc. from imports of the same library and alias.
 */
fun Collection<DartDirective>.optimizeImports() = asSequence().optimizeImports()

/**
 * Combine combinators, annotations, etc. from imports of the same library and alias.
 */
fun Sequence<DartDirective>.optimizeImports() =
    distinct()
        .filterIsInstance<DartImportDirective>()
        .groupBy { it.name to it.alias }
        .entries
        .map { (key, directives) ->
            val (name, alias) = key
            DartImportDirective(
                name, alias,
                annotations = directives.map { it.annotations }.flatten(),
                combinators = directives.map { it.combinators }
                    .flatten()
                    .groupBy { it.keyword }
                    .entries.map { (keyword, combinators) ->
                        DartCombinator.fromKeyword(
                            keyword,
                            names = combinators.map { it.names }.flatten().distinct()
                        )
                    },
                documentationComment = directives.mapNotNull { it.documentationComment }
                    .joinToString("\n\n") { it }
            )
        }
        .plus(filter { it !is DartImportDirective })