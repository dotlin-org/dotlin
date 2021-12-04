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

import org.dotlin.compiler.backend.steps.ir2ast.ir.buildConstructorFrom
import org.dotlin.compiler.backend.steps.ir2ast.ir.remap
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrExpressionBodyImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isThrowable
import org.jetbrains.kotlin.name.Name

// TODO: Can go, just edit stdlib source.

@Suppress("UnnecessaryVariable")
class ThrowableLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || !declaration.defaultType.isThrowable()) return noChange()

        val throwable = declaration

        // Replace the old 'primary' constructor with a constructor that's actually marked as primary.
        val primaryConstructor = throwable.declarations
            .filterIsInstance<IrConstructor>()
            .first { it.valueParameters.size == 2 }
            .let {
                val primaryConstructor = context.irFactory.buildConstructorFrom(it) {
                    isPrimary = true
                }

                throwable.declarations.add(throwable.declarations.indexOf(it), primaryConstructor)
                throwable.declarations.remove(it)

                throwable.remap(it to primaryConstructor)

                primaryConstructor
            }

        // Remove toString.
        throwable.declarations.removeIf {
            it is IrFunction && it.name.asString() == "toString"
        }

        // Initialize properties with the corresponding parameters.
        throwable.declarations
            .filterIsInstance<IrProperty>()
            .forEach { property ->
                val getter = property.getter!!

                getter.origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR

                property.backingField = context.irFactory.buildField {
                    name = Name.special("<getter>")
                    type = getter.returnType
                }.apply {
                    initializer = IrExpressionBodyImpl(
                        IrGetValueImpl(
                            UNDEFINED_OFFSET,
                            UNDEFINED_OFFSET,
                            type = getter.returnType,
                            symbol = primaryConstructor
                                .valueParameters
                                .first { param -> param.name == property.name }
                                .symbol,
                            origin = IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER
                        )
                    )
                }
            }

        return just { replaceWith(throwable) }
    }
}