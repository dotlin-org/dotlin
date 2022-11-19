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
import org.dotlin.compiler.dart.ast.declaration.classlike.*

object DartClassLikeTransformer : DartAstNodeTransformer() {
    override fun DartGenerationContext.visitClassLikeDeclaration(classLikeDeclaration: DartClassLikeDeclaration) =
        classLikeDeclaration.run {
            val annotations = acceptChildAnnotations()
            val name = acceptChild { name }
            val typeParameters = acceptChild { typeParameters }

            val keyword = when (this) {
                is DartClassDeclaration -> "class"
                is DartEnumDeclaration -> "enum"
                is DartExtensionDeclaration -> "extension"
            }

            val extends = acceptChildIf<DartClassDeclaration>(prefix = " ") { extendsClause }
            val implements = acceptChild(prefix = " ") { implementsClause }
            val with = acceptChildIf<DartClassDeclaration>(prefix = " ") { withClause }
            val on = when (this) {
                is DartExtensionDeclaration -> " on ${acceptChild { extendedType }}"
                else -> ""
            }

            val members = acceptChild(separator = "") { members }

            val enumConstants = acceptChildIf<DartEnumDeclaration>(
                separator = ", ",
                suffix = ";",
                ifEmpty = ""
            ) { constants }

            val abstract = if (this is DartClassDeclaration && isAbstract) "abstract " else ""
            "$annotations${abstract}$keyword $name$typeParameters$extends$with$implements$on{$enumConstants$members}"
        }

    override fun DartGenerationContext.visitExtendsClause(extendsClause: DartExtendsClause) = extendsClause.run {
        val type = acceptChild { type }
        "extends $type"
    }

    override fun DartGenerationContext.visitImplementsClause(implementsClause: DartImplementsClause) =
        implementsClause.run {
            val types = acceptChild(separator = ", ") { interfaces }
            "implements $types"
        }

    override fun DartGenerationContext.visitWithClause(withClause: DartWithClause) = withClause.run {
        val types = acceptChild(separator = ", ") { mixins }
        "with $types"
    }
}