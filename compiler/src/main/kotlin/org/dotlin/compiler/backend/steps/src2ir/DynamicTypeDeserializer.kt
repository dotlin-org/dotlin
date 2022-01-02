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

package org.dotlin.compiler.backend.steps.src2ir

import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.serialization.deserialization.FlexibleTypeDeserializer
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.checker.StrictEqualityTypeChecker
import org.jetbrains.kotlin.types.createDynamicType
import org.jetbrains.kotlin.types.typeUtil.builtIns

object DynamicTypeDeserializer : FlexibleTypeDeserializer {
    override fun create(
        proto: ProtoBuf.Type,
        flexibleId: String,
        lowerBound: SimpleType,
        upperBound: SimpleType
    ): KotlinType {
        require(flexibleId == "kotlin.DynamicType") { "Invalid id: $flexibleId" }

        infix fun SimpleType.strictlyEquals(other: SimpleType) = StrictEqualityTypeChecker.strictEqualTypes(this, other)

        return lowerBound.builtIns.run {
            when {
                lowerBound strictlyEquals nothingType && upperBound strictlyEquals nullableAnyType -> {
                    createDynamicType(this)
                }
                else -> error("Invalid type range for dynamic: $lowerBound..$upperBound")
            }
        }
    }

}