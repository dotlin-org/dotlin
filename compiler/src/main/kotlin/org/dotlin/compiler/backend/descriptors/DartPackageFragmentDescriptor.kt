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
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorVisitor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
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
    private val original: DartPackageFragmentDescriptor? = null,
) : DartDescriptor, PackageFragmentDescriptor {

    private val _memberScope by storageManager.createLazyValue {
        DartMemberScope(owner = this, context, element.classes + element.functions)
    }

    override val fqName: FqName = context.fqNameOf(library)

    override fun getMemberScope(): DartMemberScope = _memberScope

    override fun getName(): Name = fqName.shortNameOrSpecial()

    override fun getOriginal(): DartPackageFragmentDescriptor = original ?: this

    override fun getContainingDeclaration(): ModuleDescriptor = module

    override fun getSource(): SourceElement {
        return SourceElement.NO_SOURCE // TODO
    }

    override fun <R : Any?, D : Any?> accept(visitor: DeclarationDescriptorVisitor<R, D>, data: D): R =
        visitor.visitPackageFragmentDescriptor(this, data)

    override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>) {
        visitor.visitPackageFragmentDescriptor(this, null)
    }
}