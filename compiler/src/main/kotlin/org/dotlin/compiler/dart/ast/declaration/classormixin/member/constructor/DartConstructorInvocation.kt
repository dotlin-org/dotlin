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

package org.dotlin.compiler.dart.ast.declaration.classormixin.member.constructor

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.expression.DartArgumentList
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier

interface DartConstructorInvocation : DartConstructorInitializer {
    val name: DartSimpleIdentifier?
    val arguments: DartArgumentList
    val keyword: DartConstructorInvocationKeyword

    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, context: C): R =
        visitor.visitConstructorInvocation(this, context)
}

enum class DartConstructorInvocationKeyword(val value: String) {
    SUPER("super"),
    THIS("this"),
}

data class DartSuperConstructorInvocation(
    override val name: DartSimpleIdentifier? = null,
    override val arguments: DartArgumentList = DartArgumentList(),
) : DartConstructorInvocation {
    override val keyword = DartConstructorInvocationKeyword.SUPER
}

data class DartRedirectingConstructorInvocation(
    override val name: DartSimpleIdentifier? = null,
    override val arguments: DartArgumentList = DartArgumentList(),
) : DartConstructorInvocation {
    override val keyword = DartConstructorInvocationKeyword.THIS
}