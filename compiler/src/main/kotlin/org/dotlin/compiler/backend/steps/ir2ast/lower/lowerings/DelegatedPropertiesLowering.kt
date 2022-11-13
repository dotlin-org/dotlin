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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDotlinDeclarationOrigin
import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.ir.irCall
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.name.Name

/**
 * Must run before [PropertySimplifyingLowering].
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class DelegatedPropertiesLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrProperty || !declaration.isDelegated) return noChange()

        val delegateField = declaration.backingField!!

        return just {
            add(delegateField).also {
                delegateField.correspondingPropertySymbol = null
            }
        }
    }

    class Local(override val context: DotlinLoweringContext) : IrDeclarationLowering {
        override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
            if (declaration !is IrLocalDelegatedProperty) return noChange()

            var transformations = add(declaration.delegate, before = true) and remove(declaration)

            val getter = declaration.getter.deepCopyWith(remapReferences = false) {
                name = Name.identifier("get\$${declaration.name}")
                origin = IrDotlinDeclarationOrigin.LOCAL_DELEGATED_PROPERTY_REFERENCE_ACCESSOR
            }.also {
                transformations += add(it)
            }

            val setter = declaration.setter?.deepCopyWith(remapReferences = false) {
                name = Name.identifier("set\$${declaration.name}")
                origin = IrDotlinDeclarationOrigin.LOCAL_DELEGATED_PROPERTY_REFERENCE_ACCESSOR
            }?.also {
                transformations += add(it)
            }

            (declaration.parent as IrFunction).remapLocalPropertyAccessors(
                getter = { irCall(getter) },
                setter = { irCall(setter!!, receiver = null, it.getValueArgument(0)!!) }
            )

            return transformations
        }
    }
}