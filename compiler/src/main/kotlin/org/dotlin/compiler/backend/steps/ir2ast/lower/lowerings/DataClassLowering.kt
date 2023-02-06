package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings

import org.dotlin.compiler.backend.steps.ir2ast.ir.copy
import org.dotlin.compiler.backend.steps.ir2ast.ir.methodWithName
import org.dotlin.compiler.backend.steps.ir2ast.ir.transformExpressions
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.MUL
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin.PLUS
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentClassOrNull

/**
 * Fixes the `times` and `plus` calls in data class' `hashCode`.
 */
class DataClassLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        // For some reason, companion objects are marked as data classes.
        if (declaration !is IrClass || !declaration.isData || declaration.isCompanion) return noChange()

        val hashCodeFun = declaration.methodWithName("hashCode")

        hashCodeFun.body!!.transformExpressions(initialParent = hashCodeFun) { expression, _ ->
            fun IrFunctionSymbol.parentIsInt() = owner.parentClassOrNull?.defaultType?.isInt() == true

            when {
                expression is IrCall && expression.symbol.parentIsInt() -> expression.copy(
                    origin = when (val name = expression.symbol.owner.name.toString()) {
                        "times" -> MUL
                        "plus" -> PLUS
                        else -> throw UnsupportedOperationException("Unexpected call: $name")
                    }
                )
                else -> expression
            }
        }

        return noChange()
    }
}