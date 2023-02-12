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

package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.DartNameGenerator
import org.dotlin.compiler.backend.DartProject
import org.dotlin.compiler.backend.attributes.IrAttributes
import org.dotlin.compiler.backend.steps.ir2ast.IrExpressionSourceMapper
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.lower
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.output.AnnotateDartConstDeclarationsLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.output.AnnotateExternalCompanionObjectsLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.output.AnnotateSpecialInheritanceTypes
import org.dotlin.compiler.backend.steps.src2ir.analyze.DartKotlinAnalyzerReporter
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.DartIrAnalyzer
import org.jetbrains.kotlin.backend.common.serialization.DeserializationStrategy
import org.jetbrains.kotlin.backend.common.serialization.signature.IdSignatureDescriptor
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.descriptors.konan.kotlinLibrary
import org.jetbrains.kotlin.ir.builders.TranslationPluginContext
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.util.IrMessageLogger
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi2ir.Psi2IrConfiguration
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.resolve.BindingTraceContext

fun sourceToIr(
    env: KotlinCoreEnvironment,
    config: CompilerConfiguration,
    dartProject: DartProject
): IrResult {
    val sourceFiles = env.getSourceFiles()

    var ir = loadIr(
        env,
        config,
        sourceFiles,
        irFactory = IrFactoryImpl,
        dartProject,
    )

    // If the Dart package is a library, we must specify const declarations
    // that are not normally const in Kotlin. Otherwise, this information is lost in the output IR.
    if (dartProject.compileKlib) {
        ir = ir.copy(
            loweringContext = ir.lower(
                config,
                context = null,
                listOf(
                    ::AnnotateDartConstDeclarationsLowering,
                    ::AnnotateExternalCompanionObjectsLowering,
                    ::AnnotateSpecialInheritanceTypes
                )
            )
        )
    }

    return ir
}

private fun loadIr(
    env: KotlinCoreEnvironment,
    config: CompilerConfiguration,
    files: List<KtFile>,
    irFactory: IrFactory,
    dartProject: DartProject
): IrResult {
    val elementLocator = DartElementLocator()
    val builtIns = DotlinBuiltIns()
    val dependencyDartModules = DartPackageModuleResolver.resolve(dartProject, builtIns, elementLocator)

    val isCompilingBuiltIns = dependencyDartModules.none { it.isStdlib }

    if (!isCompilingBuiltIns) {
        dependencyDartModules.setDependencies()
    }

    val trace = BindingTraceContext()

    val analysisResult = DartKotlinAnalyzerReporter(env, config, dartProject, elementLocator).analyzeAndReport(
        files,
        dependencies = when {
            // We don't pass dependencies to the analyzer when compiling built-ins, because the only dependency
            // the built-ins have is the "meta" package, which is not used in Kotlin code. However, we must not pass
            // it as a dependency to the analyzer, we'll get errors otherwise.
            isCompilingBuiltIns -> emptyList()
            else -> dependencyDartModules.map { it.impl }
        },
        isCompilingBuiltIns,
        builtIns,
        trace
    )

    val mainModule = analysisResult.moduleDescriptor

    if (isCompilingBuiltIns) {
        dependencyDartModules.setDependencies(builtInsModule = mainModule as ModuleDescriptorImpl)
    }

    val psi2Ir = Psi2IrTranslator(
        config.languageVersionSettings,
        Psi2IrConfiguration(ignoreErrors = false)
    )
    val symbolTable = SymbolTable(IdSignatureDescriptor(DotlinDescriptorBasedMangler), irFactory)
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

    val irLinker = DotlinIrLinker(
        currentModule = mainModule,
        IrMessageLogger.None,
        psi2IrContext.irBuiltIns,
        symbolTable,
        frontEndContext,
    )

    val modulesByIrFragments = mutableMapOf<ModuleDescriptor, IrModuleFragment>()

    if (!isCompilingBuiltIns) {
        mainModule.builtIns.builtInsModule.let {
            modulesByIrFragments[it] = irLinker.deserializeIrModuleHeader(
                moduleDescriptor = it,
                kotlinLibrary = it.kotlinLibrary,
                // For built-ins, we want everything.
                deserializationStrategy = { DeserializationStrategy.ALL }
            )
        }
    }

    dependencyDartModules
        .filter { it.impl != mainModule.builtIns.builtInsModule } // Built-ins module is already done above.
        .associateWithTo(modulesByIrFragments) {
        irLinker.deserializeIrModuleHeader(it, it.klib, _moduleName = it.name.asString())
    }

    val dartIrProviders = dependencyDartModules.plus(mainModule)
        .map { DartIrProvider(it, symbolTable, psi2IrContext.irBuiltIns, modulesByIrFragments[it]) }
    val mainModuleDartIrProvider = dartIrProviders.last()

    val irModule = psi2Ir.generateModuleFragment(
        context = psi2IrContext,
        ktFiles = files,
        irProviders = buildList {
            // Dart IR providers must come first.
            addAll(dartIrProviders)
            add(irLinker)
        },
        linkerExtensions = emptyList(),
    )

    mainModuleDartIrProvider.setModuleFiles(irModule)

    val extraIrAttributes = IrAttributes.Default()

    IrExpressionSourceMapper.run(irModule.files, extraIrAttributes)

    val dartNameGenerator = DartNameGenerator()

    DartIrAnalyzer(
        irModule,
        trace,
        symbolTable,
        dartNameGenerator,
        dartProject,
        config,
        extraIrAttributes
    ).analyzeAndReport()
        .also {
            it.throwIfHasErrors()
        }

    return IrResult(
        irModule,
        trace,
        symbolTable,
        extraIrAttributes,
        dartNameGenerator,
        dartProject,
        loweringContext = null
    )
}

data class IrResult(
    val module: IrModuleFragment,
    val bindingTrace: BindingTraceContext,
    val symbolTable: SymbolTable,
    val irAttributes: IrAttributes,
    val dartNameGenerator: DartNameGenerator,
    val dartProject: DartProject,
    /**
     * A lowering context might be available if the IR output was already (partially) lowered.
     */
    val loweringContext: DotlinLoweringContext?
)