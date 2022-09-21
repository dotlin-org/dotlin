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

import org.dotlin.compiler.backend.steps.ir2ast.ir.buildStatement
import org.dotlin.compiler.backend.steps.ir2ast.ir.hasReferenceToThis
import org.dotlin.compiler.backend.steps.ir2ast.ir.otherPropertyDependents
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.statements

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "UnnecessaryVariable")
class PropertiesReferencingParametersLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrValueParameter) return noChange()

        val irValueParameter = declaration

        val currentIrFunction = irValueParameter.parent as IrFunction
        val currentFunctionIsConstructor = currentIrFunction is IrConstructor

        val irBuilder = createIrBuilder(currentIrFunction.symbol)

        // If a parameter is used by a property initializer, we need to move that initializer to the constructor body
        // in Dart.
        val otherPropertyDependents = irValueParameter.otherPropertyDependents
        if (currentFunctionIsConstructor && otherPropertyDependents.isNotEmpty()) {
            val body = irBuilder.irBlockBody {}.apply {
                statements.addAll(currentIrFunction.body?.statements.orEmpty())
            }

            otherPropertyDependents.mapNotNullTo(body.statements) { prop ->
                val value = prop.backingField?.initializer?.expression
                if (!prop.isInitializedSomewhereElse && value != null) {
                    when {
                        value.hasReferenceToThis() -> prop.isInitializedInConstructorBody = true
                        else -> prop.isInitializedInFieldInitializerList = true
                    }

                    irBuilder.buildStatement {
                        irSetField(
                            receiver = prop.parentClassOrNull?.thisReceiver?.let { r -> irGet(r) },
                            field = prop.backingField!!,
                            value = value
                        )
                    }
                } else {
                    null
                }
            }

            currentIrFunction.body = body
        }

        return noChange()
    }
}