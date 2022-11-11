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

import org.dotlin.compiler.backend.kotlin
import org.dotlin.compiler.backend.steps.ir2ast.DartIrBuiltIns
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.*
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.impl.IrTypeAbbreviationBuilder
import org.jetbrains.kotlin.ir.types.impl.build
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.types.impl.toBuilder

/**
 * Transforms any use of [Collection] (or [MutableCollection] that's not a supertype to
 * `dotlin.intrinsics.AnyCollection`. This is needed because [Collection] does not exist in Dart,
 * but in Dotlin it is a supertype of `List` and `Set`. Meaning that those types plus user types made
 * in Dotlin implementing [Collection] should be accepted whenever it's used.
 */
// TODO: Assert in functions that the type is correct
@Suppress("UnnecessaryVariable")
class RuntimeCollectionTypeLowering private constructor() {
    class Declarations(override val context: DartLoweringContext) : IrDeclarationLowering {
        override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
            fun <D : IrDeclaration> D.transform(type: IrType, set: D.(IrType) -> Unit) {
                if (type.isCollection()) {
                    set(type.toAnyCollection(context.dartBuiltIns))
                }
            }

            declaration.apply {
                when (this) {
                    is IrVariable -> transform(type) { type = it }
                    is IrFunction -> valueParameters.onEach { param ->
                        param.apply {
                            transform(type) { type = it }
                        }
                    }
                    is IrField -> transform(type) { type = it }
                }
            }

            return noChange()
        }
    }

    class Casts(override val context: DartLoweringContext) : IrExpressionLowering {
        override fun DartLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
            if (expression !is IrTypeOperatorCall) {
                return noChange()
            }

            when (expression.operator) {
                CAST, IMPLICIT_CAST, IMPLICIT_DYNAMIC_CAST -> {
                    expression.typeOperand = expression.typeOperand.toAnyCollection(context.dartBuiltIns)
                    expression.type = expression.typeOperand
                }
                else -> {}
            }

            return noChange()
        }
    }
}

private fun IrType.isCollection() =
    classFqName in listOf(kotlin.collections.Collection, kotlin.collections.MutableCollection)

private fun IrType.toAnyCollection(builtIns: DartIrBuiltIns): IrType {
    if (!isCollection() || this !is IrSimpleType) return this

    val thisType = this

    // The expanded type will stay Collection<T> or MutableCollection<T>, but in Dart it will
    // be dynamic.
    return toBuilder().run {
        abbreviation = with(IrTypeAbbreviationBuilder()) {
            typeAlias = builtIns.dotlin.anyCollection
            arguments = thisType.arguments
            hasQuestionMark = thisType.hasQuestionMark
            build()
        }
        buildSimpleType()
    }
}

