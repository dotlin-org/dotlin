/*
 * Copyright 2021 Wilko Manger
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
import org.jetbrains.kotlin.konan.file.file
import org.jetbrains.kotlin.konan.file.File as KonanFile
import org.jetbrains.kotlin.konan.file.zipFileSystem
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory

object KotlinToDartCompiler {
    /**
     * Compiles the given Kotlin code and returns Dart code.
     */
    @OptIn(ExperimentalPathApi::class)
    fun compile(kotlin: String, dependencies: Set<File> = emptySet(), format: Boolean = false): String {
        val tmpDir = createTempDirectory().also {
            it.resolve("main.kt")
                .createFile()
                .toFile()
                .writeText(kotlin)
        }

        val (env, config) = prepareCompile(setOf(tmpDir), showFiles = false)

        var dartSource = compileToDartSource(
            ir = sourceToIr(env, config, dependencies),
            config
        )

        if (format) {
            dartSource = dartFormat(dartSource).removeSuffix("\n")
        }

        return dartSource
    }

    /**
     * Compiles the given Kotlin source roots and writes it to [outputFile].
     */
    fun compile(
        sourceRoots: Set<Path>,
        outputFile: File,
        dependencies: Set<File>,
        format: Boolean = false,
        klib: Boolean = false
    ) {
        val (env, config) = prepareCompile(sourceRoots, showFiles = true)

        val ir = sourceToIr(env, config, dependencies)

        // The klib file must be written before compiling to Dart source, since it will chang the IR in place.
        if (klib) {
            writeToKlib(env, config, ir, outputFile)
        }

        val dartSource = compileToDartSource(ir, config)

        when {
            klib -> {
                // If we're compiling to a klib, we want to put the Dart source in the klib.
                KonanFile(outputFile.absolutePath).zipFileSystem().use {
                    val source = when {
                        format -> dartFormat(dartSource)
                        else -> dartSource
                    }

                    it.file("dart/main.dart").apply {
                        parentFile.mkdirs()
                        writeText(source)
                    }
                }
            }
            else -> outputFile.apply {
                parentFile?.mkdirs()
                writeText(dartSource)

                if (format) {
                    dartFormat(this)
                }
            }
        }
    }

    private fun prepareCompile(sourceRoots: Set<Path>, showFiles: Boolean, outputFile: File? = null)
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

            put(CommonConfigurationKeys.MODULE_NAME, outputFile?.nameWithoutExtension ?: "main")
            put(CLIConfigurationKeys.PERF_MANAGER, object : CommonCompilerPerformanceManager("Dotlin") {})

            addKotlinSourceRoots(sourceRoots.map { it.toString() })
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
    private fun compileToDartSource(
        ir: IrResult,
        config: CompilerConfiguration,
    ): String {
        val dartAst = irToDartAst(config, ir.module)
        return dartAstToDartSource(dartAst)
    }

    class Arguments : CommonCompilerArguments()

    private fun execDartFormat(args: String = ""): Process {
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

        return Runtime.getRuntime().exec("$dart format $args")
    }

    private fun dartFormat(code: String): String {
        val process = execDartFormat()
        val stdin = process.outputStream
        val stdout = process.inputStream

        stdin.apply {
            write(code.encodeToByteArray())
            flush()
            close()
        }

        val output = String(stdout.readBytes())

        return if (output.isNotEmpty()) output else code
    }

    private fun dartFormat(file: File) {
        execDartFormat(file.absolutePath)
    }
}