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

import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.backend.common.ir.copyTypeParametersFrom
import org.jetbrains.kotlin.backend.common.ir.copyValueParametersFrom
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.name.Name

@Suppress("UnnecessaryVariable")
class ConstructorNamesLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrConstructor) return noChange()
        // No change necessary in this case.
        if (declaration.isPrimary) return noChange()

        val irConstructor = declaration
        val parentClass = irConstructor.parentClassOrNull!!

        val name = Name.identifier(
            "\$constructor$" + parentClass.declarations
                .filterIsInstance<IrConstructor>()
                .indexOf(irConstructor)
        )

        return just {
            replaceWith(
                irConstructor.let {
                    context.irFactory.createConstructor(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        origin = it.origin,
                        symbol = IrConstructorSymbolImpl(),
                        name = name,
                        visibility = it.visibility,
                        returnType = it.returnType,
                        isInline = it.isInline,
                        isExternal = it.isExternal,
                        isPrimary = it.isPrimary,
                        isExpect = it.isExpect,
                        containerSource = it.containerSource,
                    ).apply {
                        parent = it.parent
                        body = it.body

                        copyValueParametersFrom(it, emptyMap())
                        copyTypeParametersFrom(it)
                    }
                }
            )
        }
    }
}