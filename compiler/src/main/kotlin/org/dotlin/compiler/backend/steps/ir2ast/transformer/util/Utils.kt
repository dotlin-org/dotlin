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

import org.dotlin.compiler.backend.dartAnnotatedName
import org.dotlin.compiler.backend.dartImportAliasPrefix
import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.isPrivate
import org.dotlin.compiler.backend.steps.ir2ast.ir.owner
import org.dotlin.compiler.backend.steps.ir2ast.ir.toDart
import org.dotlin.compiler.dart.ast.expression.identifier.*
import org.dotlin.compiler.dart.ast.type.DartNamedType
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList
import org.jetbrains.kotlin.ir.declarations.*
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

val IrDeclarationWithName.dartName: DartIdentifier
    get() = dartNameOrNull.let {
        require(it != null) { "Name (${name.asString()}) cannot be special" }
        it
    }

val IrDeclarationWithName.dartNameOrNull: DartIdentifier?
    get() {
        val prefix = dartImportAliasPrefix?.toDartSimpleIdentifier()

        val name = dartAnnotatedName?.toDartSimpleIdentifier()
            ?: if (!name.isSpecial) name.identifier.toDartSimpleIdentifier() else null

        return when {
            prefix != null && name != null -> DartPrefixedIdentifier(prefix, name)
            name != null -> name
            else -> null
        }
    }

val <D> D.dartName: DartIdentifier where D : IrDeclarationWithName, D : IrDeclarationWithVisibility
    @JvmName("dartNameWithVisibility")
    get() = (this as IrDeclarationWithName).dartName
        .let { if (it.isSimple() && isPrivate) it.asPrivate() else it }

val <D> D.dartNameOrNull: DartIdentifier? where D : IrDeclarationWithName, D : IrDeclarationWithVisibility
    @JvmName("dartNameOrNullWithVisibility")
    get() = (this as IrDeclarationWithName).dartNameOrNull
        .let { if (it?.isSimple() == true && isPrivate) it.asPrivate() else it }

val IrDeclarationWithName.dartNameAsSimple: DartSimpleIdentifier
    get() = dartName as DartSimpleIdentifier

val IrDeclarationWithName.dartNameAsSimpleOrNull: DartSimpleIdentifier?
    get() = dartNameOrNull as DartSimpleIdentifier?

/**
 * The [dartName] for this declaration. If it's a [DartPrefixedIdentifier], the prefix is removed.
 */
val IrDeclarationWithName.simpleDartName: DartSimpleIdentifier
    get() = when (val dartName = dartName) {
        is DartSimpleIdentifier -> dartName
        is DartPrefixedIdentifier -> dartName.identifier
    }

val IrDeclarationWithName.simpleDartNameOrNull: DartSimpleIdentifier?
    get() = when (val dartName = dartNameOrNull) {
        is DartSimpleIdentifier -> dartName
        is DartPrefixedIdentifier -> dartName.identifier
        else -> null
    }

// Some IR elements can be asserted that they always have simple identifiers.

val IrValueDeclaration.dartName: DartSimpleIdentifier
    get() = dartNameAsSimple

val IrField.dartName: DartSimpleIdentifier
    get() = dartNameAsSimple

val IrConstructor.dartName: DartSimpleIdentifier
    get() = dartNameAsSimple

val IrConstructor.dartNameOrNull: DartSimpleIdentifier?
    get() = dartNameAsSimpleOrNull

fun <T> Iterable<T>.toPair(): Pair<T, T> {
    if (this.count() != 2) throw IllegalStateException("There must be exactly 2 elements to convert to a Pair")

    return first() to last()
}