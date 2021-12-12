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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.accept
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.dartName
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList
import org.dotlin.compiler.dart.ast.statement.*
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
object IrToDartStatementTransformer : IrDartAstTransformer<DartStatement> {
    override fun visitReturn(expression: IrReturn, context: DartTransformContext) =
        DartReturnStatement(expression.value.accept(context))

    override fun visitWhen(irWhen: IrWhen, context: DartTransformContext) = irWhen.branches.reversed().toList().run {
        drop(1).fold(
            initial = first().let {
                val thenStatement = it.result.acceptAsStatement(context).wrapInBlock()

                when (it) {
                    is IrElseBranch -> thenStatement
                    else -> DartIfStatement(
                        condition = it.condition.accept(context),
                        thenStatement = thenStatement
                    )
                }
            },
            operation = { statement, irBranch ->
                DartIfStatement(
                    condition = irBranch.condition.accept(context),
                    thenStatement = irBranch.result.acceptAsStatement(context).wrapInBlock(),
                    elseStatement = statement
                )
            }
        )
    }

    override fun visitVariable(irVariable: IrVariable, context: DartTransformContext) = irVariable.let {
        DartVariableDeclarationStatement(
            variables = DartVariableDeclarationList(
                DartVariableDeclaration(
                    name = it.dartName,
                    expression = it.initializer?.accept(context)
                ),
                type = irVariable.type.accept(context),
                isConst = it.isConst,
                isFinal = !it.isVar,
                isLate = false
            )
        )
    }


    override fun visitBlock(irBlock: IrBlock, context: DartTransformContext) = irBlock.run {
        // If there's only a single statement in the block, we remove the block.
        statements.singleOrNull()?.accept(context) ?: DartBlock(
            statements = irBlock.statements.accept(context)
        )
    }

    override fun visitExpression(expression: IrExpression, context: DartTransformContext) =
        expression.accept(context).asStatement()
}

fun IrStatement.accept(context: DartTransformContext) = accept(IrToDartStatementTransformer, context)
fun Iterable<IrStatement>.accept(context: DartTransformContext) = map { it.accept(context) }
fun IrExpression.acceptAsStatement(context: DartTransformContext) = accept(IrToDartStatementTransformer, context)





