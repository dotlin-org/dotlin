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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementTransformerVoid
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrNullAwareExpression
import org.dotlin.compiler.backend.steps.ir2ast.ir.firstNonFakeOverrideOrSelf
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.superTypes
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class MultipleTypeParametersLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrTypeParametersContainer || declaration.typeParameters.all { it.superTypes.size < 2 }) {
            return noChange()
        }

        val newSuperTypes = declaration.typeParameters
            .associateWith { it.superSuperTypes() }
            .map { (param, superTypesBySuperTypes) ->
                param to superTypesBySuperTypes.values
                    .reduce { acc, types -> acc.intersect(types) } // These are the common super types.
                    .first() // First is the nearest super type.
            }
            .map { (param, superType) ->
                when {
                    // If all original super types of all type parameters are nullable, the new super type should be too.
                    declaration.typeParameters
                        .map { it.defaultType.superTypes() }
                        .flatten()
                        .all { it.isNullable() } -> {
                        param to superType.makeNullable()
                    }
                    else -> param to superType
                }
            }
            .toMap()

        declaration.typeParameters.forEach {
            it.superTypes = listOf(newSuperTypes[it]!!)
        }

        // Add explicit casts of relevant types.
        declaration.transformChildrenVoid(
            object : IrCustomElementTransformerVoid() {
                fun IrExpression?.possiblyCastReceiver(
                    of: IrDeclarationReference,
                    isInNullAware: Boolean,
                    set: (IrTypeOperatorCall) -> Unit
                ) = this?.possiblyCast(
                    castType = (of.symbol.owner as IrDeclaration)
                        .firstNonFakeOverrideOrSelf()
                        .parentClassOrNull
                        ?.defaultType,
                    isInNullAware,
                    set,
                )

                fun IrExpression?.possiblyCastArgument(
                    of: IrDeclarationReference,
                    index: Int,
                    isInNullAware: Boolean,
                    set: (IrTypeOperatorCall) -> Unit
                ) = this?.possiblyCast(
                    castType = (of.symbol.owner as? IrFunction)?.valueParameters?.get(index)?.type,
                    isInNullAware,
                    set,
                )

                fun IrExpression.possiblyCast(
                    castType: IrType?,
                    isInNullAware: Boolean,
                    set: (IrTypeOperatorCall) -> Unit
                ) {
                    if (castType == null) return

                    val matchedType = declaration.typeParameters
                        .map { it.defaultType }
                        .firstOrNull { type == it } ?: return

                    fun IrType.makeNullableIfNecessary() = when {
                        isInNullAware || matchedType.isNullable() -> makeNullable()
                        else -> this
                    }

                    set(
                        IrTypeOperatorCallImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            type = castType.makeNullableIfNecessary(),
                            operator = IrTypeOperator.CAST,
                            typeOperand = castType.makeNullableIfNecessary(),
                            argument = this
                        )
                    )
                }

                fun visitMemberAccess(expression: IrMemberAccessExpression<*>, isInNullAware: Boolean) =
                    expression.apply {
                        transformChildrenVoid()

                        dispatchReceiver.possiblyCastReceiver(this, isInNullAware) { dispatchReceiver = it }
                        extensionReceiver.possiblyCastReceiver(this, isInNullAware) { dispatchReceiver = it }

                        for (i in 0 until valueArgumentsCount) {
                            getValueArgument(i).possiblyCastArgument(this, i, isInNullAware) { putValueArgument(i, it) }
                        }
                    }

                fun visitFieldAccess(expression: IrFieldAccessExpression, isInNullAware: Boolean) =
                    expression.apply {
                        transformChildrenVoid()

                        receiver.possiblyCastReceiver(this, isInNullAware) { receiver = it }
                    }

                override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) =
                    visitMemberAccess(expression, isInNullAware = false)

                override fun visitFieldAccess(expression: IrFieldAccessExpression) =
                    visitFieldAccess(expression, isInNullAware = false)

                override fun visitNullAwareExpression(expression: IrNullAwareExpression): IrNullAwareExpression {
                    fun IrExpression.wrap() = IrNullAwareExpression(this)

                    return when (val baseExpression = expression.expression) {
                        is IrMemberAccessExpression<*> -> visitMemberAccess(baseExpression, isInNullAware = true).wrap()
                        is IrFieldAccessExpression -> visitFieldAccess(baseExpression, isInNullAware = true).wrap()
                        else -> expression
                    }
                }
            }
        )

        return noChange()
    }

    private fun IrTypeParameter.superSuperTypes() = superTypes.associateWith { it.allSuperTypes() }

    private fun IrType.allSuperTypes(): Set<IrType> =
        superTypes().map { listOf(it, *it.allSuperTypes().toTypedArray()) }.flatten().toSet()
}