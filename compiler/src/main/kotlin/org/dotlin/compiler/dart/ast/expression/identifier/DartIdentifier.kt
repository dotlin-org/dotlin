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

package org.dotlin.compiler.dart.ast.expression.identifier

import org.dotlin.compiler.dart.ast.DartAstNodeVisitor
import org.dotlin.compiler.dart.ast.expression.DartExpression
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed interface DartIdentifier : DartExpression {
    val value: String

    override fun <R, C> accept(visitor: DartAstNodeVisitor<R, C>, data: C): R =
        visitor.visitIdentifier(this, data)

    override fun <D> acceptChildren(visitor: DartAstNodeVisitor<Nothing?, D>, data: D) {}
}

@JvmInline
value class DartSimpleIdentifier(override val value: String) : DartIdentifier {
    constructor(value: String, isPrivate: Boolean, isGenerated: Boolean = false) :
            this(
                when {
                    isPrivate -> when {
                        isGenerated -> "_\$$value"
                        else -> "_$value"
                    }
                    isGenerated -> "\$$value"
                    else -> value
                }
            )

    init {
        require(value.isNotEmpty())
    }

    val isPrivate: Boolean
        get() = value.startsWith("_")

    val isGenerated: Boolean
        get() = if (!isPrivate) value.startsWith("$") else value.startsWith("_$")

    /**
     * The [value] of this identifier without private (`_`) or generation (`$`) prefixes.
     */
    val baseValue: String
        get() = value.removePrefix("_").removePrefix("$")

    fun asPrivate(): DartSimpleIdentifier =
        DartSimpleIdentifier(baseValue, isPrivate = true, isGenerated = isGenerated)

    fun asGenerated(): DartSimpleIdentifier =
        DartSimpleIdentifier(baseValue, isPrivate = isPrivate, isGenerated = true)

    fun asGeneratedPrivate(): DartSimpleIdentifier =
        DartSimpleIdentifier(baseValue, isPrivate = true, isGenerated = true)

    override fun toString() = value
}

fun String.toDartSimpleIdentifier(): DartSimpleIdentifier = DartSimpleIdentifier(this)

data class DartPrefixedIdentifier(
    val prefix: DartSimpleIdentifier,
    val identifier: DartSimpleIdentifier
) : DartIdentifier {

    override val value = "${prefix}.${identifier}"

    override fun toString() = value
}


@OptIn(ExperimentalContracts::class)
fun DartIdentifier.isSimple(): Boolean {
    contract {
        returns(true) implies (this@isSimple is DartSimpleIdentifier)
    }

    return this is DartSimpleIdentifier
}

@OptIn(ExperimentalContracts::class)
fun DartIdentifier.isPrefixed(): Boolean {
    contract {
        returns(true) implies (this@isPrefixed is DartPrefixedIdentifier)
    }

    return this is DartPrefixedIdentifier
}
