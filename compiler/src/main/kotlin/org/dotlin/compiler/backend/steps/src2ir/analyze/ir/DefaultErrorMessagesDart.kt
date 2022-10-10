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
    DiagnosticFactoryToRendererMap("Dart").run {

        put(
            ErrorsDart.DART_NAME_CLASH,
            "Dart name ''{0}'' generated for this declaration clashes with another declaration: {1}",
            Renderers.TO_STRING,
            Renderers.TO_STRING
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
            Renderers.TO_STRING
        )

        put(
            ErrorsDart.SPECIAL_INHERITANCE_CONSTRUCTOR_MISUSE,
            "special inheritance constructor can only be used as super type constructor"
        )

        put(
            ErrorsDart.CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE,
            "const variables must be initialized with a constant value"
        )

        put(
            ErrorsDart.ONLY_CONSTRUCTOR_CALLS_CAN_BE_CONST,
            "only constructor call expressions can be const"
        )

        put(
            ErrorsDart.LONG_REFERENCE,
            "cannot use Long, use Int instead"
        )

        put(
            ErrorsDart.IMPLICIT_LONG_REFERENCE,
            "{0} has implicit type of Long, specify Int type explicitly",
            Renderers.TO_STRING
        )

        put(
            ErrorsDart.FLOAT_REFERENCE,
            "cannot use Float, use Double instead"
        )

        put(
            ErrorsDart.CHAR_REFERENCE,
            "cannot use Char, use String instead"
        )

        put(
            ErrorsDart.UNNECESSARY_REIFIED,
            "using reified is not necessary, there's no type erasure"
        )

        put(
            ErrorsDart.CONST_LAMBDA_ACCESSING_NON_GLOBAL_VALUE,
            "const lambdas cannot access values from local or class closure"
        )

        put(
            ErrorsDart.KOTLIN_ITERATOR_METHOD_USAGE,
            "use 'moveNext' and 'current'"
        )

        put(
            ErrorsDart.DART_NAME_ON_OVERRIDE,
            "cannot use @DartName on overridden member"
        )

        put(
            ErrorsDart.DART_INDEX_OUT_OF_BOUNDS,
            "index out of bounds: must be in range of 0..{0}",
            Renderers.TO_STRING
        )

        put(
            ErrorsDart.DART_INDEX_CONFLICT,
            "index is equal to that of another @DartIndex",
        )

        // Dart emulated errors.

        put(
            ErrorsDart.CONST_WITH_NON_CONST,
            "the constructor being called isn't a const constructor"
        )

        put(
            ErrorsDart.NON_CONSTANT_DEFAULT_VALUE_IN_CONST_CONSTRUCTOR,
            "the default value of an optional parameter in a const constructor must be constant"
        )

        this
    }
}