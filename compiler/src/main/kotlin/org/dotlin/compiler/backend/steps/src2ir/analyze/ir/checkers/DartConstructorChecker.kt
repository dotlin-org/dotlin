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

package org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers

import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.steps.ir2ast.ir.typeParameterOrNull
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.DART_CONSTRUCTOR_WRONG_RETURN_TYPE
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.DART_CONSTRUCTOR_WRONG_TARGET
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.getAnnotation
import org.dotlin.compiler.backend.util.hasAnnotation
import org.dotlin.compiler.backend.util.isDotlinExternal
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.toKotlinType
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtTypeReference

object DartConstructorChecker : IrDeclarationChecker {
    override val reports = listOf(DART_CONSTRUCTOR_WRONG_TARGET, DART_CONSTRUCTOR_WRONG_RETURN_TYPE)

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration !is IrFunction) return

        if (!declaration.hasAnnotation(dotlin.DartConstructor)) return

        val annotationSource by lazy { source.getAnnotation(dotlin.DartConstructor, trace.bindingContext)!! }

        if (!declaration.isDotlinExternal || declaration.parentClassOrNull?.isCompanion != true) {
            trace.report(DART_CONSTRUCTOR_WRONG_TARGET.on(annotationSource))
        }

        val classType = declaration.parentClassOrNull?.parentClassOrNull?.defaultType ?: return
        if (!declaration.returnType.equivalentTo(classType)) {
            val reportOn = source.children.lastOrNull() as? KtTypeReference ?: annotationSource
            trace.report(DART_CONSTRUCTOR_WRONG_RETURN_TYPE.on(reportOn, classType.toKotlinType()))
        }
    }
}

private fun IrType.equivalentTo(other: IrType): Boolean {
    if (this !is IrSimpleType || other !is IrSimpleType || (arguments.isEmpty() && other.arguments.isEmpty())) {
        return this == other
    }

    if (arguments.size != other.arguments.size) return false

    val zippedArguments = arguments.zip(other.arguments)

    zippedArguments.forEach { (arg, otherArg) ->
        if (arg !is IrSimpleType || otherArg !is IrSimpleType) return false

        val typeParam = arg.typeParameterOrNull ?: return false
        val otherTypeParam = otherArg.typeParameterOrNull ?: return false

        if (typeParam.index != otherTypeParam.index) return  false
        if (typeParam.variance != otherTypeParam.variance) return false
        if (typeParam.annotations != otherTypeParam.annotations) return false
        if (typeParam.superTypes.size != otherTypeParam.superTypes.size) return false

        val zippedSuperTypes = typeParam.superTypes.zip(otherTypeParam.superTypes)

        if (zippedSuperTypes.none { (superType, otherSuperType) -> superType.equivalentTo(otherSuperType) }) {
            return false
        }
    }

    return true
}