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

import org.dotlin.compiler.backend.hasDartExtensionAnnotation
import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.ir.isAbstract
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.statements

class DartExtensionsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrSimpleFunction && declaration !is IrProperty && declaration.parent !is IrClass) {
            return noChange()
        }

        if (declaration.isAbstract) return noChange()

        if (!declaration.hasDartExtensionAnnotation()) return noChange()

        declaration.also {
            when (it) {
                is IrSimpleFunction -> it.toExtension()
                is IrProperty -> {
                    it.getter = it.getter?.toExtension()
                    it.setter = it.setter?.toExtension()
                }
            }
        }

        return noChange()
    }

    private fun IrSimpleFunction.toExtension(): IrSimpleFunction {
        parentAsClass.declarations.remove(this)

        return deepCopyWith {
            isExternal = false
        }.apply {
            extensionReceiverParameter = dispatchReceiverParameter
            dispatchReceiverParameter = null
            file.addChild(this)
        }
    }
}

