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

package org.dotlin.compiler.backend.descriptors.export

import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.descriptors.DartDescriptorContext
import org.dotlin.compiler.backend.descriptors.DartPackageFragmentDescriptor
import org.dotlin.compiler.dart.element.DartLibraryExportElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.PackageFragmentDescriptorImpl
import org.jetbrains.kotlin.storage.getValue

class DartExportPackageFragmentDescriptor(
    override val element: DartLibraryExportElement,
    override val context: DartDescriptorContext,
    val fragment: DartPackageFragmentDescriptor,
    private val exportedFragment: DartPackageFragmentDescriptor,
) : PackageFragmentDescriptorImpl(
    context.module,
    fragment.fqName,
), DartDescriptor {
    override val annotations: Annotations = Annotations.EMPTY

    private val _memberScope by context.storageManager.createLazyValue {
        DartExportScope(owner = this, context, exportedFragment.getMemberScope())
    }

    override fun getMemberScope(): DartExportScope = _memberScope
}