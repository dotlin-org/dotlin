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

package org.dotlin.compiler.backend.util

import org.dotlin.compiler.backend.dotlin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter

val IrValueParameter.dartIndex: Int
    get() {
        val explicitIndex = getOverriddenAnnotationArgumentOf<Int>(dotlin.DartIndex)

        // We assume the explicit index is correct.
        if (explicitIndex != null) return explicitIndex

        val parent = parent as? IrFunction ?: return explicitIndex ?: index
        val params = parent.valueParameters

        // If there are no @DartIndex annotations on any of the parameters, we can just return the normal index.
        if (params.none { it.hasOverriddenAnnotation(dotlin.DartIndex) }) return index

        // If a param has an explicit index that's our original index, we swap with them.
        val paramWithOurIndex = params.firstOrNull {
            it.getOverriddenAnnotationArgumentOf<Int>(dotlin.DartIndex) == index
        }

        return paramWithOurIndex?.index ?: index
    }

fun IrValueParameter.hasDifferentDefaultValueInDart(): Boolean =
    hasOverriddenAnnotation(dotlin.DartDifferentDefaultValue)