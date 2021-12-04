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

package org.dotlin.compiler.backend.steps.ast2dart.transformer

import org.dotlin.compiler.backend.steps.ast2dart.DartGenerationContext
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartConstructorFieldInitializer
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartConstructorInitializer
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartConstructorInvocation

object DartConstructorInitializerTransformer : DartAstNodeTransformer {
    override fun visitConstructorInvocation(
        invocation: DartConstructorInvocation,
        context: DartGenerationContext,
    ): String {
        val keyword = invocation.keyword.value

        val name = if (invocation.name != null)
            "." + invocation.name!!.accept(context)
        else
            ""

        val arguments = invocation.arguments.accept(context)

        return "$keyword$name$arguments"
    }

    override fun visitConstructorFieldInitializer(
        initializer: DartConstructorFieldInitializer,
        context: DartGenerationContext,
    ): String {
        val field = initializer.fieldName.accept(context)
        val value = initializer.expression.accept(context)

        return "$field = $value"
    }
}

fun DartConstructorInitializer.accept(context: DartGenerationContext) =
    accept(DartConstructorInitializerTransformer, context)