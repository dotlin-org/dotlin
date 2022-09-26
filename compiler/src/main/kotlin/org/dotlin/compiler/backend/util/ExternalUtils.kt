package org.dotlin.compiler.backend.util

import org.jetbrains.kotlin.backend.jvm.ir.psiElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.util.isEffectivelyExternal
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtDeclaration

/**
 * True if this declaration has the `external` keyword in front of it in the source.
 */
val IrDeclaration.isExplicitlyExternal: Boolean
    get() = (psiElement as? KtDeclaration)?.hasModifier(KtTokens.EXTERNAL_KEYWORD) == true

/**
 * Whether this declaration is external by Dotlin standards. This means that companion objects are only
 * considered external if they explicitely had the `external` keyword in front of them.
 */
val IrDeclaration.isActuallyExternal: Boolean
    get() = when {
        this is IrClass && isCompanion -> isExplicitlyExternal
        parentClassOrNull is IrClass && parentClassOrNull?.isCompanion == true -> isExplicitlyExternal
        else -> isEffectivelyExternal()
    }

