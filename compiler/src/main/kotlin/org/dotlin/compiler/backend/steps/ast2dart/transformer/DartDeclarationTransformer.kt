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

package org.dotlin.compiler.backend.steps.ast2dart.transformer

import org.dotlin.compiler.backend.steps.ast2dart.DartGenerationContext
import org.dotlin.compiler.dart.ast.declaration.variable.DartTopLevelVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList
import org.dotlin.compiler.dart.ast.`typealias`.DartTypeAlias

object DartDeclarationTransformer : DartAstNodeTransformer() {
    override fun DartGenerationContext.visitTopLevelVariableDeclaration(variableDeclaration: DartTopLevelVariableDeclaration) =
        variableDeclaration.run {
            val annotations = acceptChildAnnotations()
            val variables = variableDeclaration.acceptChild(suffix = ";") { variables }

            "$annotations$variables"
        }

    override fun DartGenerationContext.visitVariableDeclaration(variableDeclaration: DartVariableDeclaration) =
        variableDeclaration.run {
            val annotations = acceptChildAnnotations()
            val name = acceptChild { name }
            val expression = acceptChild(prefix = " = ") { expression }

            "$annotations$name$expression"
        }

    override fun DartGenerationContext.visitVariableDeclarationList(variables: DartVariableDeclarationList) =
        variables.run {
            val type = acceptChildOrNull(prefix = " ") { type }
            val prefix = when {
                !isConst && !isFinal && type == null -> "var "
                isConst -> "const$type "
                isFinal -> when {
                    isLate -> "late final$type "
                    else -> "final$type "
                }

                isLate -> "late$type "
                else -> "$type "
            }

            prefix + accept(separator = " ")
        }

    override fun DartGenerationContext.visitTypeAlias(typeAlias: DartTypeAlias) =
        typeAlias.run {
            val name = acceptChild { name }
            val typeParameters = acceptChild { typeParameters }
            val aliased = acceptChild { aliased }

            "typedef $name$typeParameters = $aliased;"
        }
}