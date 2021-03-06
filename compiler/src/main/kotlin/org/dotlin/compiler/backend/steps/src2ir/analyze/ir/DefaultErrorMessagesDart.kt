/*
 * Copyright 2022 Wilko Manger
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

package org.dotlin.compiler.backend.steps.src2ir.analyze.ir

import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.Renderers

class DefaultErrorMessagesDart : DefaultErrorMessages.Extension {
    override fun getMap(): DiagnosticFactoryToRendererMap = DIAGNOSTIC_FACTORY_TO_RENDERER
}

private val DIAGNOSTIC_FACTORY_TO_RENDERER by lazy {
    with(DiagnosticFactoryToRendererMap("Dart")) {

        put(
            ErrorsDart.DART_NAME_CLASH,
            "dart name ({0}) generated for this declaration clashes with another declaration: {1}",
            Renderers.STRING,
            Renderers.COMPACT
        )

        put(
            ErrorsDart.EXTENSION_WITHOUT_EXPLICIT_DART_EXTENSION_NAME_IN_PUBLIC_PACKAGE,
            "public extension has no name set with @DartExtensionName even though this package is public",
        )

        put(
            ErrorsDart.WRONG_SET_OPERATOR_RETURN_TYPE,
            "return type of set operator must be the same type as its value parameter: {0}",
            Renderers.RENDER_TYPE
        )

        put(
            ErrorsDart.WRONG_SET_OPERATOR_RETURN,
            "set operator must return its value parameter: {0}",
            Renderers.NAME
        )

        this
    }
}