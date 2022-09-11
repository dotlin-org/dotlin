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

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.codegen.kotlinType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtConstructorCalleeExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.types.KotlinType

const val interfaceOrMixinMarkerName = "InterfaceOrMixin"
const val interfaceMarkerName = "Interface"
const val mixinMarkerName = "Mixin"

fun FqName?.isDotlinInterfaceOrMixin() = this?.asString() == "dotlin.$interfaceOrMixinMarkerName"
fun FqName?.isDotlinInterface() = this?.asString() == "dotlin.$interfaceMarkerName"
fun FqName?.isDotlinMixin() = this?.asString() == "dotlin.$mixinMarkerName"

/**
 * Whether type is either `dotlin.Interface`, `Mixin` or `InterfaceOrMixin`.
 */
fun IrType.isSpecialInheritanceMarker() = isInterfaceOrMixinMarker() || isImplicitInterfaceMarker() || isMixinMarker()

/**
 * Whether type is either `dotlin.Interface`, `Mixin` or `InterfaceOrMixin`.
 */
fun KotlinType.isSpecialInheritanceMarker() =
    isInterfaceOrMixinMarker() || isImplicitInterfaceMarker() || isMixinMarker()

/**
 * Whether type is either a implicit interface or mixin marker.
 */
fun IrType.isDerivedSpecialInheritanceMarker() = isImplicitInterfaceMarker() || isMixinMarker()

/**
 * Whether type is either a implicit interface or mixin marker.
 */
fun KotlinType.isDerivedSpecialInheritanceMarker() = isImplicitInterfaceMarker() || isMixinMarker()

/**
 * Whether the type is `dotlin.InterfaceOrMixin`. Not to be confused with [isDerivedSpecialInheritanceMarker].
 */
fun IrType.isInterfaceOrMixinMarker() = classFqName?.isDotlinInterfaceOrMixin() == true

/**
 * Whether the type is `dotlin.InterfaceOrMixin`. Not to be confused with [isDerivedSpecialInheritanceMarker].
 */
fun KotlinType.isInterfaceOrMixinMarker() =
    constructor.declarationDescriptor?.fqNameOrNull()?.isDotlinInterfaceOrMixin() == true

fun IrType.isImplicitInterfaceMarker() = classFqName?.isDotlinInterface() == true
fun KotlinType.isImplicitInterfaceMarker() =
    constructor.declarationDescriptor?.fqNameOrNull()?.isDotlinInterface() == true

fun IrType.isMixinMarker() = classFqName?.isDotlinMixin() == true
fun KotlinType.isMixinMarker() = constructor.declarationDescriptor?.fqNameOrNull()?.isDotlinMixin() == true

fun PsiElement.isImplicitInterfaceConstructorCall(bindingContext: BindingContext) =
    isSpecialInheritanceConstructorCall(bindingContext, mustBe = SpecialInheritanceKind.IMPLICIT_INTERFACE)

fun PsiElement.isMixinConstructorCall(bindingContext: BindingContext) =
    isSpecialInheritanceConstructorCall(bindingContext, mustBe = SpecialInheritanceKind.MIXIN)

/**
 * Works on the [KtTypeReference] that's part of a [KtConstructorCalleeExpression].
 */
fun PsiElement.isSpecialInheritanceConstructorCall(
    bindingContext: BindingContext,
    mustBe: SpecialInheritanceKind? = null,
): Boolean {
    val typeReference = this as? KtTypeReference ?: return false

    val constructorCallee = typeReference.parent as? KtConstructorCalleeExpression ?: return false

    val valueArguments = constructorCallee.parent?.children?.last()?.children ?: return false
    val valueArgument = valueArguments.singleOrNull() as? KtValueArgument ?: return false

    val nameReference = valueArgument.children.first() as? KtNameReferenceExpression ?: return false

    return when (val type = nameReference.kotlinType(bindingContext)) {
        // If the type information is not available, just assume it's correct if the name matches.
        null -> {
            val text = nameReference.text

            when (mustBe) {
                SpecialInheritanceKind.IMPLICIT_INTERFACE -> text == interfaceMarkerName
                SpecialInheritanceKind.MIXIN -> text == mixinMarkerName
                else -> when (nameReference.text) {
                    interfaceMarkerName, mixinMarkerName -> true
                    else -> false
                }
            }
        }
        else -> when (mustBe) {
            SpecialInheritanceKind.IMPLICIT_INTERFACE -> type.isImplicitInterfaceMarker()
            SpecialInheritanceKind.MIXIN -> type.isMixinMarker()
            else -> type.isDerivedSpecialInheritanceMarker()
        }
    }
}

enum class SpecialInheritanceKind {
    IMPLICIT_INTERFACE,
    MIXIN
}