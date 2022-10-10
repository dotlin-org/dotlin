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

import com.intellij.psi.PsiElement
import org.dotlin.compiler.backend.DartNameGenerator
import org.dotlin.compiler.backend.DartPackage
import org.dotlin.compiler.backend.IrContext
import org.dotlin.compiler.backend.steps.ir2ast.attributes.IrAttributes
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrExpressionContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.transformExpressions
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers.*
import org.dotlin.compiler.backend.steps.src2ir.reportAll
import org.dotlin.compiler.backend.util.ktDeclaration
import org.dotlin.compiler.hasErrors
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingTraceContext
import java.nio.file.Path

open class DartIrAnalyzer(
    private val module: IrModuleFragment,
    trace: BindingTraceContext,
    symbolTable: SymbolTable,
    dartNameGenerator: DartNameGenerator,
    sourceRoot: Path,
    dartPackage: DartPackage,
    config: CompilerConfiguration,
    irAttributes: IrAttributes,
    private val checkers: List<IrChecker<*, *, *>> = listOf(
        DartExtensionWithoutProperAnnotationChecker,
        WrongSetOperatorReturnTypeChecker,
        WrongSetOperatorReturnChecker,
        ImplicitInterfaceOverrideChecker,
        ConstValInitializerChecker,
        ConstConstructorParameterDefaultValueChecker,
        LongTypeReferenceChecker,
        ConstLambdaChecker,
        DartIndexChecker
    ),
) {
    private val messageCollector = config[CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY] ?: MessageCollector.NONE

    private val context: IrAnalyzerContext = IrAnalyzerContext(
        trace, symbolTable, module.irBuiltins,
        dartNameGenerator, sourceRoot, dartPackage,
        irAttributes
    )

    fun analyzeAndReport(): AnalysisResult {
        module.files.forEach {
            context.enterFile(it)
            it.acceptChildrenVoid(
                object : IrElementVisitorVoid {
                    override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)

                    override fun visitDeclaration(declaration: IrDeclarationBase) {
                        super.visitDeclaration(declaration)

                        // TODO: Use `_` for all types after IrDeclarationChecker
                        checkers.checkAll<IrDeclarationChecker, KtDeclaration, IrDeclaration, Nothing?>(
                            declaration.ktDeclaration,
                            declaration,
                            data = null
                        )
                    }
                }
            )

            it.transformExpressions { expression, context ->
                expression.also {
                    with(this@DartIrAnalyzer.context) {
                        checkers.checkAll<IrExpressionChecker, KtExpression, IrExpression, IrExpressionContext>(
                            expression.ktExpression,
                            expression,
                            context
                        )
                    }
                }
            }
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

    private inline fun <reified C : IrChecker<S, I, D>, S : PsiElement, I : IrElement, D> Iterable<IrChecker<*, *, *>>.checkAll(
        source: S?,
        element: I,
        data: D
    ) {
        filterIsInstance<C>().forEach { checker ->
            checker.run {
                val sourceElement = source ?: return
                context.check(sourceElement, element, data)
            }
        }
    }
}

class IrAnalyzerContext(
    val trace: BindingTraceContext,
    override val symbolTable: SymbolTable,
    override val irBuiltIns: IrBuiltIns,
    override val dartNameGenerator: DartNameGenerator,
    override val sourceRoot: Path,
    override val dartPackage: DartPackage,
    irAttributes: IrAttributes
) : IrContext(), IrAttributes by irAttributes {
    override val bindingContext = trace.bindingContext
}

interface IrChecker<S : PsiElement, I : IrElement, D> {
    val reports: List<DiagnosticFactory<*>>
    fun IrAnalyzerContext.check(source: S, element: I) {}

    fun IrAnalyzerContext.check(source: S, element: I, data: D) = check(source, element)
}

interface IrDeclarationChecker : IrChecker<KtDeclaration, IrDeclaration, Nothing?>

interface IrExpressionChecker : IrChecker<KtExpression, IrExpression, IrExpressionContext>