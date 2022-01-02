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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.dartName
import org.dotlin.compiler.dart.ast.DartLabel
import org.dotlin.compiler.dart.ast.expression.DartArgumentList
import org.dotlin.compiler.dart.ast.expression.DartExpression
import org.dotlin.compiler.dart.ast.expression.DartNamedExpression
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.parameter.identifier
import org.dotlin.compiler.dart.ast.parameter.isDefault
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartArgumentListTransformer : IrDartAstTransformer<DartArgumentList> {
    override fun visitFunctionAccess(
        irCallLike: IrFunctionAccessExpression,
        context: DartTransformContext
    ): DartArgumentList {
        val irParameters = irCallLike.symbol.owner.valueParameters

        val dartParameters = irParameters.accept(context)

        val arguments = mutableListOf<DartExpression>()
        for (i in 0 until irCallLike.valueArgumentsCount) {
            val argument = irCallLike.getValueArgument(i) ?: continue

            // Find the param with the given name or parameter at our position.
            val irParameter = irParameters[i]
            val dartParameter = dartParameters.first {
                it.identifier == irParameter.dartName
            }

            val dartArgument = when {
                dartParameter.isDefault() && dartParameter.isNamed -> DartNamedExpression(
                    label = DartLabel(dartParameter.identifier as DartSimpleIdentifier),
                    expression = argument.accept(context)
                )
                else -> argument.accept(context)
            }

            arguments.add(dartArgument)
        }

        arguments.sortBy { it is DartNamedExpression }

        return DartArgumentList(arguments)
    }
}