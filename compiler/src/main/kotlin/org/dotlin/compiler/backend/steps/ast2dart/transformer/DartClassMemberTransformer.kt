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
import org.dotlin.compiler.backend.steps.ast2dart.DartGenerationContext.Flag.GETTER
import org.dotlin.compiler.dart.ast.accept
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.DartClassMember
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.DartMethodDeclaration
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartConstructorDeclaration
import org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor.DartFieldDeclaration

object DartClassMemberTransformer : DartAstNodeTransformer {
    override fun visitMethodDeclaration(
        methodDeclaration: DartMethodDeclaration,
        context: DartGenerationContext,
    ) = methodDeclaration.let {
        val annotations = it.annotations.accept(context)
        val name = it.name.accept(context)
        val returnType = it.returnType.accept(context)
        val function: String

        val getOrSet =
            when {
                it.isGetter -> {
                    function = context.withFlag(GETTER) {
                        it.function.accept(context)
                    }
                    "get "
                }
                else -> {
                    function = it.function.accept(context)
                    when {
                        it.isSetter -> "set "
                        else -> ""
                    }
                }
            }

        val operator = if (it.isOperator) "operator " else ""
        val static = if (it.isStatic) "static " else ""

        "$annotations$static$returnType $operator$getOrSet$name$function"
    }

    override fun visitConstructorDeclaration(
        constructorDeclaration: DartConstructorDeclaration,
        context: DartGenerationContext,
    ) = constructorDeclaration.let {
        val annotations = it.annotations.accept(context)
        val keyword = when {
            it.isConst -> "const "
            it.isFactory -> "factory "
            else -> ""
        }

        val type = it.returnType.accept(context)
        val name = it.name?.accept(context)
        val constructorName = if (name != null) "$type.$name" else type

        val parameters = it.function.parameters.accept(context)
        val body = it.function.body.accept(context)

        val initializers = when {
            it.initializers.isNotEmpty() -> " : " + it.initializers.joinToString { init -> init.accept(context) }
            else -> ""
        }

        "$annotations$keyword $constructorName$parameters$initializers$body"

    }

    override fun visitFieldDeclaration(fieldDeclaration: DartFieldDeclaration, context: DartGenerationContext) =
        fieldDeclaration.let {
            val annotations = it.annotations.accept(context)

            val keywords = when {
                it.isStatic -> "static "
                it.isAbstract -> when {
                    it.isCovariant -> "abstract covariant "
                    else -> "abstract "
                }
                it.isCovariant -> "covariant "
                else -> ""
            }

            val fields = it.fields.accept(context)

            "$annotations$keywords$fields;"
        }
}

fun DartClassMember.accept(context: DartGenerationContext) = accept(DartClassMemberTransformer, context)
fun Collection<DartClassMember>.accept(context: DartGenerationContext) = accept(DartClassMemberTransformer, context)