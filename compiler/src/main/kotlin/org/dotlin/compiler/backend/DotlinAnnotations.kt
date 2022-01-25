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

package org.dotlin.compiler.backend

import org.dotlin.compiler.backend.steps.ir2ast.ir.correspondingProperty
import org.dotlin.compiler.backend.util.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*

object DotlinAnnotations {
    const val dartName = "dotlin.DartName"
    const val dartConst = "dotlin.DartConst"
    const val dartPositional = "dotlin.DartPositional"
    const val dartLibrary = "dotlin.DartLibrary"
    const val dartImplementationOf = "dotlin.DartImplementationOf"
    const val dartStatic = "dotlin.DartStatic"

    // Internal annotations.
    const val dartGetter = "dotlin.DartGetter"
    const val dartExtension = "dotlin.DartExtension"
    const val dartHideNameFromCore = "dotlin.DartHideNameFromCore"
    const val dartCatchAs = "dotlin.DartCatchAs"
}

fun IrDeclaration.hasDartGetterAnnotation() = hasOverriddenAnnotation(DotlinAnnotations.dartGetter)
fun IrDeclaration.hasDartExtensionAnnotation() = hasOverriddenAnnotation(DotlinAnnotations.dartExtension)
fun IrFunction.hasDartPositionalAnnotation() = hasOverriddenAnnotation(DotlinAnnotations.dartPositional)
fun IrDeclaration.hasDartHideNameFromCoreAnnotation() = hasAnnotation(DotlinAnnotations.dartHideNameFromCore)
fun IrDeclaration.hasDartImplementationOfAnnotation() = hasAnnotation(DotlinAnnotations.dartImplementationOf)
private fun IrDeclaration.hasDartStaticAnnotation() = hasAnnotation(DotlinAnnotations.dartStatic)

val IrDeclaration.dartAnnotatedName: String?
    get() = when (this) {
        is IrField -> correspondingProperty ?: this
        is IrSimpleFunction -> when {
            isGetter -> correspondingProperty!!
            isSetter -> correspondingProperty!!
            else -> this
        }
        else -> this
    }.run { getSingleAnnotationStringArgumentOf(DotlinAnnotations.dartName) }

data class DartUnresolvedImport(val library: String, val alias: String?, val hidden: Boolean)

private val builtInImports = mapOf(
    "dart.core" to "dart:core",
    "dart.typeddata" to "dart:typed_data",
    "dart.math" to "dart:math"
)

private fun IrAnnotationContainer.dartLibraryImportOf(declaration: IrDeclarationWithName): DartUnresolvedImport? {
    return getTwoAnnotationArgumentsOf<String, Boolean>(DotlinAnnotations.dartLibrary)
        ?.let { (library, aliased) ->
            DartUnresolvedImport(
                library,
                alias = when {
                    aliased -> library.split(':')[1] // TODO: Improve for non Dart SDK imports.
                    else -> null
                },
                hidden = aliased
            )
        } ?: when (this) {
        // Try to see if the file has a @DartLibrary annotation.
        !is IrFile -> declaration.fileOrNull?.dartLibraryImportOf(declaration)
            ?: declaration.getPackageFragment()?.fqName?.let { fqName ->
                builtInImports[fqName.asString()]?.let {
                    DartUnresolvedImport(
                        library = it,
                        alias = null,
                        hidden = false,
                    )
                }
            }
        else -> null
    }
}

val IrDeclaration.dartUnresolvedImport: DartUnresolvedImport?
    get() = (this as? IrDeclarationWithName)?.dartLibraryImportOf(this)

val IrDeclaration.dartLibrary: String?
    get() = dartUnresolvedImport?.library

val IrDeclaration.dartLibraryAlias: String?
    get() = dartUnresolvedImport?.alias

val IrDeclaration.dartCatchAsType: IrType?
    get() = getSingleAnnotationTypeArgumentOf(DotlinAnnotations.dartCatchAs)

val IrValueParameter.isDartPositional: Boolean
    get() = (parent as? IrFunction)?.hasDartPositionalAnnotation() == true

val IrDeclaration.dartImplementationFqName: String?
    get() = getSingleAnnotationStringArgumentOf(DotlinAnnotations.dartImplementationOf)

val IrDeclaration.isDartStatic: Boolean
    get() = hasDartStaticAnnotation() ||
            parentClassOrNull?.hasDartStaticAnnotation() == true ||
            (this is IrSimpleFunction && correspondingProperty?.hasDartStaticAnnotation() == true)