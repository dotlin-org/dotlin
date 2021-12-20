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

import org.dotlin.compiler.backend.DotlinAnnotations
import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDartDeclarationOrigin
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrAnnotatedExpression
import org.dotlin.compiler.backend.util.hasAnnotation
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrEnumConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.isAnnotationClass
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.parentAsClass

fun IrDeclaration.isDartConst(): Boolean = when (this) {
    is IrConstructor -> when {
        // Enums always get const constructors.
        parentAsClass.isEnumClass -> true
        // Annotations always get const constructors.
        parentAsClass.isAnnotationClass -> true
        // The constructor of _$DefaultMarker is always const.
        origin == IrDartDeclarationOrigin.COMPLEX_PARAM_DEFAULT_VALUE -> true
        else -> hasAnnotation(DotlinAnnotations.dartConst)
    }
    // Enum fields are always const.
    is IrField -> origin == IrDeclarationOrigin.FIELD_FOR_ENUM_ENTRY
    else -> false
}

/**
 * **NOTE**: Always pass `context` when calling in a [IrDartTransformer].
 */
fun IrExpression.isDartConst(context: DartTransformContext? = null): Boolean = when (this) {
    // Enums are always constructed as const.
    is IrEnumConstructorCall -> true
    is IrConst<*> -> true
    is IrAnnotatedExpression -> hasAnnotation(DotlinAnnotations.dartConst)
    is IrConstructorCall -> when (symbol.owner.origin) {
        // The constructor of _$DefaultMarker should always be invoked with const.
        IrDartDeclarationOrigin.COMPLEX_PARAM_DEFAULT_VALUE -> true
        else -> context?.annotatedExpressions?.get(this)?.hasAnnotation(DotlinAnnotations.dartConst) ?: false
    }
    else -> false
}