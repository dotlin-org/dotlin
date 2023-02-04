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

package org.dotlin.compiler.backend.steps.src2ir

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual
import org.dotlin.compiler.dart.element.DartElement
import org.dotlin.compiler.dart.element.DartElementLocation

/**
 * Maps [DartElementLocation]s to [DartElement]s.
 */
class DartElementLocator {
    private val map = mutableMapOf<DartElementLocation, DartElement>()

    @Suppress("UNCHECKED_CAST")
    fun <E : DartElement> locate(location: DartElementLocation) = map[location] as E

    fun register(element: DartElement) {
        map[element.location] = element
    }
}

class RegisteringDartElementSerializer<E : DartElement>(
    private val delegate: KSerializer<E>,
    private val locator: DartElementLocator,
) : KSerializer<E> by delegate {

    override fun deserialize(decoder: Decoder): E = delegate.deserialize(decoder).also {
        locator.register(it)
    }

    override fun serialize(encoder: Encoder, value: E) = delegate.serialize(encoder, value)
}

context(SerializersModuleBuilder)
inline fun <reified E : DartElement> KSerializer<E>.registerOnSerialize(locator: DartElementLocator) =
    contextual(RegisteringDartElementSerializer(this, locator))