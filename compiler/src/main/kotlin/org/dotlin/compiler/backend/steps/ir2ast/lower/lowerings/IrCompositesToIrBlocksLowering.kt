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

import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrComposite
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl

/**
 * [IrComposite]s are converted to [IrBlock].
 */
class IrCompositesToIrBlocksLowering(override val context: DartLoweringContext) : IrExpressionLowering {
    override fun <D> DartLoweringContext.transform(
        expression: IrExpression,
        container: D
    ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
        if (expression !is IrComposite || !expression.isTransparentScope) return noChange()

        return replaceWith(
            expression.let {
                IrBlockImpl(
                    it.startOffset, it.endOffset,
                    it.type,
                    it.origin
                ).apply {
                    statements.addAll(it.statements)
                }
            }
        )
    }
}

