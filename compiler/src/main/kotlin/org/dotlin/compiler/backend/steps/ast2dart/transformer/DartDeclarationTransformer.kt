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
import org.dotlin.compiler.dart.ast.declaration.DartDeclaration
import org.dotlin.compiler.dart.ast.declaration.classormixin.DartClassDeclaration
import org.dotlin.compiler.dart.ast.declaration.extension.DartExtensionDeclaration
import org.dotlin.compiler.dart.ast.declaration.function.DartTopLevelFunctionDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclaration
import org.dotlin.compiler.dart.ast.declaration.variable.DartVariableDeclarationList

object DartDeclarationTransformer : DartAstNodeTransformer {
    override fun visitTopLevelFunctionDeclaration(
        functionDeclaration: DartTopLevelFunctionDeclaration,
        context: DartGenerationContext,
    ): String {
        val name = functionDeclaration.name.accept(context)
        val returnType = functionDeclaration.returnType.accept(context)
        val function = functionDeclaration.function.accept(context)

        return "$returnType $name$function"
    }

    override fun visitClassDeclaration(classDeclaration: DartClassDeclaration, context: DartGenerationContext): String {
        val abstract = if (classDeclaration.isAbstract) "abstract " else ""
        val name = classDeclaration.name.accept(context)

        val members =
            if (classDeclaration.members.isNotEmpty())
                classDeclaration.members.accept(context).joinToString("", prefix = " {", postfix = "}")
            else
                " {}"

        val extends =
            if (classDeclaration.extendsClause != null)
                " ${classDeclaration.extendsClause.accept(context)}"
            else
                ""

        val implements =
            if (classDeclaration.implementsClause != null)
                " ${classDeclaration.implementsClause.accept(context)}"
            else
                ""

        return "${abstract}class $name$extends$implements$members"
    }

    override fun visitExtensionDeclaration(
        extensionDeclaration: DartExtensionDeclaration,
        context: DartGenerationContext
    ) = extensionDeclaration.let {
        val name = if (it.name != null) it.name.accept(context) else ""
        val typeParameters = it.typeParameters.accept(context)
        val type = it.extendedType.accept(context)

        val members = it.members.accept(context).joinToString("", prefix = "{", postfix = "}")

        "extension $name$typeParameters on $type$members"
    }

    override fun visitVariableDeclaration(
        variableDeclaration: DartVariableDeclaration,
        context: DartGenerationContext,
    ) = variableDeclaration.let {
        val name = it.name.accept(context)
        val expression = if (it.expression != null) " = " + it.expression.accept(context) else ""

        "$name$expression"
    }

    override fun visitVariableDeclarationList(
        variables: DartVariableDeclarationList,
        context: DartGenerationContext,
    ) = variables.let {
        val type = if (it.type != null) " " + it.type.accept(context) else ""
        val prefix = when {
            !it.isConst && !it.isFinal && it.type == null -> "var "
            it.isConst -> "const$type "
            it.isFinal -> when {
                it.isLate -> "late final$type "
                else -> "final$type "
            }
            it.isLate -> "late$type "
            it.type == null -> "var "
            else -> "$type "
        }

        prefix + it.joinToString { variable -> variable.accept(context) }
    }
}

fun DartDeclaration.accept(context: DartGenerationContext) = accept(DartDeclarationTransformer, context)
fun DartVariableDeclarationList.accept(context: DartGenerationContext) = accept(DartDeclarationTransformer, context)