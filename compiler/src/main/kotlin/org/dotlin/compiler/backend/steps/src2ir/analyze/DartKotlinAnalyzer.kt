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

import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.functions.functionInterfacePackageFragmentProvider
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.context.ContextForNewModule
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.descriptors.impl.CompositePackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.frontend.di.configureModule
import org.jetbrains.kotlin.frontend.di.configureStandardResolveComponents
import org.jetbrains.kotlin.incremental.components.ExpectActualTracker
import org.jetbrains.kotlin.incremental.components.InlineConstTracker
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.js.JsPlatforms
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.lazy.KotlinCodeAnalyzer
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory

class DartKotlinAnalyzer(
    private val env: KotlinCoreEnvironment,
    private val config: CompilerConfiguration,
) {
    fun analyze(
        files: List<KtFile>,
        dependencies: List<ModuleDescriptorImpl>,
        isBuiltInsModule: Boolean = false,
        builtIns: KotlinBuiltIns,
        targetEnvironment: TargetEnvironment,
        trace: BindingTrace,
    ): AnalysisResult {
        val moduleName = config[CommonConfigurationKeys.MODULE_NAME]!!
        val moduleContext = ContextForNewModule(
            ProjectContext(env.project, "Dart Kotlin Analyzer"),
            Name.special("<$moduleName>"),
            builtIns,
            platform = JsPlatforms.defaultJsPlatform, // TODO: JS reference
        )

        val thisModule = moduleContext.module

        if (isBuiltInsModule) {
            builtIns.builtInsModule = thisModule
        }

        thisModule.setDependencies(
            (setOf(thisModule) + dependencies).toList()
        )

        val container = createContainer(
            id = "DartKotlinAnalyzer",
            analyzerServices = DartPlatformAnalyzerServices,
        ) {
            configureModule(
                moduleContext,
                platform = JsPlatforms.defaultJsPlatform, // TODO: JS reference
                analyzerServices = DartPlatformAnalyzerServices,
                trace = trace,
                languageVersionSettings = config.languageVersionSettings,
            )

            configureStandardResolveComponents()
            useInstance(ExpectActualTracker.DoNothing)
            useInstance(
                FileBasedDeclarationProviderFactory(moduleContext.storageManager, files)
            )
            useInstance(InlineConstTracker.DoNothing)
            targetEnvironment.configure(this)
        }.apply {
            thisModule.initialize(
                CompositePackageFragmentProvider(
                    providers = listOf(
                        get<KotlinCodeAnalyzer>().packageFragmentProvider,
                        functionInterfacePackageFragmentProvider(moduleContext.storageManager, thisModule)
                    ),
                    debugName = "CompositeProvider: $thisModule"
                )
            )
        }

        container.get<LazyTopDownAnalyzer>().analyzeDeclarations(
            TopDownAnalysisMode.TopLevelDeclarations,
            files,
        )

        return when {
            trace.bindingContext.diagnostics.any { it.severity == Severity.ERROR } -> {
                AnalysisResult.compilationError(trace.bindingContext)
            }
            else -> AnalysisResult.success(trace.bindingContext, thisModule)
        }
    }
}