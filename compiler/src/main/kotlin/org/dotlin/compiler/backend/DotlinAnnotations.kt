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
import org.dotlin.compiler.backend.util.getSingleAnnotationStringArgumentOf
import org.dotlin.compiler.backend.util.getSingleAnnotationTypeArgumentOf
import org.dotlin.compiler.backend.util.hasOverriddenAnnotation
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isSetter

object DotlinAnnotations {
    const val dartName = "dotlin.DartName"
    const val dartConst = "dotlin.DartConst"
    const val dartPositional = "dotlin.DartPositional"

    // Internal annotations.
    const val dartGetter = "dotlin.DartGetter"
    const val dartExtension = "dotlin.DartExtension"
    const val dartImportAlias = "dotlin.DartImportAlias"
    const val dartHideImport = "dotlin.DartHideImport"
    const val dartCatchAs = "dotlin.DartCatchAs"
}

fun IrDeclaration.hasDartGetterAnnotation() = hasOverriddenAnnotation(DotlinAnnotations.dartGetter)
fun IrDeclaration.hasDartExtensionAnnotation() = hasOverriddenAnnotation(DotlinAnnotations.dartExtension)
fun IrFunction.hasDartPositionalAnnotation() = hasOverriddenAnnotation(DotlinAnnotations.dartPositional)

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

val IrDeclaration.dartImportAliasLibrary: String?
    get() = getSingleAnnotationStringArgumentOf(DotlinAnnotations.dartImportAlias)

val IrDeclaration.dartHideImportLibrary: String?
    get() = getSingleAnnotationStringArgumentOf(DotlinAnnotations.dartHideImport)

val IrDeclaration.dartImportAliasPrefix: String?
    get() = dartImportAliasLibrary?.let { it.split(':')[1] }

val IrDeclaration.dartCatchAsType: IrType?
    get() = getSingleAnnotationTypeArgumentOf(DotlinAnnotations.dartCatchAs)

val IrValueParameter.isDartPositional: Boolean
    get() = (parent as? IrFunction)?.hasDartPositionalAnnotation() == true
