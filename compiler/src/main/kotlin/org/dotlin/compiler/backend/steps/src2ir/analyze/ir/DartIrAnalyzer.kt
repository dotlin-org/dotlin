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
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers.DartExtensionWithoutProperAnnotationChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers.ImplicitInterfaceOverrideChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers.WrongSetOperatorReturnChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers.WrongSetOperatorReturnTypeChecker
import org.dotlin.compiler.backend.steps.src2ir.reportAll
import org.dotlin.compiler.backend.steps.src2ir.reportOnly
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
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.BindingTraceContext
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.nio.file.Path

class DartIrAnalyzer(
    private val module: IrModuleFragment,
    private val trace: BindingTraceContext,
    private val symbolTable: SymbolTable,
    private val dartNameGenerator: DartNameGenerator,
    private val sourceRoot: Path,
    private val dartPackage: DartPackage,
    config: CompilerConfiguration,
    private val checkers: List<IrDeclarationChecker> = listOf(
        DartExtensionWithoutProperAnnotationChecker,
        WrongSetOperatorReturnTypeChecker,
        WrongSetOperatorReturnChecker,
        ImplicitInterfaceOverrideChecker
    ),
) {
    private val messageCollector = config[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY] ?: MessageCollector.NONE

    fun analyzeAndReport(): AnalysisResult {
        val context =
            IrAnalyzerContext(trace, symbolTable, module.irBuiltins, dartNameGenerator, sourceRoot, dartPackage)

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
            // We only want to report the diagnostics from the IR checkers
            // not previously reported, unrelated diagnostics.
            val factories = checkers.flatMap { c -> c.reports }
            val diagnosticsToReport = it.diagnostics.filter { d -> d.factory in factories }

            diagnosticsToReport.reportAll(messageCollector)

            when {
                diagnosticsToReport.hasErrors -> AnalysisResult.compilationError(it)
                else -> AnalysisResult.success(it, module.descriptor)
            }
        }
    }

    private val IrDeclaration.ktDeclaration: KtDeclaration?
        get() = psiElement as? KtDeclaration ?: when (this) {
            is IrAttributeContainer -> attributeOwnerId.safeAs<IrDeclaration>()?.psiElement as? KtDeclaration
            else -> null
        }
}

class IrAnalyzerContext(
    val trace: BindingTraceContext,
    override val symbolTable: SymbolTable,
    override val irBuiltIns: IrBuiltIns,
    override val dartNameGenerator: DartNameGenerator,
    override val sourceRoot: Path,
    override val dartPackage: DartPackage
) : IrContext() {
    override val bindingContext = trace.bindingContext
}

interface IrDeclarationChecker {
    val reports: List<DiagnosticFactory<*>>
    fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration)
}