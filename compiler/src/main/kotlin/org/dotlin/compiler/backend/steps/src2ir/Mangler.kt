/*
 * Copyright 2021 Wilko Manger
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

import org.jetbrains.kotlin.backend.common.serialization.mangle.KotlinExportChecker
import org.jetbrains.kotlin.backend.common.serialization.mangle.KotlinMangleComputer
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.backend.common.serialization.mangle.descriptor.DescriptorBasedKotlinManglerImpl
import org.jetbrains.kotlin.backend.common.serialization.mangle.descriptor.DescriptorExportCheckerVisitor
import org.jetbrains.kotlin.backend.common.serialization.mangle.descriptor.DescriptorMangleComputer
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

object DartDescriptorBasedMangler : DescriptorBasedKotlinManglerImpl() {
    private object DartExportChecker : DescriptorExportCheckerVisitor() {
        override fun DeclarationDescriptor.isPlatformSpecificExported() = false
    }

    override fun getExportChecker(compatibleMode: Boolean): KotlinExportChecker<DeclarationDescriptor> =
        DartExportChecker

    private class DartDescriptorMangleComputer(
        builder: StringBuilder, mode: MangleMode,
    ) : DescriptorMangleComputer(builder, mode) {
        override fun copy(newMode: MangleMode) = DartDescriptorMangleComputer(builder, newMode)
    }

    override fun getMangleComputer(
        mode: MangleMode,
        compatibleMode: Boolean
    ): KotlinMangleComputer<DeclarationDescriptor> = DartDescriptorMangleComputer(StringBuilder(256), mode)
}

