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

package org.dotlin.compiler.backend.steps.ir2ast.transformer.util

import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.INTERNAL
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PROTECTED
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Modality.FINAL
import org.jetbrains.kotlin.descriptors.Modality.SEALED
import org.jetbrains.kotlin.ir.declarations.*

val IrDeclaration.dartAnnotations: List<DartAnnotation>
    get() {
        val isExtension = isExtension

        val isStatic = when (this) {
            is IrField -> isStatic
            is IrFunction -> isStatic
            else -> false
        }

        val override = when {
            !isExtension && (this is IrOverridableDeclaration<*> && isOverride) || (this is IrField && isOverride) -> {
                DartAnnotation.OVERRIDE
            }
            else -> null
        }

        val visibility = when (this) {
            is IrDeclarationWithVisibility -> when (visibility) {
                INTERNAL -> DartAnnotation.INTERNAL
                else -> when {
                    !isExtension -> when (visibility) {
                        PROTECTED -> DartAnnotation.PROTECTED
                        else -> null
                    }
                    else -> null
                }
            }
            else -> null
        }

        val modality = when {
            !isExtension -> when (modality) {
                SEALED, FINAL -> when {
                    // Sealed as well as final classes get marked @sealed.
                    this is IrClass && !isDartExtensionContainer -> DartAnnotation.SEALED
                    // Non-static, non-top-level final declarations get marked @nonVirtual.
                    parent !is IrFile && !isStatic -> DartAnnotation.NON_VIRTUAL
                    else -> null
                }
                else -> null
            }
            else -> null
        }

        return listOfNotNull(visibility, modality, override)
    }

private val IrDeclaration.modality: Modality?
    get() = when (this) {
        is IrOverridableDeclaration<*> -> modality
        is IrField -> correspondingProperty?.modality
        is IrClass -> modality
        else -> null
    }