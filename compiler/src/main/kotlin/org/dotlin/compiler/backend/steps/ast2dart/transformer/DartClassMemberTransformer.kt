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
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartConstructorDeclaration
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartFieldDeclaration

object DartClassMemberTransformer : DartAstNodeTransformer() {
    override fun DartGenerationContext.visitConstructorDeclaration(constructorDeclaration: DartConstructorDeclaration) =
        constructorDeclaration.run {
            val annotations = acceptChildAnnotations()
            val keyword = when {
                isConst -> "const "
                isFactory -> "factory "
                else -> ""
            }

            val type = acceptChild { returnType }
            val name = acceptChildOrNull { name }
            val constructorName = if (name != null) "$type.$name" else type

            val parameters = acceptChild { function.parameters }
            val body = acceptChild { function.body }

            val initializers = acceptChild(separator = ", ", prefix = " : ", ifEmpty = "") { initializers }

            "$annotations$keyword $constructorName$parameters$initializers$body"

    }

    override fun DartGenerationContext.visitFieldDeclaration(fieldDeclaration: DartFieldDeclaration) =
        fieldDeclaration.run {
            val annotations = acceptChildAnnotations()

            val keywords = when {
                isStatic -> "static "
                isAbstract -> when {
                    isCovariant -> "abstract covariant "
                    else -> "abstract "
                }
                isCovariant -> "covariant "
                else -> ""
            }

            val fields = acceptChild { fields }

            "$annotations$keywords$fields;"
        }
}