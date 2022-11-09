/*
 * Copyright 2022 Wilko Manger
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

package org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers.post

import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.isPropertyAccessor
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.util.collectionUtils.filterIsInstanceAnd
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

object DartNameChecker : IrDeclarationChecker {
    override val reports = listOf(ErrorsDart.DART_NAME_CLASH)

    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration !is IrDeclarationWithName) return
        val scope = declaration.parents.firstIsInstanceOrNull<IrDeclarationContainer>() ?: return

        val dartName = declaration.dartNameOrNull ?: return

        val clashes = scope.declarations
            .filterIsInstanceAnd<IrDeclarationWithName> { it != declaration && it.dartNameOrNull == dartName }

        for (clash in clashes) {
            // Value parameter clashes are ignored.
            if (declaration is IrValueParameter) {
                continue
            }

            // Constructors don't clash with functions, properties or fields.
            if (declaration is IrConstructor && (clash is IrFunction || clash is IrProperty || clash is IrField)) {
                continue
            }

            // We ignore getters and setters that "clash" with their own property.
            if (declaration is IrSimpleFunction && declaration.isPropertyAccessor &&
                clash is IrProperty && declaration.correspondingPropertySymbol == clash.symbol
            ) {
                continue
            }

            // We ignore getters and setters that "clash" with each other.
            if (declaration is IrSimpleFunction && clash is IrSimpleFunction &&
                declaration.isPropertyAccessor && clash.isPropertyAccessor &&
                declaration.correspondingPropertySymbol == clash.correspondingPropertySymbol
            ) {
                continue
            }

            // We ignore local variables that "clash" with class members.
            if (declaration.parent is IrFunction && clash.parent !is IrFunction && scope is IrClass) {
                continue
            }

            trace.report(ErrorsDart.DART_NAME_CLASH.on(source, dartName.value, clash.name.toString()))
        }
    }
}