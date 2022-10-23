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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.steps.ir2ast.ir.constructorWithNameOrNull
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.hasAnnotation
import org.dotlin.compiler.backend.util.isDartConst
import org.dotlin.compiler.backend.util.isDotlinExternal
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.backend.common.ir.copyValueParametersFrom
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.copyAttributes
import org.jetbrains.kotlin.ir.declarations.impl.IrConstructorImpl
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.util.copyTypeAndValueArgumentsFrom
import org.jetbrains.kotlin.ir.util.parentClassOrNull

/**
 * Make external companion object methods marked `@DartConstructor` real constructors.
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object DartConstructorsLowering {
    class Declarations(override val context: DartLoweringContext) : IrDeclarationLowering {
        override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
            if (declaration !is IrSimpleFunction || !declaration.hasAnnotation(dotlin.DartConstructor)) return noChange()

            val parentCompanion = declaration.parentClassOrNull ?: return noChange()
            if (!parentCompanion.isCompanion) return noChange()

            val parentClass = parentCompanion.parentClassOrNull ?: return noChange()

            parentCompanion.declarations.remove(declaration)

            parentClass.addChild(
                IrConstructorImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    origin = IrDeclarationOrigin.DEFINED,
                    symbol = IrConstructorSymbolImpl(),
                    declaration.name,
                    declaration.visibility,
                    declaration.returnType,
                    isInline = false,
                    isExpect = false,
                    isExternal = declaration.isDotlinExternal,
                    isPrimary = false,
                ).apply {
                    copyValueParametersFrom(declaration, substitutionMap = emptyMap())

                    if (declaration.isDartConst()) {
                        addConstAnnotation()
                    }
                }
            )

            return noChange()
        }
    }

    class Calls(override val context: DartLoweringContext) : IrExpressionLowering {
        override fun DartLoweringContext.transform(expression: IrExpression): Transformation<IrExpression>? {
            if (expression !is IrCall) return noChange()

            val dartConstructor = expression.symbol.owner
            if (!dartConstructor.hasAnnotation(dotlin.DartConstructor)) return noChange()

            val realConstructor = dartConstructor.parentClassOrNull?.parentClassOrNull
                ?.constructorWithNameOrNull(dartConstructor.name.toString()) ?: return noChange()

            return replaceWith(
                IrConstructorCallImpl(
                    expression.startOffset, expression.endOffset,
                    expression.type,
                    symbol = realConstructor.symbol,
                    expression.typeArgumentsCount,
                    constructorTypeArgumentsCount = 0,
                    expression.valueArgumentsCount,
                ).apply {
                    copyAttributes(expression)
                    copyTypeAndValueArgumentsFrom(expression)
                }
            )
        }
    }
}