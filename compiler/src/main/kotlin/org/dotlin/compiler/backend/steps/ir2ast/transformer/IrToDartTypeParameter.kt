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
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrDartDeclarationOrigin
import org.dotlin.compiler.backend.steps.ir2ast.ir.correspondingProperty
import org.dotlin.compiler.backend.steps.ir2ast.ir.isInitializedInBody
import org.dotlin.compiler.backend.steps.ir2ast.ir.isToBeInitializedInFieldInitializerList
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.dartName
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.simpleDartName
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.toDart
import org.dotlin.compiler.dart.ast.parameter.*
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameter
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter

fun IrTypeParameter.accept(context: DartTransformContext): DartTypeParameter =
    DartTypeParameter(
        name = simpleDartName
    )

fun List<IrTypeParameter>.accept(context: DartTransformContext) = DartTypeParameterList(map { it.accept(context) })