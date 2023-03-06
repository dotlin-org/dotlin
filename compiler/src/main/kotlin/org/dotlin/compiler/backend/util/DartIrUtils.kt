/*
 * Copyright 2023 Wilko Manger
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

@file:OptIn(ObsoleteDescriptorBasedAPI::class)

package org.dotlin.compiler.backend.util

import org.dotlin.compiler.backend.descriptors.DartDescriptor
import org.dotlin.compiler.backend.descriptors.dartElementAs
import org.dotlin.compiler.dart.element.DartDeclarationElement
import org.dotlin.compiler.dart.element.DartElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclaration

val IrDeclaration.isDartDeclaration
    get() = descriptor is DartDescriptor

val IrDeclaration.dartElement: DartDeclarationElement?
    get() = descriptor.dartElementAs()

fun <E : DartElement> IrDeclaration.dartElementAs(): E? = descriptor.dartElementAs()