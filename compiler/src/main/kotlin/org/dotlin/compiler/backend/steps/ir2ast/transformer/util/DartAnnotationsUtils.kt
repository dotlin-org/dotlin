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

import org.dotlin.compiler.backend.descriptors.annotation.isDartSyntheticPropertyAnnotation
import org.dotlin.compiler.backend.steps.ir2ast.DartAstTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.*
import org.dotlin.compiler.backend.steps.ir2ast.transformer.accept
import org.dotlin.compiler.backend.steps.ir2ast.transformer.acceptArguments
import org.dotlin.compiler.backend.util.annotationsWithRuntimeRetention
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.parentAsClass

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun IrDeclaration.acceptAnnotations(
    context: DartAstTransformContext
): List<DartAnnotation> {
    val isExtension = isExtension

    val override = when {
        !isExtension && (this is IrOverridableDeclaration<*> && isOverride) || (this is IrField && isOverride) -> {
            DartAnnotation.OVERRIDE
        }

        else -> null
    }

    val pragmaInline = when {
        this is IrFunction && isInline -> DartAnnotation.pragma("vm:always-consider-inlining")
        else -> null
    }

    val annotations = annotationsWithRuntimeRetention
        .map {
            val parentClass = it.symbol.owner.parentAsClass
            val isPropertyAnnotation = parentClass.descriptor.isDartSyntheticPropertyAnnotation

            DartAnnotation(
                name = with(context) { parentClass.dartName },
                arguments = when {
                    !isPropertyAnnotation -> it.acceptArguments(context)
                    else -> null
                },
                typeArguments = when {
                    !isPropertyAnnotation -> it.typeArguments.accept(context)
                    else -> null
                }
            )
        }
        .toList()

    return annotations + listOfNotNull(override, pragmaInline)
}