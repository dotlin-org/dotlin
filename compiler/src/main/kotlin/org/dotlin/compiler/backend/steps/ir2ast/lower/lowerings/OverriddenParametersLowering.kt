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

import org.dotlin.compiler.backend.steps.ir2ast.ir.asAssignable
import org.dotlin.compiler.backend.steps.ir2ast.ir.hasDirectReferenceTo
import org.dotlin.compiler.backend.steps.ir2ast.ir.isInitializerForComplexParameter
import org.dotlin.compiler.backend.steps.ir2ast.ir.resolveRootOverride
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.statements

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class OverriddenParametersLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrValueParameter) return noChange()

        val parameter = declaration

        val irFunction = parameter.parent as? IrSimpleFunction ?: return noChange()
        val overriddenParameter = parameter.resolveRootOverride() ?: return noChange()
        val overriddenFunction = overriddenParameter.parent as? IrSimpleFunction ?: return noChange()

        var newIrValueParameter = parameter.apply {
            overriddenParameter.type.let {
                if (!it.isTypeParameter()) {
                    type = it
                }
            }

            defaultValue = overriddenParameter.defaultValue
        }

        // Complex parameters will be initialized in the first statements, we copy them over.
        overriddenFunction.body?.statements
            ?.filterIsInstance<IrExpression>()
            ?.singleOrNull {
                it.isInitializerForComplexParameter && it.hasDirectReferenceTo(overriddenParameter)
            }?.let { initializerStatement ->
                newIrValueParameter = newIrValueParameter.asAssignable()

                irFunction.body?.apply {
                    when (this) {
                        is IrBlockBody -> statements.add(0, initializerStatement)
                        is IrExpressionBody -> expression = initializerStatement
                    }

                    // TODO: Copy
                    //remap(overriddenParameter to newIrValueParameter)
                }
            }


        return just { replaceWith(newIrValueParameter) }
    }
}