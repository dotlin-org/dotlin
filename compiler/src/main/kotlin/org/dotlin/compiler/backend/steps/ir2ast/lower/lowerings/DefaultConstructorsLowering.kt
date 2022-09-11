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

import org.dotlin.compiler.backend.steps.ir2ast.ir.valueArguments
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.util.isSpecialInheritanceMarker
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.Name

/**
 * Default constructors (primary, no parameters, nothing passed to super, etc.) are removed.
 */
@Suppress("UnnecessaryVariable")
class DefaultConstructorsLowering(override val context: DartLoweringContext) : IrDeclarationLowering {
    override fun DartLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrConstructor) return noChange()

        if (!declaration.isPrimary ||
            declaration.name != Name.special("<init>") ||
            declaration.visibility != DescriptorVisibilities.PUBLIC ||
            declaration.annotations.isNotEmpty() ||
            declaration.valueParameters.isNotEmpty() ||
            declaration.typeParameters.isNotEmpty()
        ) {
            return noChange()
        }

        val body = declaration.body

        if (body == null || body.statements.size != 1) return noChange()

        val delegatingConstructorCall = body.statements
            .filterIsInstance<IrDelegatingConstructorCall>()
            .singleOrNull()
            ?: return noChange()

        delegatingConstructorCall.let {
            // The super constructor call for the special inheritance (implicit interfaces, mixins) should be removed.
            if (!it.isSpecialInheritanceConstructorCall() && (it.valueArgumentsCount > 0 || it.typeArgumentsCount > 0)) {
                return noChange()
            }
        }

        return just { remove() }
    }

    private fun IrDelegatingConstructorCall.isSpecialInheritanceConstructorCall(): Boolean {
        return valueArguments.singleOrNull()?.let {
            it is IrGetObjectValue && it.type.isSpecialInheritanceMarker()
        } == true
    }
}