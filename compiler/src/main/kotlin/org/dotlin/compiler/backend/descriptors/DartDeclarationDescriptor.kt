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

import org.dotlin.compiler.dart.element.DartDeclarationElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.storage.getValue

interface DartDeclarationDescriptor : DartDescriptor, DeclarationDescriptor {
    override val element: DartDeclarationElement

    val container: DeclarationDescriptor?
    override fun getContainingDeclaration(): DeclarationDescriptor? = container

    // TODO: Create from DartElement
    override val annotations: Annotations
        get() = Annotations.EMPTY

    // TODO: Strip underscores?
    @get:JvmName("_name")
    private val name: Name
        get() = Name.identifier(element.name.value)

    override fun getName() = name
}


abstract class LazyDartDeclarationDescriptor(storageManager: StorageManager) : LazyDartDescriptor(storageManager),
    DartDeclarationDescriptor {
    @get:JvmName("_name")
    private val name by storageManager.createLazyValue { super.getName() }

    override fun getName() = name
}

