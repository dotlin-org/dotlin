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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.optimizeImports
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import org.dotlin.compiler.dart.ast.directive.DartHideCombinator
import org.dotlin.compiler.dart.ast.directive.DartImportDirective
import org.dotlin.compiler.dart.ast.expression.identifier.toDartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.literal.DartSimpleStringLiteral
import org.jetbrains.kotlin.ir.declarations.IrFile

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UNCHECKED_CAST")
object IrToDartCompilationUnitTransformer : IrDartAstTransformer<DartCompilationUnit>() {
    override fun DartTransformContext.visitFile(irFile: IrFile, context: DartTransformContext) = context.run {
        DartCompilationUnit(
            declarations = irFile.declarations.map { it.accept(context) },
            directives = irFile.dartImports
                .asSequence()
                .flatMap { import ->
                    listOfNotNull(
                        // E.g. import 'dart:core' hide List;
                        DartImportDirective(
                            name = DartSimpleStringLiteral(import.library),
                            combinators = listOfNotNull(
                                import.hide?.let {
                                    DartHideCombinator(
                                        names = listOf(it.toDartSimpleIdentifier())
                                    )
                                }
                            )
                        ),
                        import.alias?.let {
                            // E.g. import 'dart:core' as core;
                            DartImportDirective(
                                name = DartSimpleStringLiteral(import.library),
                                alias = it.toDartSimpleIdentifier()
                            )
                        }
                    )
                }
                // Always import the meta package for extra annotations.
                .plus(
                    DartImportDirective(
                        name = DartSimpleStringLiteral("package:meta/meta.dart")
                    )
                )
                .optimizeImports()
                .toList()
        )
    }
}

fun IrFile.accept(context: DartTransformContext) = accept(IrToDartCompilationUnitTransformer, context)
