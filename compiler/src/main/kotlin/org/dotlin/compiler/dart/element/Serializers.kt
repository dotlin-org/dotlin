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

package org.dotlin.compiler.dart.element

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier

object DartElementLocationSerializer : KSerializer<DartElementLocation> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "org.dotlin.compiler.dart.element.DartElementReferenceSerializer",
        PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder) = DartElementLocation(decoder.decodeString().split(";"))

    override fun serialize(encoder: Encoder, value: DartElementLocation) {
        encoder.encodeString(value.toString())
    }
}

object DartSimpleIdentifierSerializer : KSerializer<DartSimpleIdentifier> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "org.dotlin.compiler.dart.element.DartSimpleIdentifierSerializer",
        PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder) = DartSimpleIdentifier(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: DartSimpleIdentifier) {
        encoder.encodeString(value.toString())
    }
}