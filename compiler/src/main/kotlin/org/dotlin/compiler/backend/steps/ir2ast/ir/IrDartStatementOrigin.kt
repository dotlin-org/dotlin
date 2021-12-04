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

package org.dotlin.compiler.backend.steps.ir2ast.ir

import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

@Suppress("ClassName")
sealed class IrDartStatementOrigin(private val name: String) : IrStatementOrigin {
    /**
     * Dart does not support parameters with complex (non-const) default values.
     *
     * The parameter will be made `dynamic`, and a
     * const constructor call of the type `_$DefaultValueMarker` will be the actual default type on the Dart side.
     * The complex default value will be assigned on the first statement (if the actual default value
     * is identical to `_$DefaultValue`).
     */
    object COMPLEX_PARAM_INIT_DEFAULT_VALUE : IrDartStatementOrigin("COMPLEX_PARAM_INIT_DEFAULT_VALUE")

    object COMPLEX_PARAM_INIT_NULLABLE : IrDartStatementOrigin("COMPLEX_PARAM_INIT_NULLABLE")

    object COMPLEX_PROPERTY_INITIALIZED_IN_BODY
        : IrDartStatementOrigin("COMPLEX_PARAM_PROPERTY_INITIALIZED_IN_BODY")

    class COMPLEX_PARAM_PROPERTY_TO_BE_INITIALIZED_IN_FIELD_INITIALIZER_LIST(val originalDefaultValue: IrExpressionBody) :
        IrDartStatementOrigin("COMPLEX_PARAM_PROPERTY_TO_BE_INITIALIZED_IN_FIELD_INITIALIZER_LIST")

    class COMPLEX_PARAM_PROPERTY_REFERENCE_REMAPPED(val originalParameter: IrValueParameter) :
        IrDartStatementOrigin("COMPLEX_PARAM_PROPERTY_REFERENCE_REMAPPED")

    /**
     * A field initializer cannot reference `this` in Dart, and thus must be initialized in the
     * body and be marked `late`.
     */
    object PROPERTY_REFERENCING_THIS_INITIALIZED_IN_BODY :
        IrDartStatementOrigin("PROPERTY_REFERENCING_THIS_INITIALIZED_IN_BODY")

    /**
     * Constructors for objects are always const.
     */
    object OBJECT_CONSTRUCTOR : IrDartStatementOrigin("OBJECT_CONSTRUCTOR")

    /**
     * A block for a when is meant as a Dart statement, not a Dart expression.
     */
    object WHEN_STATEMENT : IrDartStatementOrigin("WHEN_STATEMENT")

    override fun toString() = name
}
