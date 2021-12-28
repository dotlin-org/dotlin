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

package org.dotlin.compiler.backend.steps.ir2ast.lower

import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.builtins.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.builtins.Comparable
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.resolve.BindingContext
import kotlin.reflect.KFunction1

private val lowerings: List<KFunction1<DartLoweringContext, IrLowering>> = listOf(
    Comparable::PreOperatorsLowering,
    ::DartBuiltInImportsLowering,
    ::ExternalDeclarationsLowering,
    ::EnumLowering,
    ::IdentityChecksLowering,
    ::AnnotatedExpressionsLowering,
    ::EnumClassLowering,
    ::GetEnumValueLowering,
    ::DartConstExpressionsInConstConstructorsLowering,
    ::ConjunctionsDisjunctionsLowering,
    ::ComplexParametersLowering,
    ::PropertiesReferencingThisLowering,
    ::PropertiesReferencingParametersLowering,
    ::OverriddenParametersLowering,
    ::DefaultInterfaceImplementationsLowering,
    ::OperatorsLowering,
    Comparable::PostOperatorsLowering,
    ::ExtensionsLowering,
    ::SortStatementsLowering,
    ::ConstructorPassingComplexParamToSuperLowering,
    ::CompareToCallsLowering,
    ::RemoveInstanceInitializersLowering,
    ::IteratorSubtypeBackingFieldsLowering,
    ::IteratorSubtypeReturnsLowering,
    ::IteratorLowering,
    ::ObjectLowering,
    ::NestedClassLowering,
    ::PropertySimplifyingLowering,
    ::QualifiedSuperCallsLowering,
    ::WhensWithSubjectStatementsLowering,
    ::WhensWithSubjectExpressionsLowering,
    ::SingleExpressionBlocksLowering,
    ::UnitReturnsLowering,
    ::SafeCallsLowering,
    ::ElvisLowering,
    ::InitBlocksLowering,
    ::MultipleTypeParametersLowering,
    ::DefaultConstructorsLowering,
    ::TryStatementsLowering,
    ::TryExpressionsLowering,
    ::InvokeCallsLowering,
    ::DartCodeLowering
)

fun IrModuleFragment.lower(
    configuration: CompilerConfiguration,
    symbolTable: SymbolTable,
    bindingContext: BindingContext
): DartLoweringContext {
    val context = DartLoweringContext(
        configuration,
        irModuleFragment = this,
        symbolTable = symbolTable,
        bindingContext = bindingContext
    )

    lowerings.forEach { lowering ->
        files.forEach { lowering(context).lower(it) }
    }

    return context
}