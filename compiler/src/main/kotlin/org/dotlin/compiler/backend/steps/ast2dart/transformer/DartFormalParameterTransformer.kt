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
import org.dotlin.compiler.dart.ast.parameter.*

object DartFormalParameterTransformer : DartAstNodeTransformer {
    override fun visitFormalParameterList(parameters: DartFormalParameterList, context: DartGenerationContext): String {
        var output = "("

        var startedWithDefaultParameters = false
        parameters.forEachIndexed { index, param ->
            if (!startedWithDefaultParameters && param.isDefault()) {
                startedWithDefaultParameters = true
                output += "{"
            }

            output += param.accept(context)

            // If we have more than 2 parameters, we add a trailing comma for formatting.
            if (parameters.size >= 2) {
                output += ", "
            }
        }

        if (startedWithDefaultParameters) {
            output += "}"
        }

        output += ")"

        return output
    }

    override fun visitDefaultFormalParameter(
        defaultParameter: DartDefaultFormalParameter,
        context: DartGenerationContext,
    ): String {
        val defaultValue = defaultParameter.defaultValue?.accept(context)

        val parameter = defaultParameter.parameter.accept(context)
        val defaultValueAssignment = when {
            defaultValue != null -> " = $defaultValue"
            else -> ""
        }

        return "$parameter$defaultValueAssignment"
    }

    override fun visitSimpleFormalParameter(
        parameter: DartSimpleFormalParameter,
        context: DartGenerationContext,
    ): String {
        val type = parameter.type.accept(context)

        // TODO: Handle null identifier
        val identifier = parameter.identifier?.accept(context)

        return "$type $identifier"
    }

    override fun visitFieldFormalParameter(
        parameter: DartFieldFormalParameter,
        context: DartGenerationContext
    ): String {
        // TODO: Handle null identifier
        val identifier = parameter.identifier?.accept(context)

        return "this.$identifier"
    }
}

fun DartFormalParameter.accept(context: DartGenerationContext) = accept(DartFormalParameterTransformer, context)
fun DartFormalParameterList.accept(context: DartGenerationContext) = accept(DartFormalParameterTransformer, context)