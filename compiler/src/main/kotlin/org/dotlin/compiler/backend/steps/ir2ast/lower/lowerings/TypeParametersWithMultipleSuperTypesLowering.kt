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
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrNullAwareExpression
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.superTypes
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.types.Variance

class TypeParametersWithMultipleSuperTypesLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    private val originalSuperTypes = mutableMapOf<IrTypeParameter, List<IrType>>()

    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrTypeParametersContainer || declaration.typeParameters.all { it.superTypes.size < 2 }) {
            return noChange()
        }

        val relevantTypeParameters = declaration.typeParameters.filter { it.superTypes.size >= 2 }

        val newSuperTypes = relevantTypeParameters
            .associateWith { it.superSuperTypes() }
            .map { (param, superTypesBySuperTypes) ->
                param to (superTypesBySuperTypes.values
                    .reduce { acc, types -> acc.intersect(types) } // These are the common super types.
                    .firstOrNull() ?: irBuiltIns.anyType.makeNullable()) // First is the nearest super type.
            }
            .map { (param, superType) ->
                when {
                    // If all original super types of all type parameters are nullable,
                    // the new super type should be too.
                    relevantTypeParameters
                        .map { it.defaultType.superTypes() }
                        .flatten()
                        .all { it.isNullable() } -> {
                        param to superType.makeNullable()
                    }
                    else -> param to superType
                }
            }
            .toMap()

        relevantTypeParameters.forEach {
            originalSuperTypes[it] = it.superTypes
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

                    // No need to cast if the type is already exactly correct.
                    if (type == castType) return

                    val matchedTypeParameter = relevantTypeParameters
                        .firstOrNull { type == it.defaultType }
                        ?: return

                    val matchedType = matchedTypeParameter.defaultType

                    var actualCastType = castType.withArgumentsFrom(originalSuperTypes[matchedTypeParameter]!!)

                    actualCastType = when {
                        isInNullAware || matchedType.isNullable() -> actualCastType.makeNullable()
                        else -> actualCastType
                    }

                    // We don't need to cast if the types are polymorphically equivalent.
                    if (type polymorphicallyIs actualCastType) {
                        return
                    }

                    actualCastType = actualCastType.makeArgumentsDynamicIfNecessary()

                    set(
                        IrTypeOperatorCallImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            type = actualCastType,
                            operator = IrTypeOperator.CAST,
                            typeOperand = actualCastType,
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

    private fun IrType.withArgumentsFrom(originalSuperTypes: List<IrType>): IrType {
        if (this !is IrSimpleType) return this

        val originalSuperType =
            originalSuperTypes.firstOrNull { it.classifierOrNull == this.classifierOrNull } as? IrSimpleType
                ?: return this

        return IrSimpleTypeImpl(
            originalKotlinType,
            classifier,
            hasQuestionMark,
            arguments = originalSuperType.arguments,
            annotations,
            abbreviation
        )
    }

    private fun IrType.makeArgumentsDynamicIfNecessary(): IrType {
        if (this !is IrSimpleType) return this

        return IrSimpleTypeImpl(
            originalKotlinType,
            classifier,
            hasQuestionMark,
            arguments = arguments.map {
                val typeParam = it.typeOrNull?.typeParameterOrNull ?: return@map it

                when {
                    typeParam.originalSuperTypes.size >= 2 -> makeTypeProjection(
                        context.dynamicType,
                        Variance.INVARIANT
                    )
                    else -> it
                }
            },
            annotations,
            abbreviation
        )
    }

    private val IrTypeParameter.originalSuperTypes: List<IrType>
        get() = this@TypeParametersWithMultipleSuperTypesLowering.originalSuperTypes[this] ?: superTypes
}