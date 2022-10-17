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

import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.isDartConstInlineFunction
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable

/**
 * "Const" variables that are not actually const are made non-const.
 */
@Suppress("UnnecessaryVariable")
class ConstInlineFunctionsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrVariable || !declaration.parent.isDartConstInlineFunction()) {
            return noChange()
        }

        // We don't supply the const inline container on purpose: If it's not considered const without it,
        // it's not actually Dart const. Thus, we make it 'final'.
        if (declaration.initializer?.isDartConst(implicit = true) != true) {
            return just {
                replaceWith(
                    declaration.deepCopyWith {
                        isConst = false
                    }
                )
            }
        }

        return noChange()
    }
}