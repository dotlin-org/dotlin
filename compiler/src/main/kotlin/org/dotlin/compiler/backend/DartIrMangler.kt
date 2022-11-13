/*
 * Copyright 2021-2022 Wilko Manger
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

package org.dotlin.compiler.backend

import org.dotlin.compiler.backend.steps.ir2ast.ir.owner
import org.jetbrains.kotlin.backend.common.serialization.mangle.KotlinExportChecker
import org.jetbrains.kotlin.backend.common.serialization.mangle.KotlinMangleComputer
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrBasedKotlinManglerImpl
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrExportCheckerVisitor
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrMangleComputer
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.render

object DartIrMangler : IrBasedKotlinManglerImpl() {
    override fun getExportChecker(compatibleMode: Boolean): KotlinExportChecker<IrDeclaration> =
        DartIrExportChecker(compatibleMode)

    override fun getMangleComputer(mode: MangleMode, compatibleMode: Boolean): KotlinMangleComputer<IrDeclaration> =
        DartIrManglerComputer(StringBuilder(256), mode, compatibleMode)

    fun IrDeclaration.mangledSignatureHexString(): String = signatureMangle(compatibleMode = false).toHexString()

    fun IrType.mangledHexString(): String = hashedMangle().toHexString()

    private fun IrType.hashedMangle(): Long = when (this) {
        is IrSimpleType -> (arguments.map { it.hashedMangle() } + owner.hashedMangle(compatibleMode = false))
            .joinToString(separator = "").hashMangle

        is IrDynamicType -> "dynamic".hashMangle
        else -> error("Unexpected type argument: ${render()}")
    }

    private fun IrTypeArgument.hashedMangle(): Long = when (this) {
        is IrTypeProjection -> type.hashedMangle()
        else -> "*".hashMangle
    }

    private fun Long.toHexString(): String = toString(radix = 16).replace('-', 'm')

    private class DartIrExportChecker(compatibleMode: Boolean) : IrExportCheckerVisitor(compatibleMode) {
        override fun IrDeclaration.isPlatformSpecificExported() = false
    }

    private class DartIrManglerComputer(
        builder: StringBuilder,
        mode: MangleMode,
        compatibleMode: Boolean
    ) : IrMangleComputer(builder, mode, compatibleMode) {
        override fun copy(newMode: MangleMode): IrMangleComputer =
            DartIrManglerComputer(builder, newMode, compatibleMode)
    }
}