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

package org.dotlin.compiler.backend.util

import org.dotlin.compiler.backend.steps.ir2ast.ir.getValueArgumentOrDefault
import org.jetbrains.kotlin.backend.jvm.ir.getAnnotationRetention
import org.jetbrains.kotlin.descriptors.annotations.KotlinRetention
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.FqName

@Suppress("UNCHECKED_CAST")
fun <T> IrAnnotationContainer.getAnnotationArgumentOf(fqName: FqName): T? =
    getAnnotation(fqName)
        ?.getValueArgumentOrDefault(0)
        ?.let { it as IrConst<T> }
        ?.value


@Suppress("UNCHECKED_CAST")
fun <T> IrAnnotationContainer.getOverriddenAnnotationArgumentOf(fqName: FqName): T? =
    getAnnotationArgumentOf(fqName) ?: getFromOverride { it.getOverriddenAnnotationArgumentOf<T>(fqName) }

@Suppress("UNCHECKED_CAST")
fun <T0, T1> IrAnnotationContainer.getTwoAnnotationArgumentsOf(fqName: FqName): Pair<T0, T1>? =
    getAnnotation(fqName)
        ?.let { (0..1).map { i -> it.getValueArgumentOrDefault(i) } }
        ?.let { listOf(it[0] as IrConst<T0>, it[1] as IrConst<T1>) }
        ?.let { Pair(it[0].value as T0, it[1].value as T1) }

@Suppress("UNCHECKED_CAST")
fun <T0, T1, T2> IrAnnotationContainer.getThreeAnnotationArgumentsOf(fqName: FqName): Triple<T0, T1, T2>? =
    getAnnotation(fqName)
        ?.let { (0..2).map { i -> it.getValueArgumentOrDefault(i) } }
        ?.let { listOf(it[0] as IrConst<T0>, it[1] as IrConst<T1>, it[2] as IrConst<T2>) }
        ?.let { Triple(it[0].value as T0, it[1].value as T1, it[2].value as T2) }

fun IrAnnotationContainer.getSingleAnnotationStringArgumentOf(fqName: FqName) = getAnnotationArgumentOf<String>(fqName)

fun IrAnnotationContainer.getSingleOverriddenAnnotationStringArgumentOf(fqName: FqName) =
    getOverriddenAnnotationArgumentOf<String>(fqName)

fun IrAnnotationContainer.getSingleAnnotationTypeArgumentOf(fqName: FqName) =
    getAnnotation(fqName)?.getTypeArgument(0)

fun IrAnnotationContainer.getAnnotation(fqName: FqName) = getAnnotation(fqName)

fun IrAnnotationContainer.hasAnnotation(fqName: FqName) = hasAnnotation(fqName)

fun IrDeclaration.hasOverriddenAnnotation(fqName: FqName): Boolean =
    hasAnnotation(fqName) || getFromOverride { it.hasOverriddenAnnotation(fqName) } == true

private fun <R> Any.getFromOverride(block: (IrDeclaration) -> R): R? {
    return when (this) {
        // For value parameters, we look at the parent function.
        is IrValueParameter -> when (val parent = this.parent) {
            is IrSimpleFunction -> parent.overriddenSymbols.map {
                ((it.owner as? IrSimpleFunction)?.valueParameters?.get(this.index))?.let(block)
            }.firstOrNull()

            else -> null
        }

        is IrOverridableDeclaration<*> -> overriddenSymbols.map {
            (it.owner as? IrDeclaration)?.let(block)
        }.firstOrNull()

        else -> null
    }
}

fun IrMutableAnnotationContainer.annotate(symbol: IrClassSymbol, vararg arguments: IrExpression) {
    annotations = annotations + createAnnotation(symbol, *arguments)
}

fun createAnnotation(symbol: IrClassSymbol, vararg arguments: IrExpression) = IrConstructorCallImpl(
    SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
    type = symbol.owner.defaultType,
    symbol = symbol.owner.primaryConstructor!!.symbol,
    typeArgumentsCount = 0,
    constructorTypeArgumentsCount = 0,
    valueArgumentsCount = arguments.size,
).apply {
    arguments.forEachIndexed { index, arg -> putValueArgument(index, arg) }
}

val IrAnnotationContainer.annotationsWithRuntimeRetention: List<IrConstructorCall>
    get() = annotations.filter { it.symbol.owner.parentAsClass.getAnnotationRetention() != KotlinRetention.SOURCE }
