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

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.lower.IrDeclarationLowering
import org.dotlin.compiler.backend.steps.ir2ast.lower.Transformations
import org.dotlin.compiler.backend.steps.ir2ast.lower.noChange
import org.dotlin.compiler.backend.util.annotate
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.isAnnotationClass
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.isLocal
import org.jetbrains.kotlin.ir.util.parentClassOrNull

class DartMetaAnnotationsLowering(override val context: DotlinLoweringContext) : IrDeclarationLowering {
    override fun DotlinLoweringContext.transform(declaration: IrDeclaration): Transformations<IrDeclaration> =
        declaration.run {
            val isExtension = isExtension

            val isStatic = when (this) {
                is IrField -> isStatic
                is IrFunction -> isStatic
                else -> false
            }

            when (this) {
                is IrDeclarationWithVisibility -> when (visibility) {
                    DescriptorVisibilities.INTERNAL -> annotate(dotlinIrBuiltIns.meta.internal)
                    else -> when {
                        !isExtension -> when (visibility) {
                            DescriptorVisibilities.PROTECTED -> annotate(dotlinIrBuiltIns.meta.protected)
                        }
                    }
                }

            }

            val modality = when (this) {
                is IrOverridableDeclaration<*> -> modality
                is IrField -> correspondingProperty?.modality
                is IrClass -> when {
                    // For some reason, annotation classes are now marked as "open" in the IR,
                    // even though that's not possible.
                    isAnnotationClass -> Modality.FINAL
                    else -> modality
                }

                else -> null
            }

            // Fake overrides of enum are not outputted in the generated Dart code, so no need to
            // annotate them.
            val isFakeOverrideOfEnum = isFakeOverride() && parentClassOrNull?.isEnumClass == true

            if (!isExtension && !isFakeOverrideOfEnum) {
                when (modality) {
                    Modality.SEALED, Modality.FINAL -> when {
                        // Sealed as well as final classes get marked @sealed.
                        // Enum classes don't have to be marked @sealed, they are always sealed in Dart.
                        this is IrClass && !isDartExtensionContainer && !isEnumClass -> {
                            annotate(dotlinIrBuiltIns.meta.sealed)
                        }
                        // Non-top-level, non-static and non-local final declarations get marked @nonVirtual.
                        parent !is IrFile && !isStatic && !isLocal -> annotate(dotlinIrBuiltIns.meta.nonVirtual)
                    }
                    else -> {}
                }
            }

            return noChange()
        }
}