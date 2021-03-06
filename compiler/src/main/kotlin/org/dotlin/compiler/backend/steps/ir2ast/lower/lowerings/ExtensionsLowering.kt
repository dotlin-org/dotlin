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

import org.dotlin.compiler.backend.steps.ir2ast.ir.extensionReceiverOrNull
import org.dotlin.compiler.backend.steps.ir2ast.ir.typeParameterOrNull
import org.dotlin.compiler.backend.steps.ir2ast.ir.typeParametersOrSelf
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.TypeRemapper
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.remapTypes

class ExtensionsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        val extensionContainer = declaration.extensionContainer ?: return noChange()

        declaration.file.apply {
            if (extensionContainer !in declarations) {
                addChild(extensionContainer)
            }
        }

        extensionContainer.apply {
            declaration.file.declarations.remove(declaration)
            addChild(declaration)
        }

        val oldReceiverTypeParameters = declaration.extensionReceiverOrNull!!.type.typeParametersOrSelf
        if (oldReceiverTypeParameters.isNotEmpty()) {
            if (declaration is IrFunction) {
                declaration.typeParameters -= oldReceiverTypeParameters
            }

            val newReceiverTypeParameter = extensionContainer.typeParameters

            val newToOldReceiverTypeParameters = oldReceiverTypeParameters
                .zip(newReceiverTypeParameter)
                .toMap()

            // Remap type parameters.
            declaration.remapTypes(
                object : TypeRemapper {
                    override fun enterScope(irTypeParametersContainer: IrTypeParametersContainer) {}

                    override fun leaveScope() {}

                    override fun remapType(type: IrType): IrType {
                        val typeParameter = type.typeParameterOrNull ?: return type
                        return newToOldReceiverTypeParameters[typeParameter]?.defaultType ?: return type
                    }
                }
            )
        }




        return just { remove() }
    }
}