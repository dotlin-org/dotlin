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

package org.dotlin.compiler.backend.steps.ir2ast.lower

import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.builtins.ReplaceEnumValuesCalls
import org.dotlin.compiler.backend.steps.src2ir.IrResult
import org.jetbrains.kotlin.config.CompilerConfiguration
import kotlin.reflect.KFunction1

private typealias Lowering = KFunction1<DotlinLoweringContext, IrLowering>

private val lowerings: List<Lowering> = listOf(
    ::UnrepresentableDecimalConstsLowering,
    ::DartExtensionsLowering,
    ::ExternalDeclarationsLowering,
    ::IdentityChecksLowering,
    ::ReplaceEnumValuesCalls,
    ::EnumClassLowering,
    ::ConjunctionsDisjunctionsLowering,
    ::ComplexParametersLowering,
    ::LazyPropertiesLowering,
    ::DelegatedPropertiesLowering,
    DelegatedPropertiesLowering::Local,
    ::PropertiesReferencingThisLowering,
    ::PropertiesReferencingParametersLowering,
    ::PrivateParameterPropertyWithDefaultValuesLowering,
    ::CollectionImplementersLowering,
    ::OverriddenParametersLowering,
    ::DefaultInterfaceImplementationsLowering,
    ::OperatorsLowering,
    ExtensionsLowering::RemoveReceiverTypeArguments,
    ::ExtensionsLowering,
    ::SortStatementsLowering,
    ::ConstructorPassingComplexParamToSuperLowering,
    ::ComparableCompareToCallsLowering,
    ::CompareToCallsLowering,
    ::RemoveInstanceInitializersLowering,
    ::SecondaryRedirectingConstructorsWithBodiesLowering,
    ::ObjectLowering,
    ::NestedClassLowering,
    ::ReturnsLowering,
    ::ReturnsInFunctionExpressionsLowering,
    ::PropertyReferencesLowering,
    PropertyReferencesLowering::Local,
    ::PropertySimplifyingLowering,
    ::RemoveIntegerLiteralCastsLowering,
    ::RemoveThrowableCastsLowering,
    ::QualifiedSuperCallsLowering,
    ::WhensWithSubjectCastToNonNullLowering,
    ::WhensWithSubjectExpressionsLowering,
    ::PostfixIncrementsDecrementsLowering,
    ::SingleExpressionBlocksLowering,
    ::FunctionTypeIsChecksLowering,
    ::SafeCallsLowering,
    ::ElvisLowering,
    ::InitBlocksLowering,
    ::TypeParametersWithImplicitSuperTypesLowering,
    ::TypeParametersWithMultipleSuperTypesLowering,
    ::DefaultConstructorsLowering,
    ::TryExpressionsLowering,
    ::ConstInlineCallsLowering,
    ::ConstLambdaLiteralsLowering,
    ::ConstInlineFunctionsLowering,
    ::IrCompositesToIrBlocksLowering,
    ::ContravariantLowering,
    ::DartDifferentDefaultValueArgumentsLowering,
    ::CollectionTypeChecksLowering,
    ::CollectionFactoryCallsLowering,
    RuntimeCollectionTypeLowering::Declarations,
    RuntimeCollectionTypeLowering::Casts,
    ::DartImportsLowering
)

fun IrResult.lower(configuration: CompilerConfiguration, context: DotlinLoweringContext?): DotlinLoweringContext {
    val builtInsLowerings = when (module.descriptor) {
        module.descriptor.builtIns.builtInsModule -> listOf<Lowering>(
            ::FunctionSubtypeDeclarationsLowering
        )
        else -> emptyList()
    }

    return lower(configuration, context, builtInsLowerings + lowerings)
}

fun IrResult.lower(
    configuration: CompilerConfiguration,
    context: DotlinLoweringContext?,
    lowerings: List<Lowering>
): DotlinLoweringContext {
    val actualContext = context ?: DotlinLoweringContext(
        configuration,
        symbolTable,
        bindingContext = bindingTrace.bindingContext,
        irModuleFragment = module,
        dartNameGenerator,
        dartProject,
        irAttributes,
    )

    lowerings.forEach { lowering ->
        module.files.forEach {
            actualContext.enterFile(it)
            lowering(actualContext).lower(it)
        }
    }

    return actualContext
}