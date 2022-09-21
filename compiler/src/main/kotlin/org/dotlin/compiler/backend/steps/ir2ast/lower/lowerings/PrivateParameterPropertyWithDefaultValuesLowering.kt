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

import org.dotlin.compiler.backend.steps.ir2ast.ir.correspondingProperty
import org.dotlin.compiler.backend.steps.ir2ast.ir.isPrivate
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.util.parentAsClass

/**
 * If the property is private and has a default value in the constructor, Dart gives an error since
 * Dart does not allow named parameters starting with an underscore. We fix that by initializing the property
 * in the field initializer list.
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class PrivateParameterPropertyWithDefaultValuesLowering(override val context: DartLoweringContext) :
    IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrValueParameter ||
            declaration.parent !is IrConstructor
            || declaration.defaultValue == null) {
            return noChange()
        }

        val property = declaration.correspondingProperty

        if (property == null || !property.isPrivate || property.isInitializedSomewhereElse) {
            return noChange()
        }

        val backingField = property.backingField!!
        val constructor = declaration.parent as? IrConstructor ?: return noChange()
        val constructorBody = constructor.body as IrBlockBody

        property.isInitializedInFieldInitializerList = true

        constructorBody.statements.add(
            buildStatement(constructor.symbol) {
                irSetField(
                    receiver = irGet(constructor.parentAsClass.thisReceiver!!),
                    field = backingField,
                    value = backingField.initializer!!.expression,
                )
            }
        )

        return noChange()
    }
}