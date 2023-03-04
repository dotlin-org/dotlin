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

package org.dotlin.compiler

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import kotlinx.coroutines.runBlocking
import org.dotlin.compiler.backend.DartPathGenerator
import org.dotlin.compiler.backend.DartProject
import org.dotlin.compiler.backend.steps.ast2dart.dartAstToDartSource
import org.dotlin.compiler.backend.steps.ir2ast.irToDartAst
import org.dotlin.compiler.backend.steps.ir2klib.writeToKlib
import org.dotlin.compiler.backend.steps.src2ir.IrResult
import org.dotlin.compiler.backend.steps.src2ir.sourceToIr
import org.jetbrains.kotlin.backend.common.serialization.metadata.KlibMetadataVersion
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.CommonCompilerPerformanceManager
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.common.setupCommonArguments
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.Diagnostic
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.io.path.writeText

object KotlinToDartCompiler {
    fun compile(path: Path, format: Boolean = false): CompilationResult = compile(
        runBlocking { DartProject.from(path) },
        format
    )

    fun compile(
        project: DartProject,
        format: Boolean = false,
        showPathsInDiagnostics: Boolean = true,
    ): CompilationResult {
        require(project.path.isDirectory())

        val (env, config, rootDisposable) = prepareCompile(project, showPathsInDiagnostics)

        try {
            val ir = sourceToIr(
                env,
                config,
                project
            )

            if (project.isPublishable) {
                writeToKlib(env, config, ir, project)
            }

            val result = generateDartCode(ir, config)

            // Delete all old files first.
            // TODO: Only delete changed files, but still delete related deleted .kt files.
            project.path.toFile().walk()
                .filter { !it.isDirectory && it.path.endsWith(".${DartPathGenerator.FILE_EXTENSION}") }
                .forEach { it.delete() }

            for ((path, source) in result.sources) {
                project.path.resolve(path).apply {
                    parent?.createDirectories()
                    writeText(source)
                }
            }

            if (format) {
                dartFormat(project.path.absolutePathString())
            }

            return CompilationResult(
                project,
                diagnostics = result.diagnostics
            )
        } finally {
            Disposer.dispose(rootDisposable)
        }
    }

    private fun prepareCompile(project: DartProject, showPaths: Boolean)
            : Triple<KotlinCoreEnvironment, CompilerConfiguration, Disposable> {
        val compilerConfig = CompilerConfiguration().apply {
            val messageCollector = PrintingMessageCollector(
                System.out,
                if (showPaths) MessageRenderer.PLAIN_RELATIVE_PATHS else MessageRenderer.WITHOUT_PATHS,
                true,
            )

            put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)

            setupCommonArguments(Arguments()) { versionArray ->
                KlibMetadataVersion(*versionArray)
            }

            put(CommonConfigurationKeys.MODULE_NAME, project.name)
            put(CLIConfigurationKeys.PERF_MANAGER, object : CommonCompilerPerformanceManager("Dotlin") {})

            addKotlinSourceRoots(listOf(project.path.toString()))
        }

        val rootDisposable = Disposer.newDisposable()

        // TODO `createForProduction` has memory leaks when running tests, however `createForTests` has assumptions
        // about certain files being present.
        val env = KotlinCoreEnvironment.createForProduction(
            rootDisposable,
            compilerConfig,
            // There's no right choice here, but anything other than JVM config files seems to have little effect.
            EnvironmentConfigFiles.NATIVE_CONFIG_FILES
        )

        return Triple(env, compilerConfig, rootDisposable)
    }

    /**
     * Compiles the given Kotlin IR and returns the Dart source.
     */
    private fun generateDartCode(
        ir: IrResult,
        config: CompilerConfiguration,
    ): DartCodeGenerationResult {
        val (dartAst, diagnostics) = irToDartAst(config, ir)
        return DartCodeGenerationResult(dartAstToDartSource(dartAst), diagnostics)
    }

    class Arguments : CommonCompilerArguments()

    private fun dartFormat(vararg args: String): Int =
        Runtime.getRuntime().exec("dart format ${args.joinToString(" ")}").waitFor()
}

data class CompilationResult(
    val project: DartProject,
    val diagnostics: Collection<Diagnostic>
)

private data class DartCodeGenerationResult(
    val sources: Map<Path, String>,
    val diagnostics: Collection<Diagnostic>
)