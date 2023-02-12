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

package org.dotlin.compiler.backend.steps.src2ir.analyze

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.ExtensionPointName
import org.dotlin.compiler.backend.DartProject
import org.dotlin.compiler.backend.steps.src2ir.DartElementLocator
import org.dotlin.compiler.backend.steps.src2ir.analyze.suppress.DartDiagnosticSuppressor
import org.dotlin.compiler.backend.steps.src2ir.throwIfHasErrors
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTraceContext
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor

class DartKotlinAnalyzerReporter(
    private val env: KotlinCoreEnvironment,
    private val config: CompilerConfiguration,
    private val dartProject: DartProject,
    private val dartElementLocator: DartElementLocator,
) {
    fun analyzeAndReport(
        files: List<KtFile>,
        dependencies: List<ModuleDescriptorImpl>,
        isBuiltInsModule: Boolean,
        builtIns: KotlinBuiltIns,
        trace: BindingTraceContext
    ): AnalysisResult {
        env.registerExtension(
            DiagnosticSuppressor.EP_NAME,
            DartDiagnosticSuppressor(trace),
        )

        val report = AnalyzerWithCompilerReport(config).also {
            it.analyzeAndReport(files) {
                DotlinAnalyzer(env, config, dartProject, dartElementLocator).analyze(
                    files,
                    dependencies,
                    isBuiltInsModule,
                    builtIns,
                    it.targetEnvironment,
                    trace
                )
            }
        }

        return report.analysisResult.also {
            it.throwIfHasErrors()
        }
    }
}

fun <T : Any> KotlinCoreEnvironment.registerExtension(epName: ExtensionPointName<T>, ext: T) {
    ApplicationManager.getApplication().extensionArea.getExtensionPoint(epName)
        .let { ep ->
            if (!ep.extensions.any { it == ext }) {
                ep.registerExtension(ext, projectEnvironment.parentDisposable)
            }
        }
}