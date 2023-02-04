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

package org.dotlin.compiler.backend.descriptors.type

import org.dotlin.compiler.backend.descriptors.DartClassDescriptor
import org.dotlin.compiler.backend.steps.src2ir.DotlinModule
import org.dotlin.compiler.dart.element.DartInterfaceElement
import org.dotlin.compiler.dart.element.DartInterfaceType
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.packageFragments
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeConstructor
import org.jetbrains.kotlin.types.TypeRefinement
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner

class DartInterfaceTypeConstructor(
    val descriptor: DartClassDescriptor,
    private val builtIns: KotlinBuiltIns
) : TypeConstructor {
    constructor(element: DartInterfaceElement, module: DotlinModule) : this(
        descriptor = module.fqNameOf(element).let { fqName ->
            module.impl
                .packageFragmentProvider
                .packageFragments(fqName.parent())
                .firstNotNullOf {
                    it.getMemberScope()
                        .getContributedClassifier(fqName.shortName(), NoLookupLocation.FROM_BACKEND)
                            as? DartClassDescriptor
                }
        },
        module.builtIns
    )

    constructor(type: DartInterfaceType, module: DotlinModule) : this(
        element = module.dartElementLocator.locate(type.elementLocation),
        module
    )

    override fun getParameters(): List<TypeParameterDescriptor> {
        return emptyList() // TODO
    }

    override fun getSupertypes(): Collection<KotlinType> {
        return listOf(builtIns.anyType) // TODO
    }

    override fun isFinal(): Boolean = false // TODO: has @sealed

    override fun isDenotable(): Boolean = true // TODO: False for records? (Dart 3.0)

    override fun getDeclarationDescriptor(): ClassifierDescriptor = descriptor

    override fun getBuiltIns(): KotlinBuiltIns = builtIns

    @TypeRefinement
    override fun refine(kotlinTypeRefiner: KotlinTypeRefiner): TypeConstructor {
        return this // TODO
    }
}