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

import org.dotlin.compiler.backend.descriptors.type.toKotlinType
import org.dotlin.compiler.dart.element.DartParameterElement
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.resolve.constants.*
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.typeUtil.isBoolean
import org.jetbrains.kotlin.types.typeUtil.isDouble
import org.jetbrains.kotlin.types.typeUtil.isInt

class DartValueParameterDescriptor private constructor(
    container: FunctionDescriptor,
    override val index: Int,
    override val annotations: Annotations,
    override val element: DartParameterElement,
    override val context: DartDescriptorContext,
    delegate: ValueParameterDescriptorImpl,
) : ValueParameterDescriptor by delegate, DartDescriptor {
    constructor(
        container: FunctionDescriptor,
        index: Int,
        annotations: Annotations,
        element: DartParameterElement,
        context: DartDescriptorContext,
    ) : this(
        container, index, annotations, element, context,
        // We have to use delegate instead of inherit, because `ValueParameterDescriptorImpl` overrides
        // `getCompileTimeInitializer` with  a return type of `Nothing?`, making it impossible for a useful override.
        ValueParameterDescriptorImpl(
            container,
            null,
            index,
            annotations,
            element.kotlinName,
            element.type.toKotlinType(context),
            element.hasDefaultValue,
            isCrossinline = false,
            isNoinline = false,
            varargElementType = null,
            SourceElement.NO_SOURCE, // TODO: SourceElement
        )
    )

    // Dart parameters are vars.
    override fun isVar(): Boolean = true

    private val parsedDefaultValue by storageManager.createNullableLazyValue parse@{
        val dartCode = element.defaultValueCode ?: return@parse null
        val type = this.type

        val parsed = when {
            type.isNullable() && dartCode == "null" -> NullValue()
            type.isBoolean() -> when (dartCode) {
                "true" -> true
                "false" -> false
                else -> null
            }?.let { BooleanValue(it) }

            type.isInt() -> IntValue(dartCode.toInt())
            type.isDouble() -> DoubleValue(dartCode.toDouble())
            // Drop quotes.
            type.isString() -> {
                val quotes = listOf('"', '\'')

                when {
                    // Only if the Dart code is quoted is it a String literal, otherwise it's an identifier.
                    dartCode.first() in quotes && dartCode.last() in quotes -> StringValue(dartCode.drop(1).dropLast(1))
                    else -> null
                }
            }
            else -> null
        }

        parsed ?: DartCodeValue(dartCode, type)
    }

    override fun getCompileTimeInitializer(): ConstantValue<*>? = parsedDefaultValue
}

private fun KotlinType.isString() = KotlinBuiltIns.isString(this)