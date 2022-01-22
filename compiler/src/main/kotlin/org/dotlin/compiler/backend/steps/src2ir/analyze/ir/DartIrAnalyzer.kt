package org.dotlin.compiler.backend.steps.src2ir.analyze.ir

import org.dotlin.compiler.backend.DartNameGenerator
import org.dotlin.compiler.backend.IrContext
import org.dotlin.compiler.backend.steps.src2ir.hasErrors
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.backend.jvm.codegen.psiElement
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.DefaultDiagnosticReporter
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.BindingTrace

/*
 * Copyright 2022 Wilko Manger
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
class DartIrAnalyzer(
    private val module: IrModuleFragment,
    private val trace: BindingTrace,
    private val symbolTable: SymbolTable,
    private val dartNameGenerator: DartNameGenerator,
    config: CompilerConfiguration,
    private val checkers: List<IrDeclarationChecker> = listOf(),
) {
    private val messageCollector = config[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY] ?: MessageCollector.NONE

    fun analyzeAndReport(): AnalysisResult {
        val context = IrAnalyzerContext(trace, symbolTable, dartNameGenerator)

        module.files.forEach {
            it.acceptChildrenVoid(
                object : IrElementVisitorVoid {
                    override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

                    override fun visitDeclaration(declaration: IrDeclarationBase) {
                        super.visitDeclaration(declaration)

                        checkers.forEach { checker ->
                            checker.run {
                                val sourceElement = declaration.psiElement as? KtDeclaration ?: return
                                context.check(sourceElement, declaration)
                            }
                        }
                    }
                }
            )
        }



        return context.trace.bindingContext.let {
            reportDiagnostics(it.diagnostics)

            when {
                it.diagnostics.hasErrors -> AnalysisResult.compilationError(it)
                else -> AnalysisResult.Companion.success(it, module.descriptor)
            }
        }
    }

    private fun reportDiagnostics(diagnostics: Iterable<Diagnostic>) {
        val reporter = DefaultDiagnosticReporter(messageCollector)
        diagnostics.forEach {
            reporter.report(it, it.psiFile, DefaultErrorMessages.render(it))
        }
    }

}

class IrAnalyzerContext(
    val trace: BindingTrace,
    override val symbolTable: SymbolTable,
    override val dartNameGenerator: DartNameGenerator
) : IrContext

interface IrDeclarationChecker {
    fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration)
}