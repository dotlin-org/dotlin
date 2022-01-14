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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.isDartPositional
import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.correspondingProperty
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.isDartFactory
import org.dotlin.compiler.dart.ast.parameter.*
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrValueParameter

fun IrValueParameter.accept(context: DartTransformContext): DartFormalParameter = context.run {
    val irValueParameter = this@accept

    val correspondingIrProperty = irValueParameter.correspondingProperty
    val isInFactoryConstructor = (irValueParameter.parent as? IrConstructor)?.isDartFactory == true

    val isFieldInitializer = correspondingIrProperty != null

    val identifier = irValueParameter.dartName
    val type = irValueParameter.type.accept(context)
    val defaultValue = irValueParameter.defaultValue?.accept(context)

    val normalParameter = if (
        isFieldInitializer &&
        !correspondingIrProperty!!.isInitializedInConstructorBody &&
        !correspondingIrProperty.isInitializedInFieldInitializerList &&
        !isInFactoryConstructor
    ) {
        val fieldName = correspondingIrProperty.simpleDartName
        require(defaultValue == null || identifier == fieldName)
        DartFieldFormalParameter(
            identifier = fieldName,
            type = type,
        )
    } else {
        DartSimpleFormalParameter(
            identifier = identifier,
            type = type,
        )
    }

    if (defaultValue == null) {
        return normalParameter
    }

    return DartDefaultFormalParameter(
        // By default, parameters with default values will be named in Dart. This is overridden if
        // the containing function is annotated with @DartPositional.
        isNamed = !irValueParameter.isDartPositional,
        defaultValue = defaultValue,
        parameter = normalParameter,
    )
}

fun List<IrValueParameter>.accept(context: DartTransformContext) = DartFormalParameterList(map { it.accept(context) })
