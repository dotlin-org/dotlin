/*
 * Copyright 2022 Wilko Manger
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

package org.dotlin.compiler.backend

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.util.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartPrefixedIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.toDartIdentifier
import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.isSetter
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.konan.file.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute

class DartNameGenerator {
    private fun IrContext.dartNameOrNullOf(
        declaration: IrDeclarationWithName,
        currentFile: IrFile,
        allowNested: Boolean,
        useKotlinAlias: Boolean = true
    ): DartIdentifier? =
        declaration.run {
            val aliasPrefix = dartLibraryAlias?.toDartIdentifier() ?: when {
                useKotlinAlias -> importAliasIn(currentFile)?.toDartIdentifier()
                else -> null
            }
            val annotatedName = dartAnnotatedName?.toDartIdentifier()

            // If this declaration is the implementation of an external Dart interface, return the name of that interface.
            (this as? IrClass)?.correspondingDartInterface?.dartNameOrNull?.let { return it }

            var name = annotatedName ?: when {
                !name.isSpecial -> name.identifier.toDartIdentifier()
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
                isSetter -> when {
                    name.isSpecial -> (this as IrSimpleFunction).correspondingProperty!!.simpleDartName
                    else -> dartNameAsSimple
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
                                else -> it.dartNameValueWith(superTypes = true)
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
                            val part = parameter.dartNameValueWith(needsTypeParamBoundInfo)

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
                    .map { dartNameOrNullOf(it, currentFile, allowNested = false)!! }
                    .plus(name)
                    .joinToString(separator = "$")
                    .toDartIdentifier()
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

            if (this is IrClass && isDartExtensionWithGeneratedName) {
                // A suffix is added to extension containers to prevent name conflicts with extension containers in
                // other files for the same type.
                name = name?.copy(
                    suffix = "$" + file.relativeDartPath
                        .toString()
                        .hashCode()
                        .toUInt()
                        .toString(radix = 16)
                )
            }

            return when {
                aliasPrefix != null && name != null -> DartPrefixedIdentifier(aliasPrefix, name)
                else -> name
            }
        }

    private fun IrType?.dartNameValueWith(superTypes: Boolean): String =
        when (val classifier = this?.classifierOrNull?.owner) {
            is IrClass -> when (this) {
                is IrSimpleType -> classifier.name.toString() + arguments.mapNotNull {
                    it.typeOrNull?.dartNameValueWith(superTypes)
                }.let {
                    when {
                        it.isNotEmpty() -> "With" + it.joinToString(separator = "And")
                        else -> ""
                    }
                }
                else -> classifier.name.toString()
            }
            is IrTypeParameter -> classifier.dartNameValueWith(superTypes = false)
            else -> ""
        }

    fun IrTypeParameter.dartNameValueWith(superTypes: Boolean): String {
        val namePart = name.toString().sentenceCase()
        val boundPart by lazy {
            this.superTypes.mapIndexed { index, superType ->
                val superTypePart = superType.dartNameValueWith(superTypes)

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

    fun IrContext.dartNameOf(
        declaration: IrDeclarationWithName,
        inside: IrFile,
        useKotlinAlias: Boolean = true
    ): DartIdentifier =
        dartNameOrNullOf(declaration, inside, allowNested = true, useKotlinAlias = useKotlinAlias).let {
            require(it != null) { "Name (${declaration.name.asString()}) cannot be special" }
            it
        }

    fun IrContext.dartNameOrNullOf(declaration: IrDeclarationWithName, inside: IrFile): DartIdentifier? =
        dartNameOrNullOf(declaration, inside, allowNested = true)

    fun IrContext.dartNameAsSimpleOf(declaration: IrDeclarationWithName, inside: IrFile): DartSimpleIdentifier =
        dartNameOf(declaration, inside) as DartSimpleIdentifier

    fun IrContext.dartNameAsSimpleOrNullOf(declaration: IrDeclarationWithName, inside: IrFile): DartSimpleIdentifier? =
        dartNameOrNullOf(declaration, inside) as DartSimpleIdentifier?

    /**
     * The [dartName] for this declaration. If it's a [DartPrefixedIdentifier], the prefix is removed.
     */
    fun IrContext.simpleDartNameOf(
        declaration: IrDeclarationWithName,
        inside: IrFile,
        useKotlinAlias: Boolean = true
    ): DartSimpleIdentifier =
        when (val dartName = dartNameOf(declaration, inside, useKotlinAlias)) {
            is DartSimpleIdentifier -> dartName
            is DartPrefixedIdentifier -> dartName.identifier
        }

    fun IrContext.simpleDartNameOrNullOf(declaration: IrDeclarationWithName, inside: IrFile): DartSimpleIdentifier? =
        when (val dartName = dartNameOrNullOf(declaration, inside)) {
            is DartSimpleIdentifier -> dartName
            is DartPrefixedIdentifier -> dartName.identifier
            else -> null
        }

    private fun List<*>.isLastIndexAndNotSingle(index: Int) = index == size - 1 && size != 1

    private fun dartNameOf(file: IrFile): String {
        return file.name.foldIndexed(initial = "") { index, acc, char ->
            acc + when {
                index != 0 && char.isUpperCase() && !acc.last().isUpperCase() -> "_$char"
                else -> char.toString()
            }
        }.lowercase().replace(Regex("\\.kt$"), ".g.dart")
    }

    fun IrContext.dartRelativePathOf(file: IrFile): Path {
        val fileName = dartNameOf(file)
        val filePath = Path(file.path)

        val relativePath: Path? = when {
            file.isInCurrentModule -> Path(
                (filePath.toRealPath().absolute() - sourceRoot)
                    .joinToString(File.separator)
            ).parent
            else -> filePath.parent // File paths are always serialized as relative to their source root.
        }

        // All Dart files are always put in /src in Dotlin.
        // TODO: Don't assume all Dart files are in src/ (e.g. non-Dotlin Dart packages).
        val root = Path("src")
        return relativePath?.let { root.resolve(it) }?.resolve(fileName) ?: root.resolve(fileName)
    }
}