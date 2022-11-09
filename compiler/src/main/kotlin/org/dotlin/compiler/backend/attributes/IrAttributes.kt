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

package org.dotlin.compiler.backend.attributes

import org.dotlin.compiler.backend.util.getFqName
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrAttributeContainer
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.psiUtil.getAnnotationEntries
import org.jetbrains.kotlin.resolve.BindingContext
import kotlin.collections.set
import kotlin.reflect.KProperty

private typealias IrAttributeMap = MutableMap<IrElement, MutableMap<String, Any?>>

interface IrAttributes  {
    class Default : IrAttributes {
        private val attributes: IrAttributeMap = mutableMapOf()
        private fun <T> attribute() = IrAttribute<T>(attributes)
        private fun <T> attribute(default: T) = IrAttributeWithDefault(attributes, default)
        private fun <T> attribute(create: () -> T) = IrAttributeWithCreatedDefault(attributes, create)

        override var IrProperty.isInitializedInConstructorBody by attribute(default = false)
        override var IrProperty.isInitializedInFieldInitializerList by attribute(default = false)

        override var IrGetField.correspondingConstructorParameterReference by attribute<IrGetValue?>(default = null)

        override val IrFile.dartImports by attribute { mutableSetOf<DartImport>() }

        override var IrExpression.ktExpression by attribute<KtExpression?>(default = null)

        override var IrExpression.isParenthesized by attribute(default = false)

        override var IrTypeOperatorCall.isFunctionTypeCheck by attribute(default = false)

        override var IrVararg.literalKind by attribute(default = CollectionLiteralKind.LIST)
    }

    /**
     * A field initializer cannot reference `this` in Dart, and thus must be initialized in the
     * primary constructor body and be marked `late`.
     */
    var IrProperty.isInitializedInConstructorBody: Boolean
    var IrProperty.isInitializedInFieldInitializerList: Boolean

    /**
     * Whether the property is initialized somewhere else, e.g. in the constructor body or field initializer list.
     */
    val IrProperty.isInitializedSomewhereElse: Boolean
        get() = isInitializedInConstructorBody || isInitializedInFieldInitializerList

    /**
     * In complex parameters' default values, all `IrGetValue` are remapped to `IrGetField`s, because in the Dart
     * constructor we want to refer to the relevant properties of those referenced parameters (if there is one), not the
     * parameter itself (since the value of the parameter might be outdated, because of how Dart syntax works).
     */
    var IrGetField.correspondingConstructorParameterReference: IrGetValue?

    val IrFile.dartImports: MutableSet<DartImport>

    var IrExpression.ktExpression: KtExpression?

    fun IrExpression.hasAnnotation(fqName: FqName, bindingContext: BindingContext): Boolean {
        val ktExpression = ktExpression ?: return false

        val annotated = when (val parent = ktExpression.parent) {
            is KtDotQualifiedExpression -> parent
            else -> ktExpression
        }

        return annotated.getAnnotationEntries().any { it.getFqName(bindingContext) == fqName }
    }

    var IrExpression.isParenthesized: Boolean
    fun IrExpression.parenthesize(): IrExpression = apply {
        isParenthesized = true
    }

    var IrTypeOperatorCall.isFunctionTypeCheck: Boolean

    var IrVararg.literalKind: CollectionLiteralKind
}

@Suppress("UNCHECKED_CAST")
private open class IrAttribute<T>(
    private val attributes: IrAttributeMap
) {
    open operator fun getValue(thisRef: IrElement, property: KProperty<*>): T? {
        return attributes[thisRef.attributeOwner]?.get(property.name) as? T?
    }

    open operator fun setValue(thisRef: IrElement, property: KProperty<*>, value: T) {
        attributes.computeIfAbsent(thisRef.attributeOwner) { mutableMapOf() }[property.name] = value
    }

    private val IrElement.attributeOwner: IrElement
        get() = when (this) {
            is IrAttributeContainer -> attributeOwnerId
            else -> this
        }
}

private class IrAttributeWithDefault<T>(
    attributes: IrAttributeMap,
    private val default: T
) : IrAttribute<T>(attributes) {
    override operator fun getValue(thisRef: IrElement, property: KProperty<*>): T =
        super.getValue(thisRef, property) ?: default
}

private class IrAttributeWithCreatedDefault<T>(
    attributes: IrAttributeMap,
    private val create: () -> T
) : IrAttribute<T>(attributes) {
    override operator fun getValue(thisRef: IrElement, property: KProperty<*>): T {
        var value = super.getValue(thisRef, property)

        if (value != null) return value

        value = create()

        setValue(thisRef, property, value)

        return value
    }
}

data class DartImport(
    val library: String,
    val alias: String? = null,
    val hide: String? = null,
    val show: String? = null
)

enum class CollectionLiteralKind {
    LIST,
    SET,
    MAP
}