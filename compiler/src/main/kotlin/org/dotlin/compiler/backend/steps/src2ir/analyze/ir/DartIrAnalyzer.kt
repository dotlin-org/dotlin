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

package org.dotlin.compiler.backend.steps.src2ir.analyze.ir

import org.dotlin.compiler.backend.DartNameGenerator
import org.dotlin.compiler.backend.DartPackage
import org.dotlin.compiler.backend.IrContext
import org.dotlin.compiler.hasErrors
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.backend.jvm.codegen.psiElement
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.DefaultDiagnosticReporter
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.nio.file.Path

class DartIrAnalyzer(
    private val module: IrModuleFragment,
    private val trace: BindingTrace,
    private val symbolTable: SymbolTable,
    private val dartNameGenerator: DartNameGenerator,
    private val sourceRoot: Path,
    private val dartPackage: DartPackage,
    config: CompilerConfiguration,
    private val checkers: List<IrDeclarationChecker> = listOf(DartExtensionWithoutProperAnnotationChecker),
    private val onlyReport: Collection<DiagnosticFactory<*>>? = null,
) {
    private val messageCollector = config[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY] ?: MessageCollector.NONE

    fun analyzeAndReport(): AnalysisResult {
        val context = IrAnalyzerContext(trace, symbolTable, dartNameGenerator, sourceRoot, dartPackage)

        module.files.forEach {
            context.enterFile(it)
            it.acceptChildrenVoid(
                object : IrElementVisitorVoid {
                    override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

                    override fun visitDeclaration(declaration: IrDeclarationBase) {
                        super.visitDeclaration(declaration)

                        checkers.forEach { checker ->
                            checker.run {
                                val sourceElement = declaration.ktDeclaration ?: return
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

    private val IrDeclaration.ktDeclaration: KtDeclaration?
        get() = psiElement as? KtDeclaration ?: when (this) {
            is IrAttributeContainer -> attributeOwnerId.safeAs<IrDeclaration>()?.psiElement as? KtDeclaration
            else -> null
        }

    private fun reportDiagnostics(diagnostics: Iterable<Diagnostic>) {
        val reporter = DefaultDiagnosticReporter(messageCollector)

        val diagnosticsToReport = when {
            onlyReport == null -> diagnostics
            onlyReport.isEmpty() -> emptyList()
            else -> diagnostics.filter { it.factory in onlyReport }
        }

        diagnosticsToReport.forEach {
            reporter.report(it, it.psiFile, DefaultErrorMessages.render(it))
        }
    }
}

class IrAnalyzerContext(
    val trace: BindingTrace,
    override val symbolTable: SymbolTable,
    override val dartNameGenerator: DartNameGenerator,
    override val sourceRoot: Path,
    override val dartPackage: DartPackage
) : IrContext()

interface IrDeclarationChecker {
    fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration)
}