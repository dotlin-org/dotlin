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

import org.dotlin.compiler.backend.DartPackage
import org.dotlin.compiler.backend.steps.src2ir.DartElementLocator
import org.dotlin.compiler.dart.element.DartElement
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.storage.StorageManager

interface DartDescriptor {
    val element: DartElement
    val context: DartDescriptorContext

    val module: ModuleDescriptor
        get() = context.module

    val pkg: DartPackage
        get() = context.pkg

    val elementLocator: DartElementLocator
        get() = context.elementLocator

    val storageManager: StorageManager
        get() = context.storageManager
}