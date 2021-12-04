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

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.name.Name

/**
 * Dart uses underscore prefixes in identifier names to denote a declaration is private.
 */
class PrivateNamesLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrDeclarationWithName || declaration !is IrDeclarationWithVisibility) return noChange()
        if (declaration.name.isSpecial) return noChange()
        if (declaration.isPrivate && declaration.name.identifier.startsWith('_')) return noChange()
        // Implicit backing fields are skipped.
        if (declaration is IrField && !declaration.isExplicitBackingField) return noChange()

        val newName = if (declaration.isPrivate)
            Name.identifier("_${declaration.name.identifier}")
        // Names that start with an underscore but are not private, should have their underscore(s) removed.
        else
            Name.identifier(declaration.name.identifier.replace(Regex("^_+"), ""))

        // No change, we can leave.
        if (newName == declaration.name) return noChange()

        val newDeclaration = when (declaration) {
            // TODO: deepCopy for IrFunction and IrConstructor
            is IrClass -> declaration.deepCopyWith { name = newName }
            is IrConstructor -> context.irFactory.buildConstructorFrom(declaration) { name = newName }
            is IrFunction -> context.irFactory.buildFunFrom(declaration) { name = newName }
            is IrProperty -> declaration.deepCopyWith { name = newName }
            is IrField -> declaration.deepCopyWith { name = newName }
            else -> return noChange()
        }

        return just { replaceWith(newDeclaration) }
    }
}