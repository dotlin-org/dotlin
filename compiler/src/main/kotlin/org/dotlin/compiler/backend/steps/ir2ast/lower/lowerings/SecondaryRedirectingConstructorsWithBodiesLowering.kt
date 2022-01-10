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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDartDeclarationOrigin
import org.dotlin.compiler.backend.steps.ir2ast.ir.transformExpressions
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.util.copyTypeAndValueArgumentsFrom
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

/**
 * Constructors that redirect cannot
 */
@Suppress("UnnecessaryVariable")
class SecondaryRedirectingConstructorsWithBodiesLowering(override val context: DartLoweringContext) :
    IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrConstructor || declaration.isPrimary) return noChange()

        val body = declaration.body
        if (body !is IrBlockBody) return noChange()

        val redirectCall = body.statements.firstIsInstanceOrNull<IrDelegatingConstructorCall>() ?: return noChange()

        // If the only statement is the delegating constructor call, there's no body.
        if (body.statements.size <= 1) return noChange()

        declaration.origin = IrDartDeclarationOrigin.FACTORY

        val classType = declaration.parentAsClass.defaultType

        body.statements.apply {
            remove(redirectCall)

            val instanceVar = buildStatement(declaration.symbol) {
                scope.createTemporaryVariable(
                    redirectCall.let {
                        IrConstructorCallImpl(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                            classType,
                            it.symbol,
                            it.typeArgumentsCount,
                            constructorTypeArgumentsCount = 0,
                            it.valueArgumentsCount,
                        ).apply {
                            copyTypeAndValueArgumentsFrom(it)
                        }
                    },
                    nameHint = "instance",
                    irType = classType
                )
            }

            val thisSymbol = declaration.parentAsClass.thisReceiver!!.symbol

            body.transformExpressions(initialParent = declaration) { exp, parent ->
                exp.transformChildren(parent)
                when {
                    exp is IrGetValue && exp.symbol == thisSymbol -> buildStatement(parent.symbol) {
                        irGet(instanceVar)
                    }
                    else -> exp
                }
            }

            add(index = 0, instanceVar)
            add(
                buildStatement(declaration.symbol) {
                    irReturn(
                        irGet(instanceVar)
                    )
                }
            )
        }

        return noChange()
    }
}