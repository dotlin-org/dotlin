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

package org.dotlin.compiler.backend.steps.ir2ast.transformer.util

import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.isPrivate
import org.dotlin.compiler.backend.steps.ir2ast.ir.owner
import org.dotlin.compiler.backend.steps.ir2ast.ir.toDart
import org.dotlin.compiler.backend.steps.util.DotlinAnnotations
import org.dotlin.compiler.backend.steps.util.getSingleAnnotationStringArgumentOf
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithVisibility
import org.jetbrains.kotlin.ir.types.IrDynamicType
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isNullable

fun IrType.toDart(context: DartTransformContext): DartTypeAnnotation {
    // TODO: Check for function type

    return when (this) {
        is IrSimpleType -> DartNamedType(
            name = owner.dartName,
            isNullable = isNullable(),
            // TODO isDeferred
            typeArguments = DartTypeArgumentList(arguments.map { it.toDart(context) }.toMutableList()),
        )
        is IrDynamicType -> DartTypeAnnotation.DYNAMIC
        else -> throw UnsupportedOperationException()
    }
}

val IrDeclarationWithName.dartName: DartSimpleIdentifier
    get() = dartNameOrNull.let {
        require(it != null) { "Name (${name.asString()}) cannot be special" }
        it
    }

val IrDeclarationWithName.dartNameOrNull: DartSimpleIdentifier?
    get() = getSingleAnnotationStringArgumentOf(DotlinAnnotations.dartName)?.let { DartSimpleIdentifier(it) }
        ?: if (!name.isSpecial) DartSimpleIdentifier(name.identifier) else null

val <D> D.dartName: DartSimpleIdentifier where D : IrDeclarationWithName, D : IrDeclarationWithVisibility
    @JvmName("dartNameWithVisibility")
    get() = (this as IrDeclarationWithName).dartName.let { if (isPrivate) it.asPrivate() else it }

val <D> D.dartNameOrNull: DartSimpleIdentifier? where D : IrDeclarationWithName, D : IrDeclarationWithVisibility
    @JvmName("dartNameOrNullWithVisibility")
    get() = (this as IrDeclarationWithName).dartNameOrNull.let { if (isPrivate) it?.asPrivate() else it }

fun <T> Iterable<T>.toPair(): Pair<T, T> {
    if (this.count() != 2) throw IllegalStateException("There must be exactly 2 elements to convert to a Pair")

    return first() to last()
}