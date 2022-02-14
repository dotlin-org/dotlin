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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.builtins

import org.dotlin.compiler.backend.steps.ir2ast.ir.irCall
import org.dotlin.compiler.backend.steps.ir2ast.ir.methodWithName
import org.dotlin.compiler.backend.steps.ir2ast.ir.typeArguments
import org.dotlin.compiler.backend.steps.ir2ast.ir.valueArguments
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.getPublicSignature
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

object EnumValues {
    class RemoveDeclarationsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
        override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
            if (!declaration.isEnumValues() && !declaration.isEnumValueOf()) return noChange()

            return just { remove() }
        }
    }

    class ReplaceExpressionsLowering(override val context: DartLoweringContext) : IrExpressionLowering {
        override fun <D> DartLoweringContext.transform(
            expression: IrExpression,
            container: D
        ): Transformation<IrExpression>? where D : IrDeclaration, D : IrDeclarationParent {
            if (expression !is IrCall ||
                (!expression.symbol.owner.isEnumValues() && !expression.symbol.owner.isEnumValueOf())
            ) {
                return noChange()
            }

            val function = expression.symbol.owner
            val enumClass = expression.typeArguments.singleOrNull()?.classOrNull?.owner ?: return noChange()

            return replaceWith(
                when {
                    function.isEnumValues() -> buildStatement(container.symbol) {
                        irCall(enumClass.methodWithName("values"))
                    }
                    else -> buildStatement(container.symbol) {
                        irCall(
                            enumClass.methodWithName("valueOf"),
                            receiver = null,
                            expression.valueArguments.single()!!
                        )
                    }
                }
            )
        }
    }
}


private fun IrDeclaration.isEnumValues() =
    this is IrFunction && kotlinFqName == FqName.fromSegments(listOf("kotlin", "enumValues"))

private fun IrDeclaration.isEnumValueOf() =
    this is IrFunction && kotlinFqName == FqName.fromSegments(listOf("kotlin", "enumValueOf"))