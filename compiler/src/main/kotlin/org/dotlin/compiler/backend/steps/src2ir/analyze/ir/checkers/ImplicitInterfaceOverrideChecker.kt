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

package org.dotlin.compiler.backend.steps.src2ir.analyze.ir.checkers

import org.dotlin.compiler.backend.steps.ir2ast.ir.isFakeOverride
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrAnalyzerContext
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.IrDeclarationChecker
import org.jetbrains.kotlin.backend.common.ir.isMethodOfAny
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.dotlin.compiler.backend.steps.ir2ast.ir.resolveRootOverride
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration

object ImplicitInterfaceOverrideChecker : IrDeclarationChecker {
    override val reports = listOf(Errors.ABSTRACT_MEMBER_NOT_IMPLEMENTED)

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun IrAnalyzerContext.check(source: KtDeclaration, declaration: IrDeclaration) {
        if (declaration !is IrClass || declaration.isInterface) return

        source as KtClassOrObject

        val implicitInterfaces = declaration.allSuperImplicitInterfaces()
        if (implicitInterfaces.isEmpty()) return

        for (member in declaration.declarations) {
            if (!member.isFakeOverride()) continue
            if (member is IrFunction && member.isMethodOfAny()) continue

            val overridableMember = member as? IrOverridableDeclaration<*> ?: continue

            val rootOverride = overridableMember.resolveRootOverride()

            val isFakeOverrideOfImplicitInterface =
                when (val rootOverrideParentType = rootOverride?.parentClassOrNull?.defaultType) {
                    null -> false
                    else -> rootOverrideParentType in implicitInterfaces
                }

            if (isFakeOverrideOfImplicitInterface) {
                trace.report(
                    Errors.ABSTRACT_MEMBER_NOT_IMPLEMENTED.on(
                        source,
                        source,
                        rootOverride!!.descriptor as CallableMemberDescriptor
                    )
                )
            }
        }
    }
}