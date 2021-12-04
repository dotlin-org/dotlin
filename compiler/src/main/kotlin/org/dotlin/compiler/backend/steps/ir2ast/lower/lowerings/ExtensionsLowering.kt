/*
 * Copyright 2021 Wilko Manger
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
import org.dotlin.compiler.backend.steps.ir2ast.ir.owner
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.common.ir.createParameterDeclarations
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isSetter
import org.jetbrains.kotlin.name.Name

class ExtensionsLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    private val extensionContainers = mutableMapOf<IrType, IrClass>()

    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        val extensionReceiver = when (declaration) {
            is IrFunction -> when {
                !declaration.isGetter && !declaration.isSetter -> declaration.extensionReceiverParameter
                else -> return noChange()
            }
            is IrProperty -> declaration.getter?.extensionReceiverParameter
            else -> return noChange()
        } ?: return noChange()

        val extensionContainer = extensionContainers.getOrPut(extensionReceiver.type) {
            context.irFactory.buildClass {
                origin = IrDartDeclarationOrigin.EXTENSION
                name = Name.identifier("$${extensionReceiver.type.owner.name.identifier}Ext")
            }.apply {
                declaration.file.let {
                    parent = it
                    it.declarations.add(this)
                }
                createParameterDeclarations()
            }
        }

        extensionContainer.declarations.add(declaration)

        return just { remove() }
    }
}