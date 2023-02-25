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

import org.dotlin.compiler.dart.element.DartCompilationUnitElement
import org.dotlin.compiler.dart.element.DartLibraryElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.storage.getValue

/**
 * In Dart terms, a package fragment is a library file. A library file in most cases represents
 * a full Dart library, unless `part` and `part of` is used.
 */
class DartPackageFragmentDescriptor(
    val library: DartLibraryElement,
    override val element: DartCompilationUnitElement,
    override val context: DartDescriptorContext,
    override val annotations: Annotations = Annotations.EMPTY,
) : PackageFragmentDescriptorImpl(context.module, context.fqNameOf(library)), DartDescriptor {
    private val _memberScope by storageManager.createLazyValue {
        DartMemberScope(owner = this, context, element.classes + element.functions + element.properties)
    }

    override fun getMemberScope(): DartMemberScope = _memberScope
}