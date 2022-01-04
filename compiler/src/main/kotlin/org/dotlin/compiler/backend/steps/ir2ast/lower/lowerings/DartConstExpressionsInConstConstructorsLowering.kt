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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementVisitorVoid
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartConst
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

/**
 * Adds a `@DartConst` annotation to all expressions in const constructors.
 */
class DartConstExpressionsInConstConstructorsLowering(override val context: DartLoweringContext) :
    IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrValueParameter) return noChange()

        val parent = declaration.parent

        if (parent !is IrConstructor || !parent.isDartConst()) return noChange()

        val defaultValue = declaration.defaultValue ?: return noChange()
        val dartConst = dartBuiltIns.dotlin.dartConst.owner.primaryConstructor!!.symbol

        defaultValue.acceptChildrenVoid(
            object : IrCustomElementVisitorVoid {
                override fun visitConstructorCall(expression: IrConstructorCall) {
                    expression.acceptChildrenVoid(this)

                    expression.annotate {
                        buildStatement(parent.symbol) {
                            irCall(dartConst)
                        }
                    }
                }

                override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
            }
        )

        return noChange()
    }
}