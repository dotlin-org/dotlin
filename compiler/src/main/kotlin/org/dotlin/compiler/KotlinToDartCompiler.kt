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

import com.intellij.openapi.util.Disposer
import org.dotlin.compiler.backend.DartPackage
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
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

object KotlinToDartCompiler {
    /**
     * Compiles the given Kotlin code and returns the output path.
     */
    @OptIn(ExperimentalPathApi::class)
    fun compile(
        kotlin: String,
        dependencies: Set<Path> = emptySet(),
        format: Boolean = false,
        klib: Boolean = false,
        output: Path = createTempDirectory(),
        isPublicPackage: Boolean = false,
    ): CompilationResult {
        val tmpDir = createTempDirectory().also {
            it.resolve("main.kt")
                .createFile()
                .toFile()
                .writeText(kotlin)
        }

        return compile(
            sourceRoot = tmpDir,
            dependencies,
            format,
            klib,
            output,
            isPublicPackage
        )
    }

    fun compile(
        sourceRoot: Path,
        dependencies: Set<Path>,
        format: Boolean = false,
        isKlib: Boolean = false,
        output: Path = createTempDirectory(),
        isPublicPackage: Boolean = false,
    ): CompilationResult {
        require(output.isDirectory())

        val (env, config) = prepareCompile(sourceRoot, showFiles = true)

        val ir = sourceToIr(
            env,
            config,
            dependencies,
            sourceRoot.toRealPath().absolute(),
            DartPackage(
                isPublic = isPublicPackage
            )
        )

        // By lazy is important here, the klib must be written before compiling to Dart source,
        // since it will change the IR in place.
        val result by lazy { generateDartCode(ir, config, isPublicPackage) }

        when {
            isKlib -> {
                writeToKlib(env, config, ir, output)

                val dartSourcePath = output.resolve("lib")

                for ((path, source) in result.sources) {
                    dartSourcePath.resolve(path).apply {
                        parent?.createDirectories()
                        writeText(source)
                    }
                }
            }
            else -> for ((path, source) in result.sources) {
                output.resolve(path).toFile().apply {
                    parentFile.mkdirs()
                    writeText(source)
                }
            }
        }

        if (format) {
            dartFormat(output.absolutePathString())
        }

        return CompilationResult(
            output,
            diagnostics = result.diagnostics
        )
    }

    private fun prepareCompile(sourceRoot: Path, showFiles: Boolean)
            : Pair<KotlinCoreEnvironment, CompilerConfiguration> {
        val compilerConfig = CompilerConfiguration().apply {
            val messageCollector = PrintingMessageCollector(
                System.out,
                if (showFiles) MessageRenderer.PLAIN_RELATIVE_PATHS else MessageRenderer.WITHOUT_PATHS,
                true,
            )

            put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)

            setupCommonArguments(Arguments()) { versionArray ->
                KlibMetadataVersion(*versionArray)
            }

            put(CommonConfigurationKeys.MODULE_NAME, "main")  // TODO: Use package name
            put(CLIConfigurationKeys.PERF_MANAGER, object : CommonCompilerPerformanceManager("Dotlin") {})

            addKotlinSourceRoots(listOf(sourceRoot.toString()))
        }

        val rootDisposable = Disposer.newDisposable()
        val env = KotlinCoreEnvironment.createForProduction(
            rootDisposable,
            compilerConfig,
            // There's no right choice here, but anything other than JVM config files seems to have little effect.
            EnvironmentConfigFiles.NATIVE_CONFIG_FILES
        )

        return env to compilerConfig
    }

    /**
     * Compiles the given Kotlin IR and returns the Dart source.
     */
    private fun generateDartCode(
        ir: IrResult,
        config: CompilerConfiguration,
        isPublicPackage: Boolean
    ): DartCodeGenerationResult {
        val (dartAst, diagnostics) = irToDartAst(config, ir, isPublicPackage)
        return DartCodeGenerationResult(dartAstToDartSource(dartAst), diagnostics)
    }

    class Arguments : CommonCompilerArguments()

    private fun dartFormat(vararg args: String): Int {
        val home = System.getenv("HOME").let {
            if (!it.endsWith(File.separator)) it + File.separator else it
        }
        val paths = System.getenv("PATH")
            .split(File.pathSeparator)
            .map { it.replaceFirst(Regex("^~" + File.separator), home) }

        val dart = paths
            .map { File(it, "dart") }
            .firstOrNull { it.exists() && it.isFile && it.canExecute() }
            ?: throw IllegalStateException("dart could not be found or cannot be executed")

        return Runtime.getRuntime().exec("$dart format ${args.joinToString(" ")}").waitFor()
    }
}

data class CompilationResult(
    val output: Path,
    val diagnostics: Collection<Diagnostic>
)

private data class DartCodeGenerationResult(
    val sources: Map<Path, String>,
    val diagnostics: Collection<Diagnostic>
)