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

package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.backend.descriptors.type.toKotlinType
import org.dotlin.compiler.dart.element.DartTypeParameterElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.SupertypeLoopChecker
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.AbstractTypeParameterDescriptor
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.Variance

class DartTypeParameterDescriptor(
    override val element: DartTypeParameterElement,
    override val context: DartDescriptorContext,
    container: DeclarationDescriptor,
    index: Int,
) : AbstractTypeParameterDescriptor(
    context.storageManager,
    container,
    Annotations.EMPTY, // TODO
    element.kotlinName,
    Variance.INVARIANT,
    false,
    index,
    SourceElement.NO_SOURCE, // TODO: SourceElement
    SupertypeLoopChecker.EMPTY,
), DartDescriptor {
    override fun reportSupertypeLoopError(type: KotlinType) {
        TODO("Not yet implemented")
    }

    private val bound by storageManager.createLazyValue {
        listOf(element.bound?.toKotlinType() ?: context.module.builtIns.defaultBound)
    }

    override fun resolveUpperBounds(): List<KotlinType> = bound

}