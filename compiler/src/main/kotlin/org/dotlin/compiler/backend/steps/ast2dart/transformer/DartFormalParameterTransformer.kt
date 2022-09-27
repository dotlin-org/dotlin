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
import org.dotlin.compiler.dart.ast.parameter.*

object DartFormalParameterTransformer : DartAstNodeTransformer() {
    override fun DartGenerationContext.visitFormalParameterList(parameters: DartFormalParameterList) = parameters.run {
        var output = "("

        val (defaultBlockOpen, defaultBlockClose) = when {
            parameters.any { it.isDefault() && it.isNamed } -> '{' to '}'
            else -> '[' to ']'
        }
        var startedWithDefaultParameters = false
        parameters.forEachIndexed { index, param ->
            if (!startedWithDefaultParameters && param.isDefault()) {
                startedWithDefaultParameters = true
                output += defaultBlockOpen
            }

            output += acceptChild { param }

            // If we have more than 2 parameters, we add a trailing comma for formatting.
            if (parameters.size >= 2) {
                output += ", "
            }
        }

        if (startedWithDefaultParameters) {
            output += defaultBlockClose
        }

        output += ")"

        output
    }

    override fun DartGenerationContext.visitDefaultFormalParameter(defaultParameter: DartDefaultFormalParameter) =
        defaultParameter.run {
            val defaultValueAssignment = acceptChild(prefix = " = ") { defaultValue }

            val parameter = acceptChild { parameter }

            "$parameter$defaultValueAssignment"
        }

    override fun DartGenerationContext.visitSimpleFormalParameter(parameter: DartSimpleFormalParameter) =
        parameter.run {
            val type = acceptChild { type }
            val identifier = acceptChild { identifier }

            "$type $identifier"
        }

    override fun DartGenerationContext.visitFieldFormalParameter(parameter: DartFieldFormalParameter) = parameter.run {
        val identifier = acceptChild { identifier }

        "this.$identifier"
    }
}