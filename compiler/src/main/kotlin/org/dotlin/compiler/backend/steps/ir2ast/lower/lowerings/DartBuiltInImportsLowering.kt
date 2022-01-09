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

import org.dotlin.compiler.backend.DotlinAnnotations
import org.dotlin.compiler.backend.dartHideImportLibrary
import org.dotlin.compiler.backend.dartImportAliasLibrary
import org.dotlin.compiler.backend.dartImportAliasPrefix
import org.dotlin.compiler.backend.steps.ir2ast.attributes.DartImport
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementVisitorVoid
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrFileLowering
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.simpleDartNameOrNull
import org.dotlin.compiler.backend.util.hasAnnotation
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.TypeRemapper
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.remapTypes
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

/**
 * Dart import directives are added for Kotlin classes that would name clash with Dart built-ins.
 */
class DartBuiltInImportsLowering(override val context: DartLoweringContext) : IrFileLowering {
    override fun DartLoweringContext.transform(file: IrFile) {
        val imports = mutableSetOf<DartImport>()

        fun possiblyAddImportAliasOrHide(declaration: IrDeclarationWithName) {
            val dartName = declaration.simpleDartNameOrNull?.value ?: return

            declaration.let {
                val dartImportAlias = when {
                    it.hasAnnotation(DotlinAnnotations.dartImportAlias) -> DartImport(
                        library = it.dartImportAliasLibrary!!,
                        alias = it.dartImportAliasPrefix,
                        hide = dartName
                    )
                    else -> null
                }

                val dartImportHide = when {
                    it.hasAnnotation(DotlinAnnotations.dartHideImport) -> DartImport(
                        library = it.dartHideImportLibrary!!,
                        hide = dartName
                    )
                    else -> null
                }

                imports.addAll(listOfNotNull(dartImportAlias, dartImportHide))
            }
        }

        file.acceptChildrenVoid(
            object : IrCustomElementVisitorVoid {
                override fun visitDeclarationReference(expression: IrDeclarationReference) {
                    super.visitDeclarationReference(expression)

                    val referenced = when (val owner = expression.symbol.owner) {
                        is IrConstructor -> owner.parentClassOrNull
                        else -> owner as? IrDeclarationWithName
                    } ?: return

                    possiblyAddImportAliasOrHide(referenced)
                }

                override fun visitMemberAccess(expression: IrMemberAccessExpression<*>) =
                    visitDeclarationReference(expression)

                override fun visitElement(element: IrElement) = element.acceptChildrenVoid(this)
            }
        )

        file.remapTypes(
            object : TypeRemapper {
                override fun remapType(type: IrType): IrType {
                    type.classOrNull?.owner?.also { possiblyAddImportAliasOrHide(it) }
                    return type
                }

                override fun enterScope(irTypeParametersContainer: IrTypeParametersContainer) {}
                override fun leaveScope() {}

            }
        )

        file.addDartImports(imports)
    }
}