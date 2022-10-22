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

package org.dotlin.compiler.backend.steps.ir2ast.lower.lowerings.output

import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.dotlin.compiler.backend.util.isDotlinExternal
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.primaryConstructor

/**
 * Because source information is not available for code in dependencies, we have to add the `@DotlinExternal` annotation
 * to companion objects (or its members) that are marked explicitly `external`.
 */
class AnnotateExternalCompanionObjectsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        return declaration.let {
            if (((it is IrClass && it.isCompanion) || (it.parentClassOrNull?.isCompanion == true)) &&
                it.isDotlinExternal
            ) {
                val dotlinExternal = dartBuiltIns.dotlin.dotlinExternal
                it.annotations = it.annotations + IrConstructorCallImpl(
                    UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                    type = dotlinExternal.defaultType,
                    symbol = dotlinExternal.owner.primaryConstructor!!.symbol,
                    typeArgumentsCount = 0,
                    constructorTypeArgumentsCount = 0,
                    valueArgumentsCount = 0,
                )
            }

            noChange()
        }
    }
}