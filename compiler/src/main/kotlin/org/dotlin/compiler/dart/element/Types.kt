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

import kotlinx.serialization.Serializable
import org.dotlin.compiler.dart.element.DartNullabilitySuffix.NONE

@Serializable
sealed interface DartType {
    val nullabilitySuffix: DartNullabilitySuffix
}

@Serializable
enum class DartNullabilitySuffix {
    QUESTION_MARK,
    STAR,
    NONE
}

@Serializable
object DartDynamicType : DartType {
    override val nullabilitySuffix = NONE
}

@Serializable
data class DartFunctionType(
    val parameters: List<DartParameterElement> = emptyList(),
    val typeParameters: List<DartTypeParameterElement> = emptyList(),
    val returnType: DartType,
    override val nullabilitySuffix: DartNullabilitySuffix,
) : DartType

@Serializable
object DartNeverType : DartType {
    override val nullabilitySuffix = NONE
}

@Serializable
sealed interface DartTypeWithElement : DartType {
    val elementLocation: DartElementLocation
}

@Serializable
data class DartInterfaceType(
    override val elementLocation: DartElementLocation,
    val typeArguments: List<DartType> = emptyList(),
    val superClass: DartInterfaceType?,
    val superInterfaceTypes: List<DartInterfaceType> = emptyList(),
    val superMixinTypes: List<DartInterfaceType> = emptyList(),
    override val nullabilitySuffix: DartNullabilitySuffix,
) : DartTypeWithElement

@Serializable
data class DartTypeParameterType(
    override val elementLocation: DartElementLocation,
    val bound: DartType,
    override val nullabilitySuffix: DartNullabilitySuffix,
) : DartTypeWithElement


@Serializable
object DartVoidType : DartType {
    override val nullabilitySuffix = NONE
}