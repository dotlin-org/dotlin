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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.accept
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.simpleDartName
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameter
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.types.isNullableAny

fun IrTypeParameter.accept(context: DartTransformContext): DartTypeParameter =
    DartTypeParameter(
        name = simpleDartName,
        bound = superTypes.single().let {
            // We don't care if the super type is Any?, that's the default.
            when {
                !it.isNullableAny() -> it.accept(context)
                else -> null
            }
        }
    )

fun List<IrTypeParameter>.accept(context: DartTransformContext) = DartTypeParameterList(map { it.accept(context) })