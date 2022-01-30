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

package org.dotlin.compiler.backend.steps.ir2ast

import org.dotlin.compiler.backend.steps.ir2ast.lower.lower
import org.dotlin.compiler.backend.steps.ir2ast.transformer.IrToDartCompilationUnitTransformer
import org.dotlin.compiler.backend.steps.src2ir.IrResult
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.DartIrAnalyzer
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.DartNameChecker
import org.dotlin.compiler.backend.steps.src2ir.throwIfIsError
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import org.dotlin.compiler.dart.ast.compilationunit.DartNamedCompilationUnitMember
import org.dotlin.compiler.dart.ast.directive.DartExportDirective
import org.dotlin.compiler.dart.ast.expression.literal.DartSimpleStringLiteral
import org.jetbrains.kotlin.config.CompilerConfiguration
import java.nio.file.Path
import kotlin.io.path.Path

fun irToDartAst(
    config: CompilerConfiguration,
    ir: IrResult,
    isPublicPackage: Boolean
): Map<Path, DartCompilationUnit> {
    val loweringContext = ir.lower(config)

    // Dart names are checked after lowering.
    DartIrAnalyzer(
        ir.module, ir.bindingTrace,
        ir.symbolTable, ir.dartNameGenerator,
        ir.sourceRoot,
        config,
        checkers = listOf(DartNameChecker())
    ).analyzeAndReport().also {
        it.throwIfIsError()
    }

    val context = DartTransformContext(loweringContext)
    val units = mutableMapOf<Path, DartCompilationUnit>()
    for (file in ir.module.files) {
        context.enterFile(file)

        file.accept(IrToDartCompilationUnitTransformer, context).let {
            if (it.declarations.isNotEmpty()) {
                units[context.run { file.relativeDartPath }] = it
            }
        }
    }

    if (isPublicPackage) {
        // Add exports file.
        // TODO: Use package name
        units[Path("package_name.g.dart")] = DartCompilationUnit(
            directives = units.mapNotNull { (path, unit) ->
                when {
                    // If all declarations in the unit are private or internal, we don't need to export it.
                    unit.declarations.all {
                        (it is DartNamedCompilationUnitMember && it.name.isPrivate) || DartAnnotation.INTERNAL in it.annotations
                    } -> null
                    else -> DartExportDirective(
                        uri = DartSimpleStringLiteral(path.toString())
                    )
                }
            }
        )
    }

    return units
}