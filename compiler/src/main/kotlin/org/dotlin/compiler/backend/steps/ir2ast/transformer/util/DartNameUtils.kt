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
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.util.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartPrefixedIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.toDartSimpleIdentifier
import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.parentClassOrNull

private fun IrDeclarationWithName.getDartNameOrNull(allowNested: Boolean): DartIdentifier? {
    val aliasPrefix = dartImportAliasPrefix?.toDartSimpleIdentifier()

    val annotatedName = dartAnnotatedName?.toDartSimpleIdentifier()

    var name = annotatedName ?: when {
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

    // Handle function overloads.
    if (name != null && annotatedName == null && this is IrSimpleFunction && isOverload && !isRootOverload) {
        val baseOverload = baseOverload
        val uniqueParameters = when {
            rootOverload != baseOverload -> uniqueValueParametersComparedTo(rootOverload)
            else -> emptyList()
        }
        val uniqueTypeParameters = uniqueTypeParametersComparedTo(rootOverload)

        var uniqueValueTypeSuffix = ""
        var needsTypeParamBoundInfo = false

        if (this != baseOverload) {
            needsTypeParamBoundInfo = true

            // Find the first unique type and use that as a suffix.
            val ourValueTypes = valueParameters.map { it.type }.toSet()
            val overloadValueTypes = overloads
                .map { it.valueParameters.map { param -> param.type } }
                .flatten()
                .toSet()

            uniqueValueTypeSuffix =
                ourValueTypes.subtract(overloadValueTypes).firstOrNull().let {
                    when (it?.classOrNull) {
                        null -> ""
                        else -> it.dartNameWith(superTypes = true)
                    }
                }
        }

        val uniqueParametersPart = when {
            uniqueParameters.isNotEmpty() -> "With" + uniqueParameters
                .mapIndexed { index, parameter ->
                    val part = parameter.name.toString().sentenceCase()

                    when {
                        uniqueParameters.isLastIndexAndNotSingle(index) -> "And$part"
                        else -> part
                    }
                }
                .joinToString(separator = "")
            else -> ""
        }

        val uniqueTypeParametersPart = when {
            uniqueParameters.isEmpty() && uniqueTypeParameters.isNotEmpty() -> "WithGeneric" + uniqueTypeParameters
                .mapIndexed { index, parameter ->
                    val part = parameter.dartNameWith(needsTypeParamBoundInfo)

                    when {
                        uniqueTypeParameters.isLastIndexAndNotSingle(index) -> "And$part"
                        else -> part
                    }
                }
                .joinToString(separator = "")
            else -> ""
        }

        name = name.copy(suffix = uniqueParametersPart + uniqueTypeParametersPart + uniqueValueTypeSuffix)
    }

    // TODO: Handle case if there's a nested class named "Companion" (error or different name)?
    // Nested classes, interfaces, etc.
    if (allowNested && annotatedName == null && this is IrClass && parentClassOrNull != null) {
        name = parents
            .filterIsInstance<IrClass>()
            .toList()
            .reversed()
            .map { it.getDartNameOrNull(allowNested = false)!! }
            .plus(name)
            .joinToString(separator = "$")
            .toDartSimpleIdentifier()
    }

    // Instance methods from objects get prefixed with '$'.
    if (isFromObjectAndStaticallyAvailable) {
        name = name?.asGenerated()
    }

    // Property backing fields are prefixed with '$' and suffixed with 'BackingField'.
    if (this is IrField && isExplicitBackingField) {
        name = name?.copy(isGenerated = true, suffix = "BackingField")
    }

    if (this is IrDeclarationWithVisibility) {
        name = when {
            // Start name with underscore if the declaration is private and name didn't already start with one.
            isPrivate && name?.isPrivate == false -> name.asPrivate()
            // If a name starts with an underscore but is not for a private declaration, remove the underscore(s).
            !isPrivate && name?.isPrivate == true -> name.copy(isPrivate = false)
            else -> name
        }
    }

    return when {
        aliasPrefix != null && name != null -> DartPrefixedIdentifier(aliasPrefix, name)
        else -> name
    }
}

private fun IrType?.dartNameWith(superTypes: Boolean): String =
    when (val classifier = this?.classifierOrNull?.owner) {
        is IrClass -> when (this) {
            is IrSimpleType -> classifier.name.toString() + arguments.mapNotNull {
                it.typeOrNull?.dartNameWith(superTypes)
            }.let {
                when {
                    it.isNotEmpty() -> "With" + it.joinToString(separator = "And")
                    else -> ""
                }
            }
            else -> classifier.name.toString()
        }
        is IrTypeParameter -> classifier.dartNameWith(superTypes = false)
        else -> ""
    }

fun IrTypeParameter.dartNameWith(superTypes: Boolean): String {
    val namePart = name.toString().sentenceCase()
    val boundPart by lazy {
        this.superTypes.mapIndexed { index, superType ->
            val superTypePart = superType.dartNameWith(superTypes)

            when {
                this.superTypes.isLastIndexAndNotSingle(index) -> "And$superTypePart"
                else -> superTypePart
            }
        }.joinToString(separator = "")
    }

    return namePart + when {
        superTypes && boundPart.isNotEmpty() -> "MustBe$boundPart"
        else -> ""
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

private fun List<*>.isLastIndexAndNotSingle(index: Int) = index == size - 1 && size != 1