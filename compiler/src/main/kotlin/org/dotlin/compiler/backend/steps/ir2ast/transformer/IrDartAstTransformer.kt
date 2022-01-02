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

package org.dotlin.compiler.backend.steps.ir2ast.transformer

import org.dotlin.compiler.backend.steps.ir2ast.DartTransformContext
import org.dotlin.compiler.backend.steps.ir2ast.ir.IrCustomElementVisitor
import org.dotlin.compiler.backend.steps.ir2ast.ir.todo
import org.dotlin.compiler.dart.ast.DartAstNode
import org.jetbrains.kotlin.ir.IrElement

interface IrDartAstTransformer<N : DartAstNode?> : IrCustomElementVisitor<N, DartTransformContext> {
    override fun visitElement(element: IrElement, data: DartTransformContext): N {
        todo(element)
    }
}