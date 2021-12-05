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
import org.dotlin.compiler.backend.steps.ir2ast.ir.isExplicitBackingField
import org.dotlin.compiler.backend.steps.ir2ast.ir.isPrivate
import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.name.Name

/**
 * Dart uses underscore prefixes in identifier names to denote a declaration is private.
 */
class PrivateNamesLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrDeclarationWithName || declaration !is IrDeclarationWithVisibility) return noChange()
        // We want to handle constructor names, which are special by default.
        if (declaration.name.isSpecial && (declaration !is IrConstructor || !declaration.isPrivate)) return noChange()

        val originalName = when {
            declaration is IrConstructor && declaration.name.isSpecial -> ""
            else -> declaration.name.identifier
        }

        if (declaration.isPrivate && originalName.startsWith('_')) return noChange()
        // Implicit backing fields are skipped.
        if (declaration is IrField && !declaration.isExplicitBackingField) return noChange()

        val newName = when {
            declaration.isPrivate -> Name.identifier("_$originalName")
            // Names that start with an underscore but are not private, should have their underscore(s) removed.
            else -> Name.identifier(originalName.replace(Regex("^_+"), ""))
        }

        // No change, we can leave.
        if (newName == declaration.name) return noChange()

        val newDeclaration = when (declaration) {
            is IrClass -> declaration.deepCopyWith { name = newName }
            is IrConstructor -> declaration.deepCopyWith { name = newName }
            is IrFunction -> declaration.deepCopyWith { name = newName }
            is IrProperty -> declaration.deepCopyWith { name = newName }
            is IrField -> declaration.deepCopyWith { name = newName }
            else -> return noChange()
        }

        return just { replaceWith(newDeclaration) }
    }
}