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

package org.dotlin.compiler.backend.descriptors.type

import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.SimpleType
import org.jetbrains.kotlin.types.TypeAttributes
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.TypeRefinement
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner

class DartSimpleType(
    override val constructor: DartInterfaceTypeConstructor,
    override val arguments: List<TypeProjection> = emptyList(),
    override val attributes: TypeAttributes = TypeAttributes.Empty,
    override val isMarkedNullable: Boolean = false, // TODO: Default should be based on interfaceType
) : SimpleType() {
    override val memberScope: MemberScope by lazy { constructor.descriptor.unsubstitutedMemberScope }

    override fun makeNullableAsSpecified(newNullability: Boolean): SimpleType =
        DartSimpleType(constructor, arguments, attributes, newNullability)

    @TypeRefinement
    override fun refine(kotlinTypeRefiner: KotlinTypeRefiner): SimpleType = kotlinTypeRefiner.refineType(this) as SimpleType

    override fun replaceAttributes(newAttributes: TypeAttributes): SimpleType =
        DartSimpleType(constructor, arguments, newAttributes, isMarkedNullable)
}
