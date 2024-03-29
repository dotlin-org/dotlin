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

import org.dotlin.compiler.backend.DartPathGenerator.dartPath
import org.dotlin.compiler.backend.steps.ir2ast.lower.lower
import org.dotlin.compiler.backend.steps.ir2ast.transformer.IrToDartCompilationUnitTransformer
import org.dotlin.compiler.backend.steps.src2ir.IrResult
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.DartIrAnalyzer
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers.post.DartNameChecker
import org.dotlin.compiler.backend.steps.src2ir.throwIfHasErrors
import org.dotlin.compiler.dart.ast.compilationunit.DartCompilationUnit
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.Diagnostic
import java.nio.file.Path

fun irToDartAst(
    config: CompilerConfiguration,
    ir: IrResult,
): IrToDartAstResult {
    val loweringContext = ir.lower(config, ir.loweringContext, ir.dotlinIrBuiltIns)

    // Some analysis must be done after lowering.
    DartIrAnalyzer(
        ir.module, ir.bindingTrace,
        ir.symbolTable, ir.dartNameGenerator,
        ir.dartProject,
        config,
        ir.irAttributes,
        checkers = listOf(DartNameChecker),
    ).analyzeAndReport().also {
        it.throwIfHasErrors()
    }

    val context = DartAstTransformContext(loweringContext)
    val units = mutableMapOf<Path, DartCompilationUnit>()
    for (file in ir.module.files) {
        context.enterFile(file)

        file.accept(IrToDartCompilationUnitTransformer, context).let {
            if (it.declarations.isNotEmpty()) {
                units[context.run { file.dartPath }] = it
            }
        }
    }

    val dartProject = ir.dartProject

    // TODO: Only do this if we're not mixing public Dart/Kotlin code.
    // TODO: Make separate option in pubspec
    // TODO: Move to lowering/IR level?
    if (/*dartProject.compileKlib*/false) {
        // Add exports file.
        /*units[Path("lib/${dartProject.name}.${DartPathGenerator.FILE_EXTENSION}")] = DartCompilationUnit(
            directives = units.mapNotNull { (path, unit) ->
                when {
                    // If all declarations in the unit are private or internal, we don't need to export it.
                    unit.declarations.all {
                        (it is DartNamedCompilationUnitMember && it.isPrivate) || it.isInternal
                    } -> null
                    else -> DartExportDirective(
                        uri = DartSimpleStringLiteral(path.toString()),
                        combinators = unit.declarations
                            .filterIsInstanceAnd<DartNamedCompilationUnitMember> { it.isInternal }
                            .map { it.name }
                            .let {
                                when {
                                    it.isEmpty() -> emptyList()
                                    else -> listOf(DartHideCombinator(it))
                                }
                            }
                    )
                }
            }
        )*/
    }

    return IrToDartAstResult(units, ir.bindingTrace.bindingContext.diagnostics.all())
}

data class IrToDartAstResult(
    val units: Map<Path, DartCompilationUnit>,
    val diagnostics: Collection<Diagnostic>
)