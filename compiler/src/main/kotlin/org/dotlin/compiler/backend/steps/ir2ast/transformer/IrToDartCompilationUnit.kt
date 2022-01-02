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

import org.dotlin.compiler.backend.DotlinAnnotations
import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.valueArguments
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.optimizeImports
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import org.dotlin.compiler.dart.ast.directive.DartHideCombinator
import org.dotlin.compiler.dart.ast.directive.DartImportDirective
import org.dotlin.compiler.dart.ast.expression.identifier.toDartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.literal.DartSimpleStringLiteral
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.util.isAnnotation
import org.jetbrains.kotlin.name.FqName

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UNCHECKED_CAST")
object IrToDartCompilationUnitTransformer : IrDartAstTransformer<DartCompilationUnit> {
    override fun visitFile(irFile: IrFile, context: DartTransformContext) = DartCompilationUnit(
        declarations = irFile.declarations
            .map { it.accept(context) },
        // Add import alias directives against Kotlin/Dart built-ins name clashes.
        directives = irFile.annotations
            .asSequence()
            .filter { it.isDartImportAlias() || it.isDartHideImport() }
            .map {
                val importLibrary = (it.valueArguments[0] as IrConst<String>).value
                val hiddenName = (it.valueArguments[1] as IrConst<String>).value
                val aliasName = when {
                    it.isDartImportAlias() -> importLibrary.split(':')[1]
                    else -> null
                }

                Triple(importLibrary, hiddenName, aliasName)
            }
            .map { (importLibrary, hiddenName, aliasName) ->
                listOfNotNull(
                    // E.g. import 'dart:core' hide List;
                    DartImportDirective(
                        name = DartSimpleStringLiteral(importLibrary),
                        combinators = listOf(
                            DartHideCombinator(
                                names = listOf(hiddenName.toDartSimpleIdentifier())
                            )
                        )
                    ),
                    aliasName?.let {
                        // E.g. import 'dart:core' as core;
                        DartImportDirective(
                            name = DartSimpleStringLiteral(importLibrary),
                            alias = aliasName.toDartSimpleIdentifier()
                        )
                    }
                )
            }
            .flatten()
            // Always import the meta package for extra annotations.
            .plus(
                DartImportDirective(
                    name = DartSimpleStringLiteral("package:meta/meta.dart")
                )
            )
            .optimizeImports()
    )

    private fun IrConstructorCall.isDartImportAlias() =
        isAnnotation(FqName(DotlinAnnotations.dartImportAlias))

    private fun IrConstructorCall.isDartHideImport() =
        isAnnotation(FqName(DotlinAnnotations.dartHideImport))
}

fun IrFile.accept(context: DartTransformContext) = accept(IrToDartCompilationUnitTransformer, context)
