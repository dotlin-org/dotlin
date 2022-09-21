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

package org.dotlin.compiler.backend.steps.ir2ast

import org.dotlin.compiler.backend.IrContext
import org.dotlin.compiler.backend.steps.ir2ast.attributes.IrAttributes
import org.dotlin.compiler.backend.steps.ir2ast.lower.DartLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.transformer.accept
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.dartAnnotations
import org.dotlin.compiler.dart.ast.DartAstNode
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.parameter.DartFormalParameterList
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList
import org.jetbrains.kotlin.ir.declarations.IrFunction

class DartTransformContext(
    loweringContext: DartLoweringContext,
) : IrContext(), IrAttributes by loweringContext {
    override val bindingContext = loweringContext.bindingContext
    override val symbolTable = loweringContext.symbolTable
    override val irBuiltIns = loweringContext.irBuiltIns
    override val dartNameGenerator = loweringContext.dartNameGenerator
    override val sourceRoot = loweringContext.sourceRoot
    override val dartPackage = loweringContext.dartPackage

    fun <N : DartAstNode> IrFunction.transformBy(
        context: DartTransformContext,
        block: DartFunctionDeclarationDefaults.() -> N
    ): N {
        return block(
            DartFunctionDeclarationDefaults(
                name = simpleDartNameOrNull,
                returnType = returnType.accept(context),
                typeParameters = typeParameters.accept(context),
                parameters = valueParameters.accept(context),
                annotations = dartAnnotations
            )
        )
    }
}

data class DartFunctionDeclarationDefaults(
    val name: DartSimpleIdentifier?,
    val returnType: DartTypeAnnotation,
    val parameters: DartFormalParameterList,
    val typeParameters: DartTypeParameterList,
    val annotations: List<DartAnnotation> = listOf(),
    val documentationComment: String? = null,
)
