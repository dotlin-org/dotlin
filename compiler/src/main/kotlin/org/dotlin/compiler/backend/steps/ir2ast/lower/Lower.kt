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
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.builtins.Comparable
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.builtins.EnumLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.builtins.EnumValues
import org.dotlin.compiler.backend.steps.src2ir.IrResult
import org.jetbrains.kotlin.config.CompilerConfiguration
import kotlin.reflect.KFunction1

private typealias Lowering = KFunction1<DartLoweringContext, IrLowering>

private val lowerings: List<Lowering> = listOf(
    ::UnrepresentableDecimalConstsLowering,
    Comparable::PreOperatorsLowering,
    ::DartExtensionsLowering,
    ::ExternalDeclarationsLowering,
    ::EnumLowering,
    ::IdentityChecksLowering,
    ::EnumClassLowering,
    ::GetEnumValueLowering,
    EnumValues::ReplaceExpressionsLowering,
    EnumValues::RemoveDeclarationsLowering,
    ::ConjunctionsDisjunctionsLowering,
    ::ComplexParametersLowering,
    ::LazyPropertiesLowering,
    ::DelegatedPropertiesLowering,
    DelegatedPropertiesLowering::Local,
    ::PropertiesReferencingThisLowering,
    ::PropertiesReferencingParametersLowering,
    ::PrivateParameterPropertyWithDefaultValuesLowering,
    ::OverriddenParametersLowering,
    ::DefaultInterfaceImplementationsLowering,
    ::OperatorsLowering,
    Comparable::PostOperatorsLowering,
    ExtensionsLowering::RemoveReceiverTypeArguments,
    ::ExtensionsLowering,
    ::ConflictingExtensionCallsLowering,
    ::SortStatementsLowering,
    ::ConstructorPassingComplexParamToSuperLowering,
    ::CompareToCallsLowering,
    ::RemoveInstanceInitializersLowering,
    ::SecondaryRedirectingConstructorsWithBodiesLowering,
    ::ObjectLowering,
    ::NestedClassLowering,
    ::ReturnsLowering,
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
    ::InvokeCallsLowering,
    ::ConstLambdaLiteralsLowering,
    ::IrCompositesToIrBlocksLowering,
    ::ContravariantLowering,
    ::DartCodeLowering,
    ::DartImportsLowering
)

fun IrResult.lower(configuration: CompilerConfiguration, context: DartLoweringContext?): DartLoweringContext {
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
    context: DartLoweringContext?,
    lowerings: List<Lowering>
): DartLoweringContext {
    val actualContext = context ?: DartLoweringContext(
        configuration,
        irModuleFragment = module,
        symbolTable = symbolTable,
        bindingContext = bindingTrace.bindingContext,
        dartNameGenerator = dartNameGenerator,
        sourceRoot = sourceRoot,
        dartPackage = dartPackage,
        irAttributes = irAttributes
    )

    lowerings.forEach { lowering ->
        module.files.forEach {
            actualContext.enterFile(it)
            lowering(actualContext).lower(it)
        }
    }

    return actualContext
}