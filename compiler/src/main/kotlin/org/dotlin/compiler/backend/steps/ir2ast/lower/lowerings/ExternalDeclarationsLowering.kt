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

import org.dotlin.compiler.backend.DotlinAnnotations
import org.dotlin.compiler.backend.steps.ir2ast.ir.deepCopyWith
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.hasAnnotation
import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.isEffectivelyExternal
import kotlin.contracts.ExperimentalContracts

class ExternalDeclarationsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (!declaration.hasAnnotation(DotlinAnnotations.dartBuiltIn) &&
            !declaration.isEffectivelyExternal()
        ) {
            return noChange()
        }

        // We don't want to remove companion objects.
        if (declaration.isCompanion()) {
            // We don't use addChild on purpose, we want to keep parent information of the companion object.
            declaration.file.declarations.add(
                (declaration as IrClass).deepCopyWith { isExternal = false }
            )
        }

        if (declaration.parents.any { it.isCompanion() }) {
            return noChange()
        }

        return just(remove())
    }

    @OptIn(ExperimentalContracts::class)
    private fun IrElement.isCompanion() = this is IrClass && isCompanion
}