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

package org.dotlin.compiler.backend.steps.ir2ast.transformer.util

import org.dotlin.compiler.backend.steps.falseIfNull
import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.isOverride
import org.dotlin.compiler.backend.steps.ir2ast.transformer.accept
import org.dotlin.compiler.dart.ast.DartAstNode
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.parameter.DartFormalParameterList
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

fun <N : DartAstNode> IrFunction.transformBy(
    context: DartTransformContext,
    block: DartFunctionDeclarationDefaults.() -> N
): N {
    val isOverride = (this as? IrSimpleFunction)?.isOverride.falseIfNull()

    return block(
        DartFunctionDeclarationDefaults(
            name = dartNameOrNull,
            returnType = returnType.toDart(context),
            parameters = valueParameters.accept(context),
            annotations = if (isOverride) listOf(DartAnnotation.OVERRIDE) else listOf(),
        )
    )
}

data class DartFunctionDeclarationDefaults(
    val name: DartSimpleIdentifier?,
    val returnType: DartTypeAnnotation,
    val parameters: DartFormalParameterList,
    val annotations: List<DartAnnotation> = listOf(),
    val documentationComment: String? = null,
)