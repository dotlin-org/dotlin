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

import org.dotlin.compiler.backend.SuperTypeKind
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.dotlin.compiler.backend.util.createAnnotation
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.impl.buildSimpleType
import org.jetbrains.kotlin.ir.types.impl.toBuilder
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

/**
 * Because source information is not available for code in dependencies, we have to add
 * the `@SpecialInheritance` annotation to super types that use this mechanism.
 */
class AnnotateSpecialInheritanceTypes(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> {
        if (declaration !is IrClass) return noChange()

        val specialSuperTypes = declaration.superTypes().filter { it.kind is SuperTypeKind.Special }

        declaration.superTypes = declaration.superTypes.map { irSuperType ->
            when (val superTypeInfo = specialSuperTypes.firstOrNull { it.type == irSuperType }) {
                null -> irSuperType
                else -> irSuperType.safeAs<IrSimpleType>()?.toBuilder()?.run {
                    annotations = annotations + listOf(
                        buildStatement(declaration.symbol) {
                            createAnnotation(
                                dotlinIrBuiltIns.specialInheritedType,
                                superTypeInfo.kind.toString().toIrConst(irBuiltIns.stringType)
                            )
                        }
                    )
                    buildSimpleType()
                } ?: irSuperType
            }
        }

        return noChange()
    }
}