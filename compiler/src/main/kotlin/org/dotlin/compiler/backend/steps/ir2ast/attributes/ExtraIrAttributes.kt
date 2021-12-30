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

package org.dotlin.compiler.backend.steps.ir2ast.attributes

import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrTry

/**
 * All [IrElement]s referenced must be the `attributeOwnerId`s.
 */
interface ExtraIrAttributes {
    companion object {
        val DEFAULT = object : ExtraIrAttributes {
            override val propertiesInitializedInConstructorBody = mutableSetOf<IrProperty>()
            override val propertiesInitializedInFieldInitializerList = mutableSetOf<IrProperty>()
            override val parameterPropertyReferencesInParameterDefaultValue = mutableSetOf<IrGetValue>()
        }
    }

    /**
     * A field initializer cannot reference `this` in Dart, and thus must be initialized in the
     * primary constructor body and be marked `late`.
     */
    val propertiesInitializedInConstructorBody: MutableSet<IrProperty>

    val IrProperty.isInitializedInConstructorBody: Boolean
        get() = attributeOwner() in propertiesInitializedInConstructorBody

    val propertiesInitializedInFieldInitializerList: MutableSet<IrProperty>

    val IrProperty.isInitializedInFieldInitializerList: Boolean
        get() = attributeOwner() in propertiesInitializedInFieldInitializerList

    /**
     * Whether the property is initialized somewhere else, e.g. in the constructor body or field initializer list.
     */
    val IrProperty.isInitializedSomewhereElse: Boolean
        get() = isInitializedInConstructorBody || isInitializedInFieldInitializerList

    /**
     * In complex parameters' default values, all `IrGetValue` are remapped to `IrGetField`s, because in the Dart
     * constructor we want to refer to the relevant properties of those references parameters (if there is one), not the
     * parameter itself (since the value of the parameter might be outdated, because of how Dart syntax works).
     */
    val parameterPropertyReferencesInParameterDefaultValue: MutableSet<IrGetValue>
}

/**
 * Assumes that [attributeOwnerId] is the same type as `this`.
 */
inline fun <reified T : IrAttributeContainer> T.attributeOwner(): T = attributeOwnerId as T