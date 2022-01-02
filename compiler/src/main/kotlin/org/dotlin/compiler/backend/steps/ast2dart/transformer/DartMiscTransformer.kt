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

object DartMiscTransformer : DartAstNodeTransformer {
    override fun visitLabel(label: DartLabel, context: DartGenerationContext) = "${label.value.accept(context)}: "

    override fun visitExtendsClause(extendsClause: DartExtendsClause, context: DartGenerationContext): String {
        val type = extendsClause.type.accept(context)
        return "extends $type"
    }

    override fun visitImplementsClause(implementsClause: DartImplementsClause, context: DartGenerationContext): String {
        val types = implementsClause.interfaces.joinToString { it.accept(context) }
        return "implements $types"
    }

    override fun visitAnnotation(annotation: DartAnnotation, context: DartGenerationContext) = annotation.let {
        val name = it.name.accept(context)
        val constructorName = if (it.constructorName != null) "." + it.constructorName.accept(context) else ""
        val typeArguments = if (!it.typeArguments.isNullOrEmpty()) it.typeArguments.accept(context) else ""
        val arguments = if (!it.arguments.isNullOrEmpty()) it.arguments.accept(context) else ""

        "@$name$constructorName$typeArguments$arguments"
    }
}

fun DartLabel.accept(context: DartGenerationContext) = accept(DartMiscTransformer, context)
fun DartExtendsClause.accept(context: DartGenerationContext) = accept(DartMiscTransformer, context)
fun DartImplementsClause.accept(context: DartGenerationContext) = accept(DartMiscTransformer, context)
fun DartAnnotation.accept(context: DartGenerationContext) = accept(DartMiscTransformer, context)
fun Collection<DartAnnotation>.accept(context: DartGenerationContext) = when {
    isEmpty() -> ""
    else -> joinToString(separator = "", postfix = " ") { it.accept(context) }
}
