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

import org.dotlin.compiler.backend.steps.ir2ast.lower.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.RemapLevel.NONE
import org.jetbrains.kotlin.backend.common.ir.setDeclarationsParent
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.copyAttributes

/**
 * Dart has no `interface`, these will be converted to `abstract class`es.
 */
@Suppress("UnnecessaryVariable")
class InterfaceToAbstractClassLowering(private val context: DartLoweringContext) : IrDeclarationTransformer {
    override fun transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass || declaration.kind != ClassKind.INTERFACE) return noChange()

        val irClass = declaration

        return just {
            replaceWith(
                context.irFactory.buildClass {
                    updateFrom(irClass)

                    kind = ClassKind.CLASS
                    modality = Modality.ABSTRACT
                }.apply {
                    parent = irClass.parent
                    declarations.addAll(irClass.declarations)
                    copyAttributes(irClass)
                    setDeclarationsParent(this)
                },
                remapAt = NONE
            )
        }
    }
}
