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

package org.dotlin.compiler.backend.util

import org.dotlin.compiler.backend.hasDartConstAnnotation
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.jetbrains.kotlin.backend.jvm.ir.psiElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtModifierListOwner
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun IrDeclaration.isDartConst(): Boolean = when (this) {
    is IrField -> when {
        // Enum fields are always const.
        origin == IrDeclarationOrigin.FIELD_FOR_ENUM_ENTRY -> true
        correspondingProperty?.isConst == true -> true
        // Instance fields for const objects are always const.
        origin == IrDeclarationOrigin.FIELD_FOR_OBJECT_INSTANCE -> when (val init = initializer?.expression) {
            is IrGetObjectValue -> init.symbol.owner.isDartConst()
            is IrConstructorCall -> init.symbol.owner.isDartConst()
            else -> throw UnsupportedOperationException("Invalid FIELD_FOR_OBJECT_INSTANCE")
        }
        isBackingField -> correspondingProperty?.isDartConst() == true
        else -> false
    }
    is IrFunction -> hasConstModifierOrAnnotation() || when (this) {
        is IrConstructor -> parentAsClass.isDartConst()
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
    is IrVariable -> isConst || hasConstModifierOrAnnotation()
    else -> false
}

fun IrDeclaration.hasConstModifierOrAnnotation(): Boolean {
    // Functions/constructors can be annotated with `@const` by the `DartConstDeclarationsLowering`. This happens
    // for libraries' outputted IR, because source information is not available for dependencies.
    if (hasDartConstAnnotation()) return true

    val source = psiElement as? KtModifierListOwner ?: return false
    return source.modifierList?.hasModifier(KtTokens.CONST_KEYWORD) == true
}

@OptIn(ExperimentalContracts::class)
fun IrDeclarationParent.isDartConstInlineFunction(): Boolean {
    contract {
        returns(true) implies (this@isDartConstInlineFunction is IrSimpleFunction)
    }

    return this is IrDeclaration && (this as IrDeclaration).isDartConstInlineFunction()
}

@OptIn(ExperimentalContracts::class)
fun IrDeclaration.isDartConstInlineFunction(): Boolean {
    contract {
        returns(true) implies (this@isDartConstInlineFunction is IrSimpleFunction)
    }

    return this is IrSimpleFunction && isInline &&
            !isPropertyAccessor && isDartConst()
}