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

import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.ir.firstNonFakeOverrideOrNull
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.dotlin.compiler.backend.util.replace
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.parentAsClass

/**
 * Since Dart requires you to implement _all_ members if you `implement` a class,
 * all default implementations of interfaces are copied into the implementer.
 */
@Suppress("UnnecessaryVariable")
class DefaultInterfaceImplementationsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || declaration.isInterface) return noChange()

        val irClass = declaration

        irClass.declarations
            .filter { it.isFakeOverride }
            .apply {
                fun makeNonFakeOverride(function: IrSimpleFunction): IrSimpleFunction? {
                    val superFunction = function.firstNonFakeOverrideOrNull()

                    if (superFunction?.parentAsClass?.isInterface != true) return null

                    return function.deepCopyWith { isFakeOverride = false }.apply {
                        body = superFunction.moveBodyTo(this)
                    }
                }

                // Handle function overrides.
                filterIsInstance<IrSimpleFunction>()
                    .forEach {
                        val new = makeNonFakeOverride(it) ?: return@forEach
                        irClass.declarations.replace(it, new)
                    }

                // Handle getter & setter overrides.
                filterIsInstance<IrProperty>()
                    .forEach { property ->
                        property.apply {
                            getter?.let { getter = makeNonFakeOverride(it) }
                            setter?.let { setter = makeNonFakeOverride(it) }
                        }
                    }
            }

        return noChange()
    }
}
