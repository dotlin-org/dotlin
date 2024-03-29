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

import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDotlinDeclarationOrigin
import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.ir.firstNonFakeOverrideOrNull
import org.dotlin.compiler.backend.steps.ir2ast.ir.isFakeOverride
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.dotlin.compiler.backend.util.replace
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.parentAsClass

/**
 * Since Dart requires you to implement _all_ members if you `implement` a class,
 * all default implementations of interfaces are copied into the implementer.
 */
@Suppress("UnnecessaryVariable")
class DefaultInterfaceImplementationsLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || declaration.isInterface) return noChange()

        val irClass = declaration

        irClass.declarations
            .filter { it.isFakeOverride() }
            .apply {
                fun makeNonFakeOverride(function: IrSimpleFunction): IrSimpleFunction? {
                    val superFunction = function.firstNonFakeOverrideOrNull()

                    if (superFunction?.parentAsClass?.isInterface != true) return null

                    return function.deepCopyWith { isFakeOverride = false }.apply {
                        body = superFunction.moveBodyTo(this)
                        origin = when (origin) {
                            IrDeclarationOrigin.FAKE_OVERRIDE -> IrDotlinDeclarationOrigin.COPIED_OVERRIDE
                            else -> origin
                        }
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
                            getter?.let { getter = makeNonFakeOverride(it) ?: it }
                            setter?.let { setter = makeNonFakeOverride(it) ?: it }
                        }
                    }
            }

        return noChange()
    }
}
