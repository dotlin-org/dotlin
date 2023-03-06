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
import org.dotlin.compiler.backend.steps.ir2ast.ir.isStatic
import org.dotlin.compiler.backend.util.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isSetter
import org.jetbrains.kotlin.ir.util.parentClassOrNull

fun IrDeclaration.hasDartConstAnnotation() = hasAnnotation(dotlin.const)
fun IrDeclaration.hasDartGetterAnnotation() = hasOverriddenAnnotation(dotlin.DartGetter)
fun IrDeclaration.hasDartExtensionAnnotation() = hasOverriddenAnnotation(dotlin.DartExtension)
fun IrFunction.hasDartPositionalAnnotation() = hasOverriddenAnnotation(dotlin.DartPositional)
fun IrDeclaration.hasDartExtensionNameAnnotation() = hasAnnotation(dotlin.DartExtensionName)

private fun IrDeclaration.hasDartStaticAnnotation() = hasAnnotation(dotlin.DartStatic)

val IrDeclaration.annotatedDartLibrary: String?
    get() = getSingleAnnotationStringArgumentOf(dotlin.DartLibrary)
        ?: fileOrNull?.getSingleAnnotationStringArgumentOf(dotlin.DartLibrary)

val IrDeclaration.annotatedDartName: String?
    get() = when (this) {
        is IrField -> correspondingProperty ?: this
        is IrSimpleFunction -> when {
            isGetter -> correspondingProperty!!
            isSetter -> correspondingProperty!!
            else -> this
        }

        else -> this
    }.run { getSingleOverriddenAnnotationStringArgumentOf(dotlin.DartName) }

val IrValueParameter.isDartPositional: Boolean
    get() = (parent as? IrFunction)?.hasDartPositionalAnnotation() == true

val IrDeclaration.dartExtensionName: String?
    get() = getSingleAnnotationStringArgumentOf(dotlin.DartExtensionName)
        ?: fileOrNull?.getSingleAnnotationStringArgumentOf(dotlin.DartExtensionName)