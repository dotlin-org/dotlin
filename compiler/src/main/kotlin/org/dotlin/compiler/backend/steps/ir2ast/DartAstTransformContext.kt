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
import org.dotlin.compiler.backend.attributes.IrAttributes
import org.dotlin.compiler.backend.steps.ir2ast.lower.DotlinLoweringContext
import org.dotlin.compiler.backend.steps.ir2ast.transformer.accept
import org.dotlin.compiler.backend.steps.ir2ast.transformer.util.acceptAnnotations
import org.dotlin.compiler.backend.util.isDartGetter
import org.dotlin.compiler.backend.util.isDartSetter
import org.dotlin.compiler.dart.ast.DartAstNode
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.expression.DartFunctionExpression
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.parameter.DartFormalParameterList
import org.dotlin.compiler.dart.ast.type.DartTypeAnnotation
import org.dotlin.compiler.dart.ast.type.parameter.DartTypeParameterList
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction

class DartAstTransformContext(
    loweringContext: DotlinLoweringContext,
) : IrContext(), IrAttributes by loweringContext {
    override val bindingContext = loweringContext.bindingContext
    override val symbolTable = loweringContext.symbolTable
    val dotlinIrBuiltIns = loweringContext.dotlinIrBuiltIns
    override val irBuiltIns = loweringContext.irBuiltIns
    override val dartNameGenerator = loweringContext.dartNameGenerator
    override val dartProject = loweringContext.dartProject

    private fun <N : DartAstNode> IrFunction.transformBy(
        context: DartAstTransformContext,
        isNamed: Boolean,
        isLocal: Boolean,
        block: DartFunctionDeclarationDefaults.() -> N
    ): N {
        val name = simpleDartNameOrNull
        val returnType = returnType.accept(context)
        val typeParameters = typeParameters.accept(context)
        val parameters = valueParameters.accept(context)
        val annotations = acceptAnnotations(context)

        return block(
            when {
                isNamed -> DartFunctionDeclarationDefaults.Named(
                    name!!,
                    returnType,
                    function = DartFunctionExpression(
                        typeParameters,
                        parameters,
                        body = body.accept(context)
                    ),
                    annotations,
                    documentationComment = null,
                    isGetter = !isLocal && isDartGetter(),
                    isSetter = !isLocal && isDartSetter(),
                )
                else -> DartFunctionDeclarationDefaults.PossiblyNamed(
                    name,
                    returnType,
                    parameters,
                    typeParameters,
                    annotations,
                    documentationComment = null
                )
            }
        )
    }

    fun <N : DartAstNode> IrSimpleFunction.transformBy(
        context: DartAstTransformContext,
        isLocal: Boolean = false,
        block: DartFunctionDeclarationDefaults.Named.() -> N
    ): N = transformBy(context, isNamed = true, isLocal) { block(this as DartFunctionDeclarationDefaults.Named) }

    fun <N : DartAstNode> IrConstructor.transformBy(
        context: DartAstTransformContext,
        block: DartFunctionDeclarationDefaults.PossiblyNamed.() -> N
    ): N = transformBy(
        context,
        isNamed = false,
        isLocal = false
    ) { block(this as DartFunctionDeclarationDefaults.PossiblyNamed) }
}

sealed interface DartFunctionDeclarationDefaults {
    val name: DartSimpleIdentifier?
    val returnType: DartTypeAnnotation
    val annotations: List<DartAnnotation>
    val documentationComment: String?

    data class Named(
        override val name: DartSimpleIdentifier,
        override val returnType: DartTypeAnnotation,
        val function: DartFunctionExpression,
        override val annotations: List<DartAnnotation>,
        override val documentationComment: String?,
        val isGetter: Boolean,
        val isSetter: Boolean,
    ) : DartFunctionDeclarationDefaults

    data class PossiblyNamed(
        override val name: DartSimpleIdentifier?,
        override val returnType: DartTypeAnnotation,
        val parameters: DartFormalParameterList,
        val typeParameters: DartTypeParameterList,
        override val annotations: List<DartAnnotation>,
        override val documentationComment: String?
    ) : DartFunctionDeclarationDefaults
}