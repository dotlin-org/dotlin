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

package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.DartDescriptorBasedMangler
import org.dotlin.compiler.backend.DartIrLinker
import org.dotlin.compiler.backend.DartKotlinBuiltIns
import org.dotlin.compiler.backend.DotlinCompilerError
import org.dotlin.compiler.backend.steps.src2ir.analyze.DartKotlinAnalyzer
import org.jetbrains.kotlin.backend.common.serialization.DeserializationStrategy
import org.jetbrains.kotlin.backend.common.serialization.signature.IdSignatureDescriptor
import org.jetbrains.kotlin.cli.common.messages.AnalyzerWithCompilerReport
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.descriptors.konan.kotlinLibrary
import org.jetbrains.kotlin.ir.backend.js.isBuiltIns
import org.jetbrains.kotlin.ir.builders.TranslationPluginContext
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.konan.util.KlibMetadataFactories
import org.jetbrains.kotlin.library.resolver.KotlinLibraryResolveResult
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.util.DummyLogger
import org.jetbrains.kotlin.utils.addToStdlib.cast
import java.io.File

fun sourceToIr(
    env: KotlinCoreEnvironment,
    config: CompilerConfiguration,
    dependencies: Set<File>,
): IrResult {
    val sourceFiles = env.getSourceFiles()

    val resolvedLibraries = resolveLibraries(
        dependencies.map { it.absolutePath },
        DummyLogger,
    )

    return loadIr(
        env,
        config,
        sourceFiles,
        irFactory = IrFactoryImpl,
        resolvedLibraries,
    )
}

private fun loadIr(
    env: KotlinCoreEnvironment,
    config: CompilerConfiguration,
    files: List<KtFile>,
    irFactory: IrFactory,
    resolvedLibs: KotlinLibraryResolveResult,
): IrResult {
    val builtIns = DartKotlinBuiltIns()

    val resolvedModules = KlibMetadataFactories({ builtIns }, DynamicTypeDeserializer)
        .DefaultResolvedDescriptorsFactory
        .createResolved(
            resolvedLibs,
            storageManager = LockBasedStorageManager("ResolvedModules"),
            builtIns = builtIns,
            config.languageVersionSettings,
            additionalDependencyModules = listOf(),
        )

    val foundBuiltInsModule = resolvedModules.resolvedDescriptors
        .firstOrNull { it.name == Name.special("<kotlin>") }

    val compilingBuiltIns = foundBuiltInsModule == null

    if (compilingBuiltIns) {
        config.put(CommonConfigurationKeys.MODULE_NAME, "kotlin")
    } else {
        builtIns.builtInsModule = foundBuiltInsModule!!
    }

    val analyzer = AnalyzerWithCompilerReport(config).also {
        it.analyzeAndReport(files) {
            DartKotlinAnalyzer(env, config).analyze(
                files,
                modules = resolvedModules.resolvedDescriptors,
                isBuiltInsModule = compilingBuiltIns,
                builtInsModule = foundBuiltInsModule,
                it.targetEnvironment
            )
        }
    }

    val analysisResult = analyzer.analysisResult

    if (analyzer.hasErrors() || analysisResult.isError()) {
        throw DotlinCompilerError()
    }

    val mainModule = analysisResult.moduleDescriptor.also {
        if (compilingBuiltIns) {
            builtIns.builtInsModule = it as ModuleDescriptorImpl
        }
    }

    val psi2Ir = Psi2IrTranslator(
        config.languageVersionSettings,
        Psi2IrConfiguration(ignoreErrors = false)
    )
    val symbolTable = SymbolTable(IdSignatureDescriptor(DartDescriptorBasedMangler), irFactory)
    val psi2IrContext = psi2Ir.createGeneratorContext(
        mainModule,
        analysisResult.bindingContext,
        symbolTable
    )

    val frontEndContext = object : TranslationPluginContext {
        override val moduleDescriptor = mainModule
        override val symbolTable = symbolTable
        override val typeTranslator = psi2IrContext.typeTranslator
        override val irBuiltIns = psi2IrContext.irBuiltIns
    }

    val irLinker = DartIrLinker(
        currentModule = mainModule,
        IrMessageLogger.None,
        psi2IrContext.irBuiltIns,
        symbolTable,
        frontEndContext,
    )

    if (!compilingBuiltIns) {
        resolvedLibs.getFullList().find { it.isBuiltIns }?.let {
            irLinker.deserializeIrModuleHeader(
                moduleDescriptor = mainModule.builtIns.builtInsModule,
                kotlinLibrary = it,
                // TODO: Optimize?
                // For built-ins, we want everything.
                deserializationStrategy = DeserializationStrategy.ALL
            )
        }
    }

    resolvedModules.resolvedDescriptors.forEach {
        irLinker.deserializeIrModuleHeader(it, it.kotlinLibrary)
    }

    // TODO: IrPlugins

    val module = psi2Ir.generateModuleFragment(
        context = psi2IrContext,
        ktFiles = files,
        irProviders = listOf(irLinker),
        linkerExtensions = emptyList(),
    )

    return IrResult(
        module,
        resolvedLibs,
        psi2IrContext.bindingContext,
        symbolTable
    )
}

private val ModuleDescriptorImpl.dependencies: Set<ModuleDescriptorImpl>
    get() {
        val allDependencies = mutableSetOf<ModuleDescriptorImpl>()
        fun add(module: ModuleDescriptorImpl) {
            val dependencies = module.allDependencyModules
            dependencies.forEach {
                if (it !in allDependencies) {
                    add(it as ModuleDescriptorImpl)
                }
            }
            allDependencies += dependencies.cast<ModuleDescriptorImpl>()
        }

        add(this)

        return allDependencies
    }

data class IrResult(
    val module: IrModuleFragment,
    val resolvedLibs: KotlinLibraryResolveResult,
    val bindingContext: BindingContext,
    val symbolTable: SymbolTable,
)
