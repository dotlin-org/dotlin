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
import org.dotlin.compiler.dart.ast.DartLabel
import org.dotlin.compiler.dart.ast.annotation.DartAnnotation
import org.dotlin.compiler.dart.ast.declaration.classormixin.DartExtendsClause
import org.dotlin.compiler.dart.ast.declaration.classormixin.DartImplementsClause
import org.dotlin.compiler.dart.ast.declaration.classormixin.DartWithClause

object DartMiscTransformer : DartAstNodeTransformer() {
    override fun DartGenerationContext.visitLabel(label: DartLabel) = "${label.acceptChild { value }}: "

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

    override fun DartGenerationContext.visitAnnotation(annotation: DartAnnotation) = annotation.run {
        val name = acceptChild { name }
        val constructorName = acceptChild(prefix = ".") { constructorName }
        val typeArguments = acceptChild { typeArguments }
        val arguments = acceptChild { arguments }

        "@$name$constructorName$typeArguments$arguments"
    }
}
