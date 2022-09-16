package org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.type

import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.calls.checkers.AdditionalTypeChecker
import org.jetbrains.kotlin.resolve.calls.context.ResolutionContext
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isChar

object CharTypeChecker : AdditionalTypeChecker{
    override fun checkType(
        expression: KtExpression,
        expressionType: KotlinType,
        expressionTypeWithSmartCast: KotlinType,
        c: ResolutionContext<*>
    ) {
        if (expressionType.isChar()) {
            c.trace.report(ErrorsDart.CHAR_REFERENCE.on(expression))
        }
    }
}