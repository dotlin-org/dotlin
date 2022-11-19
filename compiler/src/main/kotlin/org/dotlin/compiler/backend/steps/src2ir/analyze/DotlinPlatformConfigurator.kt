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

package org.dotlin.compiler.backend.steps.src2ir.analyze

import org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.call.KotlinIteratorMethodCallChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.call.SpecialInheritanceConstructorCallChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.declaration.DartNameAnnotationOverrideChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.declaration.KotlinIteratorOperatorMethodChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.declaration.TypeErasureChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.type.CharTypeChecker
import org.dotlin.compiler.backend.steps.src2ir.analyze.checkers.type.FloatTypeChecker
import org.jetbrains.kotlin.builtins.PlatformToKotlinClassMapper
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.PlatformConfiguratorBase
import org.jetbrains.kotlin.types.DynamicTypesAllowed

object DotlinPlatformConfigurator : PlatformConfiguratorBase(
    DynamicTypesAllowed(),
    platformToKotlinClassMapper = PlatformClassMapper,
    additionalDeclarationCheckers = listOf(
        TypeErasureChecker,
        KotlinIteratorOperatorMethodChecker,
        DartNameAnnotationOverrideChecker
    ),
    additionalCallCheckers = listOf(SpecialInheritanceConstructorCallChecker, KotlinIteratorMethodCallChecker),
    additionalTypeCheckers = listOf(FloatTypeChecker, CharTypeChecker)
) {
    override fun configureModuleComponents(container: StorageComponentContainer) {}

    // TODO?: Use PlatformDependentDeclarationFilter for filtering dart:io, dart:html, etc.

    // TODO: Raise warnings if using dart:core Lists, Sets etc. directly
    object PlatformClassMapper : PlatformToKotlinClassMapper {
        override fun mapPlatformClass(classDescriptor: ClassDescriptor): Collection<ClassDescriptor> {
            return mutableListOf()
        }

    }
}