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

import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationTransformer
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.parentClassOrNull

/**
 * Since Dart requires you to implement _all_ members if you `implement` a class,
 * all default implementations of interfaces are copied into the implementer.
 */
@Suppress("UnnecessaryVariable")
class DefaultInterfaceImplementationsLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || declaration.isInterface) return noChange()

        val irClass = declaration

        // TODO: Support multiple interfaces
        val superClass = irClass.superTypes.firstOrNull()?.classifierOrNull?.owner as? IrClass ?: return noChange()
        if (!superClass.isInterface) return noChange()

        irClass.declarations
            .filter { it.isFakeOverride }
            .apply {
                fun applyBodyOfSuperTo(function: IrSimpleFunction) {
                    val superFunction = function.overriddenSymbols
                        .firstOrNull { it.owner.parentClassOrNull == superClass }
                        ?.owner

                    function.body = superFunction?.moveBodyTo(function)
                }

                // Handle function overrides.
                filterIsInstance<IrSimpleFunction>()
                    .forEach(::applyBodyOfSuperTo)

                // Handle getter & setter overrides.
                filterIsInstance<IrProperty>()
                    .forEach { property ->
                        property.apply {
                            getter?.let { applyBodyOfSuperTo(it) }
                            setter?.let { applyBodyOfSuperTo(it) }
                        }
                    }
            }

        return noChange()
    }

}
