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

import org.dotlin.compiler.backend.DartProject
import org.dotlin.compiler.backend.descriptors.DartDescriptorContext
import org.dotlin.compiler.backend.descriptors.DartPackageFragmentProvider
import org.dotlin.compiler.backend.steps.src2ir.DartElementLocator
import org.dotlin.compiler.backend.steps.src2ir.DartPlatform
import org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.file.DartDuplicateImportChecker
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
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory

class DotlinAnalyzer(
    private val env: KotlinCoreEnvironment,
    private val config: CompilerConfiguration,
    private val dartProject: DartProject,
    private val dartElementLocator: DartElementLocator,
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
            platform = DartPlatform,
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
            analyzerServices = DotlinPlatformAnalyzerServices,
        ) {
            configureModule(
                moduleContext,
                platform = DartPlatform,
                analyzerServices = DotlinPlatformAnalyzerServices,
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
                    providers = listOfNotNull(
                        get<ResolveSession>().packageFragmentProvider,
                        DartPackageFragmentProvider(
                            dartProject,
                            DartDescriptorContext(
                                thisModule,
                                dartProject,
                                dartElementLocator,
                                moduleContext.storageManager
                            ),
                        ),
                        when {
                            isBuiltInsModule -> functionInterfacePackageFragmentProvider(
                                moduleContext.storageManager,
                                thisModule
                            )

                            else -> null
                        }
                    ),
                    debugName = "CompositeProvider: $thisModule"
                )
            )
        }

        container.get<LazyTopDownAnalyzer>().analyzeDeclarations(
            TopDownAnalysisMode.TopLevelDeclarations,
            files,
        )

        checkSourceFiles(files, trace)

        return when {
            trace.bindingContext.diagnostics.any { it.severity == Severity.ERROR } -> {
                AnalysisResult.compilationError(trace.bindingContext)
            }

            else -> AnalysisResult.success(trace.bindingContext, thisModule)
        }
    }

    private val sourceFileCheckers = listOf(
        ::DartDuplicateImportChecker
    )

    private fun checkSourceFiles(files: List<KtFile>, trace: BindingTrace) {
        sourceFileCheckers.forEach {
            val checker = it(trace)

            files.forEach {
                checker.check(it)
            }
        }
    }
}