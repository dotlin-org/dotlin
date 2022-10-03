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

import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.ir.type
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.utils.addToStdlib.lastIsInstanceOrNull

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class LazyPropertiesLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if ((declaration !is IrLocalDelegatedProperty || !declaration.isLazy) &&
            (declaration !is IrProperty || !declaration.isLazy)
        ) {
            return noChange()
        }

        fun IrExpression?.extractReturnOrCallLambda(): IrExpression {
            this as IrCall
            val functionExp = getValueArgument(0)!! as IrFunctionExpression

            val singleReturn = functionExp.function.body!!.statements.let {
                when {
                    it.size > 1 -> null
                    else -> it.lastIsInstanceOrNull<IrReturn>()
                }
            }

            return when (singleReturn) {
                null -> functionExp.irCall()
                else -> singleReturn.value
            }
        }

        return just {
            replaceWith(
                declaration.let {
                    when (it) {
                        is IrProperty -> it.deepCopyWith {
                            isLateinit = true
                            isDelegated = false
                        }.apply {
                            backingField!!.apply {
                                initializer = IrExpressionBodyImpl(
                                    initializer!!.expression.extractReturnOrCallLambda()
                                )

                                type = it.type
                            }

                            getter = createDefaultGetter(backingField!!.type)
                            setter = null
                        }
                        is IrLocalDelegatedProperty -> IrVariableImpl(
                            it.startOffset, SYNTHETIC_OFFSET,
                            it.origin,
                            symbol = IrVariableSymbolImpl(),
                            it.name,
                            it.type,
                            it.isVar,
                            isConst = false,
                            isLateinit = true
                        ).apply {
                            val variable = this

                            parent = it.parent
                            initializer = it.delegate.initializer.extractReturnOrCallLambda()

                            (parent as IrFunction).remapLocalPropertyAccessors(
                                getter = { irGet(variable) },
                                setter = { exp -> irSet(variable, exp.getValueArgument(0)!!) }
                            )
                        }
                        else -> error("Unsupported lazy property: $it")
                    }
                }
            )
        }
    }

    private val IrLocalDelegatedProperty.isLazy: Boolean
        get() = delegate.type.isLazy

    private val IrProperty.isLazy: Boolean
        get() = isDelegated && backingField!!.type.isLazy

    private val IrType.isLazy: Boolean
        get() = classFqName == FqName("kotlin.Lazy") // TODO: Use FqName syntax
}
