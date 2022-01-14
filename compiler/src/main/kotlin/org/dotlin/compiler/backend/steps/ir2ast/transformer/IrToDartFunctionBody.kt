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
import org.dotlin.compiler.dart.ast.declaration.function.body.DartBlockFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartEmptyFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartExpressionFunctionBody
import org.dotlin.compiler.dart.ast.declaration.function.body.DartFunctionBody
import org.dotlin.compiler.dart.ast.statement.DartBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrSyntheticBody

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class IrToDartFunctionBodyTransformer(private val allowEmpty: Boolean) : IrDartAstTransformer<DartFunctionBody>() {
    // TODO: isAsync, isGenerator

    override fun DartTransformContext.visitBlockBody(
        irBody: IrBlockBody,
        context: DartTransformContext
    ): DartFunctionBody {
        if (allowEmpty && irBody.statements.isEmpty()) return DartEmptyFunctionBody()

        return DartBlockFunctionBody(
            block = DartBlock(
                statements = irBody.statements.accept(context)
            )
        )
    }

    override fun DartTransformContext.visitExpressionBody(irBody: IrExpressionBody, context: DartTransformContext) =
        DartExpressionFunctionBody(
            expression = irBody.expression.accept(context)
        )

    override fun DartTransformContext.visitSyntheticBody(body: IrSyntheticBody, data: DartTransformContext) =
        DartEmptyFunctionBody()
}

fun IrBody?.accept(context: DartTransformContext, allowEmpty: Boolean = false) = when (this) {
    // TODO: isAsync, isGenerator
    null -> DartEmptyFunctionBody()
    else -> accept(IrToDartFunctionBodyTransformer(allowEmpty), context)
}



