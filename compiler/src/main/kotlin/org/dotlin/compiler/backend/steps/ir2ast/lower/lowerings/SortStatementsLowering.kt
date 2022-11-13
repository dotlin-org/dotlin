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

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class SortStatementsLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrFunction || declaration.body !is IrBlockBody) return noChange()

        val irFunction = declaration

        // We sort the function statements: First the parameter initializers, then field initializers,
        // and based on their ordering in the constructor and class, respectively.
        (irFunction.body as IrBlockBody).apply {
            if (irFunction is IrConstructor) {
                statements.sortWith { a, b ->
                    val aProperty = a.propertyItAssignsTo
                    val bProperty = b.propertyItAssignsTo

                    when {
                        aProperty != null && bProperty != null -> irFunction.parentClassProperties.toList().let {
                            it.indexOf(aProperty).compareTo(it.indexOf(bProperty))
                        }
                        aProperty != null && bProperty == null -> 1
                        aProperty == null && bProperty != null -> -1
                        else -> 0
                    }
                }
            }

            statements.sortWith { a, b ->
                val aParam = a.parameterItAssignsTo
                val bParam = b.parameterItAssignsTo

                when {
                    aParam != null && bParam != null -> irFunction.valueParameters.let {
                        it.indexWithSymbol(irFunction, of = aParam)
                            .compareTo(it.indexWithSymbol(irFunction, of = bParam))
                    }
                    aParam != null && bParam == null -> -1
                    aParam == null && bParam != null -> 1
                    else -> 0
                }
            }
        }

        return noChange()
    }

    private fun List<IrValueParameter>.indexWithSymbol(irFunction: IrFunction, of: IrValueParameter): Int {
        val overridden = irFunction.valueParameters.firstOrNull {
            it.resolveOverride()?.symbol == of.symbol
        }

        return indexOfFirst {
            if (it.symbol == of.symbol) return@indexOfFirst true

            return@indexOfFirst overridden != null && it.symbol == overridden.symbol
        }
    }
}
