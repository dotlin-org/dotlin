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

package org.dotlin.compiler.backend.steps.ir2ast.transformer.util

import org.dotlin.compiler.backend.DotlinAnnotations
import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementVisitor
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDartDeclarationOrigin
import org.dotlin.compiler.backend.steps.ir2ast.ir.correspondingProperty
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrAnnotatedExpression
import org.dotlin.compiler.backend.steps.ir2ast.ir.element.IrIfNullExpression
import org.dotlin.compiler.backend.steps.ir2ast.ir.hasExplicitBackingField
import org.dotlin.compiler.backend.util.hasAnnotation
import org.dotlin.compiler.backend.util.isSimple
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.isAnnotationClass
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.parentAsClass

fun IrDeclaration.isDartConst(): Boolean = when (this) {
    is IrConstructor -> hasAnnotation(DotlinAnnotations.dartConst) || parentAsClass.isDartConst()
    // Enum fields are always const.
    is IrField -> when {
        origin == IrDeclarationOrigin.FIELD_FOR_ENUM_ENTRY -> true
        correspondingProperty?.isConst == true -> true
        // Instance fields for const objects are always const.
        origin == IrDeclarationOrigin.FIELD_FOR_OBJECT_INSTANCE -> when (val init = initializer!!.expression) {
            is IrGetObjectValue -> init.symbol.owner.isDartConst()
            is IrConstructorCall -> init.symbol.owner.isDartConst()
            else -> throw UnsupportedOperationException("Invalid FIELD_FOR_OBJECT_INSTANCE")
        }
        else -> false
    }
    is IrProperty -> isConst
    // Only add cases here if a certain class should _always_ be const constructed.
    is IrClass -> when {
        isObject -> declarations.run {
            filterIsInstance<IrProperty>()
                .plus(filterIsInstance<IrField>().mapNotNull { it.correspondingProperty })
                .toSet()
                .all {
                    it.isConst || it.origin == IrDartDeclarationOrigin.WAS_CONST_OBJECT_MEMBER ||
                            (!it.isSimple && !it.hasExplicitBackingField)
                }
        }
        // Annotations, enums and _$DefaultValue classes are always const.
        else -> isEnumClass || isAnnotationClass || origin == IrDartDeclarationOrigin.COMPLEX_PARAM_DEFAULT_VALUE
    }
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
    is IrConstructorCall -> when {
        symbol.owner.parentAsClass.isDartConst() -> true
        else -> context?.annotatedExpressions?.get(this)?.hasAnnotation(DotlinAnnotations.dartConst) ?: false
    }
    is IrWhen, is IrIfNullExpression -> {
        var isConst = true

        acceptChildren(
            object : IrCustomElementVisitor<Unit, Nothing?> {
                override fun visitElement(element: IrElement, data: Nothing?) {
                    if (element is IrExpression) {
                        isConst = isConst && element.isDartConst(context)
                    }

                    element.acceptChildren(this, null)
                }

            },
            data = null
        )

        isConst
    }
    else -> false
}