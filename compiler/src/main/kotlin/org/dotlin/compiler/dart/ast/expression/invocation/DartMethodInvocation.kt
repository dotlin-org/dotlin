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

package org.dotlin.compiler.dart.ast.expression.invocation

import org.dotlin.compiler.dart.ast.expression.DartArgumentList
import org.dotlin.compiler.dart.ast.expression.DartExpression
import org.dotlin.compiler.dart.ast.expression.DartPossiblyNullAwareExpression
import org.dotlin.compiler.dart.ast.expression.DartPropertyAccessExpression
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import org.dotlin.compiler.dart.ast.type.DartTypeArgumentList

data class DartMethodInvocation(
    val target: DartExpression,
    val methodName: DartSimpleIdentifier,
    override val arguments: DartArgumentList = DartArgumentList(),
    override val typeArguments: DartTypeArgumentList = DartTypeArgumentList(),
    override val isNullAware: Boolean = false,
) : DartInvocationExpression, DartPossiblyNullAwareExpression {
    override val function = DartPropertyAccessExpression(
        target = target,
        propertyName = methodName,
        isNullAware = isNullAware,
    )

    override fun asNullAware(): DartMethodInvocation = copy(isNullAware = true)

}