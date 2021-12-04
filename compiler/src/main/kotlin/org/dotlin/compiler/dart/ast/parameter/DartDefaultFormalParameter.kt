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

package org.dotlin.compiler.dart.ast.parameter

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.expression.DartExpression
import org.dotlin.compiler.dart.ast.expression.identifier.DartIdentifier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

data class DartDefaultFormalParameter(
    val parameter: DartNormalFormalParameter,
    val defaultValue: DartExpression? = null,
    val isNamed: Boolean = false,
) : DartFormalParameter {
    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, context: C): R =
        visitor.visitDefaultFormalParameter(this, context)
}

@OptIn(ExperimentalContracts::class)
fun DartFormalParameter.isDefault(): Boolean {
    contract {
        returns(true) implies (this@isDefault is DartDefaultFormalParameter)
    }

    return this is DartDefaultFormalParameter
}

val DartFormalParameter.identifier: DartIdentifier
    get() = when (this) {
        is DartNormalFormalParameter -> identifier!!
        is DartDefaultFormalParameter -> parameter.identifier!!
        else -> throw UnsupportedOperationException()
    }