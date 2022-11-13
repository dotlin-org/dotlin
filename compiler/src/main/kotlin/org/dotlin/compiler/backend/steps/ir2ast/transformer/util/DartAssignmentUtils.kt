package org.dotlin.compiler.backend.steps.ir2ast.transformer.util

import org.dotlin.compiler.backend.steps.ir2ast.DartAstTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.getValueArgumentOrDefault
import org.dotlin.compiler.backend.steps.ir2ast.transformer.accept
import org.dotlin.compiler.dart.ast.expression.DartAssignmentExpression
import org.dotlin.compiler.dart.ast.expression.DartAssignmentOperator.*
import org.dotlin.compiler.dart.ast.expression.DartExpression
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.utils.addToStdlib.cast

fun DartAstTransformContext.createDartAssignment(
    origin: IrStatementOrigin?,
    receiver: DartExpression,
    irReceiverType: IrType,
    irValue: IrExpression,
): DartAssignmentExpression {
    val irOperationAssignValue by lazy {
        irValue.cast<IrFunctionAccessExpression>().getValueArgumentOrDefault(0)
    }

    val operator = when (origin) {
        PLUSEQ -> ADD
        MINUSEQ -> SUBTRACT
        MULTEQ -> MULTIPLY
        DIVEQ -> when {
            // Dart's int divide operator returns a double, while Kotlin's Int divide operator returns an
            // Int. So, we use the ~/ Dart operator, which returns an int.
            irReceiverType.isDartInt() && irOperationAssignValue.type.isDartInt() -> INTEGER_DIVIDE
            else -> DIVIDE
        }
        else -> ASSIGN
    }

    return DartAssignmentExpression(
        left = receiver,
        operator = operator,
        right = when {
            operator != ASSIGN -> irOperationAssignValue
            else -> irValue
        }.accept(this)
    )
}