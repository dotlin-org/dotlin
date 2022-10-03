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
import org.dotlin.compiler.backend.steps.ir2ast.ir.irCall
import org.dotlin.compiler.backend.steps.ir2ast.ir.transformExpressions
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.name.Name

/**
 * Must run before [PropertySimplifyingLowering].
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class DelegatedPropertiesLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrProperty || !declaration.isDelegated) return noChange()

        val delegateField = declaration.backingField!!

        return just {
            add(delegateField).also {
                delegateField.correspondingPropertySymbol = null
            }
        }
    }

    class Local(override val context: DartLoweringContext) : IrDeclarationLowering {
        override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
            if (declaration !is IrLocalDelegatedProperty) return noChange()

            var transformations = add(declaration.delegate, before = true) and remove(declaration)

            val getter = declaration.getter.deepCopyWith(remapReferences = false) {
                name = Name.identifier("get\$${declaration.name}")
            }.also {
                transformations += add(it)
            }

            val setter = declaration.setter?.deepCopyWith(remapReferences = false) {
                name = Name.identifier("set\$${declaration.name}")
            }?.also {
                transformations += add(it)
            }

            (declaration.parent as IrFunction).let { parent ->
                parent.transformExpressions(initialParent = parent) { exp, _ ->
                    when (exp) {
                        is IrCall -> when (exp.origin) {
                            IrStatementOrigin.GET_LOCAL_PROPERTY -> buildStatement(parent.symbol) {
                                irCall(getter)
                            }
                            else -> when (exp.symbol.owner.origin) {
                                // Must be a 'set' in this case.
                                IrDeclarationOrigin.DELEGATED_PROPERTY_ACCESSOR -> buildStatement(parent.symbol) {
                                    irCall(setter!!, receiver = null, exp.getValueArgument(0)!!)
                                }
                                else -> exp
                            }
                        }
                        else -> exp
                    }
                }
            }

            return transformations
        }
    }
}