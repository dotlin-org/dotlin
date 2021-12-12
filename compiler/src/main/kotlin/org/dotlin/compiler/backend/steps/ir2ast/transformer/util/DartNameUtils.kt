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
import org.dotlin.compiler.backend.steps.ir2ast.ir.isPrivate
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartPrefixedIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.toDartSimpleIdentifier
import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.parentClassOrNull

private fun IrDeclarationWithName.getDartNameOrNull(allowNested: Boolean): DartIdentifier? {
    val aliasPrefix = dartImportAliasPrefix?.toDartSimpleIdentifier()

    var name = dartAnnotatedName?.toDartSimpleIdentifier()
        ?: when {
            !name.isSpecial -> name.identifier.toDartSimpleIdentifier()
            this is IrConstructor -> {
                val constructors = parentClassOrNull?.declarations?.filterIsInstance<IrConstructor>() ?: emptyList()

                when {
                    constructors.size <= 1 -> when {
                        // If a constructor is private with no name, we set the name to "_".
                        isPrivate -> DartSimpleIdentifier("_")
                        else -> null
                    }
                    // If have multiple constructors (and this is not the primary constructor, which by
                    // default has no name), they're numbered in the order of appearance,
                    // e.g. `MyClass.$constructor$0`.
                    !isPrimary -> DartSimpleIdentifier("\$constructor$${constructors.indexOf(this)}")
                    else -> null
                }
            }

            else -> null
        }

    // Nested classes, interfaces, etc.
    if (allowNested && this is IrClass && parentClassOrNull != null) {
        name = parents
            .filterIsInstance<IrClass>()
            .toList()
            .reversed()
            .map { it.getDartNameOrNull(allowNested = false)!! }
            .plus(name)
            .joinToString(separator = "$")
            .toDartSimpleIdentifier()
    }

    if (this is IrDeclarationWithVisibility) {
        name = when {
            // Start name with underscore if the declaration is private and name didn't already start with one.
            isPrivate && name?.isPrivate == false -> name.asPrivate()
            // If a name starts with an underscore but is not for a private declaration, remove the underscore(s).
            !isPrivate && name?.isPrivate == true -> name.value.replace(Regex("^_+"), "").toDartSimpleIdentifier()
            else -> name
        }
    }

    return when {
        aliasPrefix != null && name != null -> DartPrefixedIdentifier(aliasPrefix, name)
        name != null -> name
        else -> null
    }
}

val IrDeclarationWithName.dartName: DartIdentifier
    get() = dartNameOrNull.let {
        require(it != null) { "Name (${name.asString()}) cannot be special" }
        it
    }

val IrDeclarationWithName.dartNameOrNull: DartIdentifier?
    get() = getDartNameOrNull(allowNested = true)

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