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

import org.dotlin.compiler.backend.kotlin.Array
import org.dotlin.compiler.backend.kotlin.collections.Collection
import org.dotlin.compiler.backend.kotlin.collections.ImmutableList
import org.dotlin.compiler.backend.kotlin.collections.ImmutableMap
import org.dotlin.compiler.backend.kotlin.collections.ImmutableSet
import org.dotlin.compiler.backend.kotlin.collections.MutableCollection
import org.dotlin.compiler.backend.kotlin.collections.MutableList
import org.dotlin.compiler.backend.kotlin.collections.MutableMap
import org.dotlin.compiler.backend.kotlin.collections.MutableSet
import org.dotlin.compiler.backend.kotlin.collections.WriteableList
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrExpressionContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.irCall
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.common.lower.irNot
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.INSTANCEOF
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.NOT_INSTANCEOF
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeOrNull

/**
 * Transforms any use of [Collection] (or [MutableCollection]) that's not a supertype to
 * `dotlin.intrinsics.AnyCollection`. This is needed because [Collection] does not exist in Dart,
 * but in Dotlin it is a supertype of `List` and `Set`. Meaning that those types plus user types made
 * in Dotlin implementing [Collection] should be accepted whenever it's used.
 *
 * Must run after [CollectionImplementersLowering].
 */
// TODO: Assert in functions that the type is correct
@Suppress("UnnecessaryVariable")
class CollectionTypeChecksLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun DartLoweringContext.transform(
        expression: IrExpression,
        context: IrExpressionContext
    ): Transformation<IrExpression>? {
        if (expression !is IrTypeOperatorCall ||
            (expression.operator != INSTANCEOF && expression.operator != NOT_INSTANCEOF) ||
            expression.typeOperand.classFqName !in loweredTypes
        ) {
            return noChange()
        }

        val negated = expression.operator == NOT_INSTANCEOF

        val typeArguments = (expression.typeOperand as IrSimpleType).arguments

        // TODO: Optimize call to intrinsic functions: Inline them directly ourselves.

        return replaceWith(
            with(dartBuiltIns.dotlin) {
                buildStatement(context.container.symbol) {
                    val symbol = when (val fqName = expression.typeOperand.classFqName) {
                        Collection -> isCollection
                        MutableCollection -> isMutableCollection
                        ImmutableList -> isImmutableList
                        WriteableList -> isWriteableList
                        Array -> isFixedSizeList
                        MutableList -> isMutableList
                        ImmutableSet -> isImmutableSet
                        MutableSet -> isMutableSet
                        ImmutableMap -> isImmutableMap
                        MutableMap -> isMutableMap
                        else -> throw UnsupportedOperationException("Unsupported: $fqName")
                    }

                    irMaybeNot(
                        irCall(
                            symbol.owner,
                            receiver = expression.argument,
                            typeArguments = typeArguments.map { it.typeOrNull },
                            isExtension = true
                        ),
                        negated
                    )
                }
            }
        )
    }

    private val loweredTypes = listOf(
        Collection, MutableCollection,
        ImmutableList, WriteableList, MutableList,
        Array,
        ImmutableSet, MutableSet,
        ImmutableMap, MutableMap,
    )

    private fun IrSingleStatementBuilder.irMaybeNot(argument: IrExpression, not: Boolean) = when {
        not -> irNot(argument)
        else -> argument
    }
}