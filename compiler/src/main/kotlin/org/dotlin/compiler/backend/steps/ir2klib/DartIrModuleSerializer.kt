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

package org.dotlin.compiler.backend.steps.ir2klib

import org.dotlin.compiler.backend.DotlinIrMangler
import org.jetbrains.kotlin.backend.common.serialization.*
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.backend.js.lower.serialization.ir.JsUniqIdClashTracker
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.IrMessageLogger

class DartIrModuleSerializer(
    messageLogger: IrMessageLogger,
    builtIns: IrBuiltIns,
) : IrModuleSerializer<IrFileSerializer>(
    messageLogger,
    CompatibilityMode.CURRENT,
    normalizeAbsolutePaths = false,
    sourceBaseDirs = emptyList()
) {

    private val declarationTable = DeclarationTable(DartGlobalDeclarationTable(builtIns))

    override fun createSerializerForFile(file: IrFile): IrFileSerializer =
        IrFileSerializer(
            messageLogger,
            declarationTable,
            expectDescriptorToSymbol = mutableMapOf(),
            skipExpects = true,
            compatibilityMode = compatibilityMode,
            sourceBaseDirs = emptyList()
        )
}

class DartGlobalDeclarationTable(builtIns: IrBuiltIns) :
    GlobalDeclarationTable(DotlinIrMangler, JsUniqIdClashTracker() /* TODO: JS reference */) {
    init {
        loadKnownBuiltins(builtIns)
    }
}