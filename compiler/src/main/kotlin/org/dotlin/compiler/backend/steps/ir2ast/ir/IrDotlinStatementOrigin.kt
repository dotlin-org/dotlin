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

package org.dotlin.compiler.backend.steps.ir2ast.ir

import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin

@Suppress("ClassName")
sealed class IrDotlinStatementOrigin(private val name: String) : IrStatementOrigin {
    /**
     * Dart does not support parameters with complex (non-const) default values.
     *
     * The parameter will be made `dynamic`, and a
     * const constructor call of the type `_$DefaultValue` will be the actual default type on the Dart side.
     * The complex default value will be assigned on the first statement (if the actual default value
     * is identical to `_$DefaultValue`).
     */
    object COMPLEX_PARAM_INIT_DEFAULT_VALUE : IrDotlinStatementOrigin("COMPLEX_PARAM_INIT_DEFAULT_VALUE")

    object COMPLEX_PARAM_INIT_NULLABLE : IrDotlinStatementOrigin("COMPLEX_PARAM_INIT_NULLABLE")

    /**
     * The call with this origin was created to call the original operator method for a synthetic operator, e.g.
     * `[]` redirecting to `get`.
     */
    object OPERATOR_REDIRECT : IrDotlinStatementOrigin("OPERATOR_REDIRECT")

    /**
     * The constructor of the relevant extension container is called, with the original receiver as the single argument,
     * to prevent extension conflicts in Dart.
     */
    object EXTENSION_CONSTRUCTOR_CALL : IrDotlinStatementOrigin("EXTENSION_CONSTRUCTOR")

    /**
     * Related to [PropertyReferenceLowering].
     */
    object PROPERTY_REFERENCE : IrDotlinStatementOrigin("PROPERTY_REFERENCE")

    object IF_NULL : IrDotlinStatementOrigin("IF_NULL")

    override fun toString() = name
}
