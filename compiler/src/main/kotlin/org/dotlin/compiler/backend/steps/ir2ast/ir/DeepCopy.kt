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

package org.dotlin.compiler.backend.steps.ir2ast.ir

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.copyAttributes
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.DeepCopyIrTreeWithSymbols
import org.jetbrains.kotlin.ir.util.SymbolRemapper
import org.jetbrains.kotlin.ir.util.TypeRemapper
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols

class DeepCopier(
    symbolRemapper: SymbolRemapper,
    typeRemapper: TypeRemapper
) : DeepCopyIrTreeWithSymbols(symbolRemapper, typeRemapper) {
    override fun visitExpression(expression: IrExpression): IrExpression {
        return when (expression) {
            is IrNullAwareExpression -> visitNullAwareExpression(expression)
            is IrConjunctionExpression -> visitConjunctionExpression(expression)
            is IrDisjunctionExpression -> visitDisjunctionExpression(expression)
            else -> super.visitExpression(expression)
        }

    }

    private fun visitNullAwareExpression(irNullAware: IrNullAwareExpression): IrNullAwareExpression =
        IrNullAwareExpression(irNullAware.expression.transform())
            .copyAttributes(irNullAware)

    private fun visitConjunctionExpression(irConjunction: IrConjunctionExpression): IrConjunctionExpression =
        IrConjunctionExpression(
            left = irConjunction.left.transform(),
            right = irConjunction.right.transform(),
            type = irConjunction.type.remapType()
        ).copyAttributes(irConjunction)

    private fun visitDisjunctionExpression(irDisjunction: IrDisjunctionExpression): IrDisjunctionExpression =
        IrDisjunctionExpression(
            left = irDisjunction.left.transform(),
            right = irDisjunction.right.transform(),
            type = irDisjunction.type.remapType()
        ).copyAttributes(irDisjunction)
}


/**
 * Always use this instead of [deepCopyWithSymbols].
 */
inline fun <reified T : IrElement> T.deepCopy(
    initialParent: IrDeclarationParent? = null
): T = deepCopyWithSymbols(initialParent, ::DeepCopier)