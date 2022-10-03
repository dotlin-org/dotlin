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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

// TODO: Analysis error when qualified super is used with a type which implementation uses private members
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class QualifiedSuperCallsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    private val propertyCopies = mutableMapOf<IrPropertySymbol, IrProperty>()
    private val declarationCopies = mutableMapOf<IrSimpleFunctionSymbol, IrSimpleFunction>()

    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrSimpleFunction || declaration.parent is IrFile) return noChange()

        val irFunction = declaration

        // Gather all super calls that are qualified.
        val qualifiedSuperCalls = irFunction.body?.statements
            ?.map { statement -> statement.filter<IrCall> { it.isQualifiedSuperCall(declaration.parent as? IrClass) } }
            ?.flatten()
            .orEmpty()

        if (qualifiedSuperCalls.isEmpty()) return noChange()

        // Copy the invoked declarations over with a mangled name.
        qualifiedSuperCalls.forEach {
            declarationCopies.computeIfAbsent(it.symbol) { symbol ->
                symbol.owner
                    .let { original ->
                        val originalDartName = when {
                            original.isGetter || original.isSetter -> original.correspondingProperty!!.simpleDartName
                            else -> original.dartName
                        }

                        val copyName = Name.identifier(
                            original.parentAsClass.simpleDartName.toString() +
                                    "$" +
                                    originalDartName
                        )

                        original.deepCopyWith(remapReferences = false) {
                            name = copyName
                            visibility = DescriptorVisibilities.PRIVATE
                        }.apply {
                            correspondingPropertySymbol =
                                original.correspondingPropertySymbol?.let { originalPropertySymbol ->
                                    propertyCopies.computeIfAbsent(originalPropertySymbol) {
                                        originalPropertySymbol.owner.deepCopyWith(remapReferences = false) {
                                            name = copyName
                                            visibility = DescriptorVisibilities.PRIVATE
                                        }.also { propertyCopy ->
                                            when {
                                                original.isGetter -> propertyCopy.getter = this@apply
                                                original.isSetter -> propertyCopy.setter = this@apply
                                            }
                                        }
                                    }.symbol
                                }
                            overriddenSymbols = emptyList()
                        }
                    }
                    .also { copy -> irFunction.parentAsClass.addChild(copy) }
            }
        }

        // Remap calls to the new copied declaration.
        irFunction.body?.transformChildrenVoid(
            object : IrCustomElementTransformerVoid() {
                override fun visitCall(expression: IrCall): IrExpression {
                    expression.transformChildrenVoid()

                    if (expression in qualifiedSuperCalls) {
                        return IrCallImpl(
                            expression.startOffset,
                            expression.endOffset,
                            expression.type,
                            symbol = declarationCopies[expression.symbol]!!.symbol,
                            expression.typeArgumentsCount,
                            expression.valueArgumentsCount,
                            expression.origin,
                            superQualifierSymbol = null
                        ).apply {
                            copyAttributes(expression)
                            copyTypeAndValueArgumentsFrom(expression)
                        }
                    }

                    return expression
                }
            }
        )

        return noChange()
    }
}
