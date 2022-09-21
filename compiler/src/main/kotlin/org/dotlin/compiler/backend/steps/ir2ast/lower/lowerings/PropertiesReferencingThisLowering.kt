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

import org.dotlin.compiler.backend.steps.ir2ast.ir.hasReferenceToThis
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class PropertiesReferencingThisLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrProperty || !declaration.hasReferenceToThis()) return noChange()

        val primaryConstructor = declaration.parentAsClass.primaryConstructor ?: return noChange()
        val primaryConstructorBody = primaryConstructor.body as IrBlockBody

        // If we have a reference to this in the initializer, backingField is guaranteed to not be null.
        val backingField = declaration.backingField!!

        declaration.isInitializedInConstructorBody = true

        primaryConstructorBody.statements.add(
            buildStatement(primaryConstructor.symbol) {
                irSetField(
                    receiver = irGet(declaration.parentAsClass.thisReceiver!!),
                    field = backingField,
                    value = backingField.initializer!!.expression,
                )
            }
        )

        return noChange()
    }
}