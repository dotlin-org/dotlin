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
import org.dotlin.compiler.backend.util.hasAnnotation
import org.dotlin.compiler.backend.util.hasOverriddenAnnotation
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isSetter
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.FqName

object DotlinAnnotations {
    val const = FqName("dotlin.const")
    val dartName = FqName("dotlin.DartName")
    val dartPositional = FqName("dotlin.DartPositional")
    val dartLibrary = FqName("dotlin.DartLibrary")
    val dartStatic = FqName("dotlin.DartStatic")
    val dartExtensionName = FqName("dotlin.DartExtensionName")

    // Internal annotations.
    val dartGetter = FqName("dotlin.DartGetter")
    val dartExtension = FqName("dotlin.DartExtension")
    val dartHideNameFromCore = FqName("dotlin.DartHideNameFromCore")
}

fun IrDeclaration.hasDartConstAnnotation() = hasAnnotation(DotlinAnnotations.const)
fun IrDeclaration.hasDartGetterAnnotation() = hasOverriddenAnnotation(DotlinAnnotations.dartGetter)
fun IrDeclaration.hasDartExtensionAnnotation() = hasOverriddenAnnotation(DotlinAnnotations.dartExtension)
fun IrFunction.hasDartPositionalAnnotation() = hasOverriddenAnnotation(DotlinAnnotations.dartPositional)
fun IrDeclaration.hasDartHideNameFromCoreAnnotation() = hasAnnotation(DotlinAnnotations.dartHideNameFromCore)
fun IrDeclaration.hasDartExtensionNameAnnotation() = hasAnnotation(DotlinAnnotations.dartExtensionName)

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

val IrValueParameter.isDartPositional: Boolean
    get() = (parent as? IrFunction)?.hasDartPositionalAnnotation() == true

val IrDeclaration.isDartStatic: Boolean
    get() = hasDartStaticAnnotation() ||
            parentClassOrNull?.hasDartStaticAnnotation() == true ||
            (this is IrSimpleFunction && correspondingProperty?.hasDartStaticAnnotation() == true)

val IrDeclaration.dartExtensionName: String?
    get() = getSingleAnnotationStringArgumentOf(DotlinAnnotations.dartExtensionName)
        ?: fileOrNull?.getSingleAnnotationStringArgumentOf(DotlinAnnotations.dartExtensionName)