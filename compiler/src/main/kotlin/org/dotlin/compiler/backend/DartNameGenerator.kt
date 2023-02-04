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

import org.dotlin.compiler.backend.DotlinIrMangler.mangledSignatureHexString
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.util.*
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartPrefixedIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.expression.identifier.toDartIdentifier
import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.isSetter
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.util.collectionUtils.filterIsInstanceAnd
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.relativeTo

class DartNameGenerator {
    private val builtInIdentifiers = listOf(
        "abstract", "as", "covariant", "deferred",
        "dynamic", "export", "extension", "external",
        "factory", "Function", "get", "implements",
        "import", "interface", "late", "library",
        "mixin", "operator", "part", "required",
        "set", "static", "typedef"
    )
    private val asyncWords = listOf("await", "yield")
    private val reservedWords = listOf(
        "assert", "break", "case", "catch", "class",
        "const", "continue", "default", "do", "else",
        "enum", "extends", "false", "final", "finally",
        "for", "if", "in", "is", "new", "null", "rethrow",
        "return", "super", "switch", "this", "throw", "true",
        "try", "var", "void", "while", "with"
    )

    private fun IrContext.dartNameOrNullOf(
        declaration: IrDeclarationWithName,
        currentFile: IrFile,
        allowNested: Boolean,
        useKotlinAlias: Boolean = true
    ): DartIdentifier? =
        declaration.run {
            // Property parameters always use the name of their property.
            // Only if the property is private is this not the case,
            // because then the parameter is separated from its property.
            if (this is IrValueParameter) {
                correspondingProperty?.let {
                    if (!it.isPrivate) {
                        return@run dartNameOrNullOf(correspondingProperty!!, currentFile, allowNested, useKotlinAlias)
                    }
                }
            }

            val aliasPrefix = when {
                useKotlinAlias -> importAliasIn(currentFile)?.toDartIdentifier()
                else -> null
            }?.let {
                // Built-in identifiers cannot be used as import prefixes.
                when (it.value) {
                    in builtInIdentifiers -> it.asGenerated()
                    else -> it
                }
            }
            val annotatedName = annotatedDartName?.toDartIdentifier()

            var name = annotatedName ?: when {
                this is IrSimpleFunction && isOperator && name.identifier == "invoke" -> "call".toDartIdentifier()
                !name.isSpecial -> name.identifier.toDartIdentifier()
                this is IrConstructor -> when {
                    // If there are multiple constructors (and this is not the primary constructor, which by
                    // default has no name), they're numbered in the order of appearance,
                    // e.g. `MyClass.$constructor$0`.
                    !isPrimary -> {
                        val constructors = parentClassOrNull?.declarations?.filterIsInstance<IrConstructor>()
                            ?: emptyList()

                        DartSimpleIdentifier("\$constructor$${constructors.indexOf(this)}")
                    }

                    else -> when {
                        // If the primary constructor is private with no name, we set the name to "_".
                        isPrivate -> DartSimpleIdentifier("_")
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
            if (name != null && annotatedName == null && this is IrSimpleFunction && isOverload) {
                name = name.copy(
                    suffix = "$" + mangledSignatureHexString()
                )
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
            if (hasObjectParentAndStaticCounterpart) {
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

            // If there's a property or field with the same name as a method or function, rename the field/property.
            // Must happen second to last.
            if (name != null && (this is IrProperty || this is IrField)) {
                val clashingMethods =
                    (parent as? IrDeclarationContainer)?.declarations
                        ?.filterIsInstanceAnd<IrFunction> { !it.isPropertyAccessor && it.dartNameOrNull == name }
                        .orEmpty()

                if (clashingMethods.isNotEmpty()) {
                    name = name.copy(suffix = "\$property")
                }
            }

            // Built-in identifier and reserved words check. This must _always_ happen last.
            if ((this is IrClass && name?.value in builtInIdentifiers) || name?.value in reservedWords) {
                name = name?.asGenerated()
            }

            // TODO: Async words check

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
        }.lowercase().replace(Regex("\\.kt$"), ".$FILE_EXTENSION")
    }

    fun IrContext.dartPathOf(file: IrFile): Path {
        val fileName = dartNameOf(file)
        val filePath = Path(file.path)

        val relativeParentPath = when {
            file.isInCurrentModule -> filePath.relativeTo(dartProject.path).parent
            else -> filePath.parent // File paths are always serialized as relative to their project root.
        } ?: Path("")

        return relativeParentPath.resolve(fileName)
    }

    fun IrContext.relativeDartPathOf(file: IrFile): Path {
        val theirDartPath = dartPathOf(file)
        val currentDartPath = dartPathOf(currentFile)

        return currentDartPath.parent?.relativize(theirDartPath) ?: theirDartPath
    }

    companion object {
        /**
         * Dotlin-generated Dart file extension, without dot.
         */
        const val FILE_EXTENSION = "dt.g.dart"
    }
}