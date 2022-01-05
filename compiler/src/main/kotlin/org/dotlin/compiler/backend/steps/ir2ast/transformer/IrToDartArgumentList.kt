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
    ) = DartArgumentList(
        irCallLike.symbol.owner.valueParameters
            .associateWith { it.accept(context) }
            .entries
            .mapNotNull map@{ (irParameter, dartParameter) ->
                val irArgument = irCallLike.getValueArgument(irParameter.index) ?: return@map null
                when {
                    dartParameter.isDefault() && dartParameter.isNamed -> DartNamedExpression(
                        label = DartLabel(dartParameter.identifier as DartSimpleIdentifier),
                        expression = irArgument.accept(context)
                    )
                    else -> irArgument.accept(context)
                }
            }
            .sortedBy { it is DartNamedExpression }
    )
}