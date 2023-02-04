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

package org.dotlin.compiler.dart.ast.expression.identifier

import kotlinx.serialization.Serializable
import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.expression.DartExpression
import org.dotlin.compiler.dart.element.DartSimpleIdentifierSerializer

@Serializable
sealed interface DartIdentifier : DartExpression {
    val value: String

    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitIdentifier(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {}
}

@JvmInline
@Serializable(with = DartSimpleIdentifierSerializer::class)
value class DartSimpleIdentifier(override val value: String) : DartIdentifier {
    fun copy(
        isPrivate: Boolean = this.isPrivate,
        isGenerated: Boolean = this.isGenerated,
        prefix: String = "",
        suffix: String = ""
    ): DartSimpleIdentifier {
        val value = "$prefix$baseValue$suffix"

        return DartSimpleIdentifier(
            when {
                isPrivate -> when {
                    isGenerated -> "_\$$value"
                    else -> "_$value"
                }
                isGenerated -> "\$$value"
                else -> value
            }
        )
    }

    val isPrivate: Boolean
        get() = value.startsWith("_")

    val isGenerated: Boolean
        get() = if (!isPrivate) value.startsWith("$") else value.startsWith("_$")

    /**
     * The [value] of this identifier without private (`_`) or generation (`$`) prefixes.
     *
     * Will remove multiple instances of `_` and `$`.
     */
    val baseValue: String
        get() = value.replace(Regex("^_+"), "").replace(Regex("^\\$+"), "")

    fun asPrivate(): DartSimpleIdentifier = copy(isPrivate = true)

    fun asGenerated(): DartSimpleIdentifier = copy(isGenerated = true)

    fun asGeneratedPrivate(): DartSimpleIdentifier = copy(isPrivate = true, isGenerated = true)

    override fun toString() = value
}

fun String.toDartIdentifier() = DartSimpleIdentifier(this)

data class DartPrefixedIdentifier(
    val prefix: DartSimpleIdentifier,
    val identifier: DartSimpleIdentifier
) : DartIdentifier {
    override val value = "${prefix}.${identifier}"

    override fun toString() = value
}