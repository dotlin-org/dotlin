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

@file:UseContextualSerialization(
    DartLibraryElement::class,
    DartCompilationUnitElement::class,
    DartClassElement::class,
    DartPropertyElement::class,
    DartPropertyAccessorElement::class,
    DartConstructorElement::class,
    DartFunctionElement::class,
    DartParameterElement::class,
)

package org.dotlin.compiler.dart.element

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseContextualSerialization
import org.dotlin.compiler.backend.util.PathSerializer
import org.dotlin.compiler.dart.ast.expression.identifier.DartSimpleIdentifier
import java.nio.file.Path

// Everything must be sealed and @Serializable.

@JvmInline
@Serializable(with = DartElementLocationSerializer::class)
value class DartElementLocation(private val parts: List<String>) {
    init {
        require(parts.isNotEmpty())
    }

    val name: String
        get() = parts.last()

    val library: DartElementLocation
        get() = DartElementLocation(parts.subList(0, 1))

    override fun toString() = parts.joinToString(";")
}

// Not a "real" element, just serves as the root of the protobuf structure.
// TODO?: Serialize DartLibraryElements into separate files for performance?
@Serializable
data class DartPackageElement(
    val libraries: List<DartLibraryElement> = emptyList(),
)

@Serializable
sealed interface DartElement {
    val location: DartElementLocation
    val isSynthetic: Boolean
        get() = false
}

@Serializable
sealed interface DartNamedElement : DartElement {
    val name: DartSimpleIdentifier
}

@Serializable
sealed interface DartAbstractableElement : DartElement {
    val isAbstract: Boolean
}

@Serializable
sealed interface DartDeclarationElement : DartNamedElement

@Serializable
data class DartLibraryElement(
    override val location: DartElementLocation,
    @Serializable(with = PathSerializer::class)
    val path: Path,
    val units: List<DartCompilationUnitElement> = emptyList()
) : DartElement

@Serializable
data class DartCompilationUnitElement(
    override val location: DartElementLocation,
    val properties: List<DartPropertyElement> = emptyList(),
    val classes: List<DartClassElement> = emptyList(),
    val functions: List<DartFunctionElement> = emptyList(),
    //val enums: List<DartEnumElement>, TODO
    //val extensions: List<DartExtensionElement>, TODO
    //val mixins: List<DartMixinElement>, TODO
    //val typeAliases: List<DartTypeAliasElement> TODO
) : DartElement

@Serializable
data class DartTypeParameterElement(
    override val name: DartSimpleIdentifier,
    override val location: DartElementLocation,
    val bound: DartType? = null
) : DartNamedElement

@Serializable
sealed interface DartTypeParameterizedElement : DartElement {
    val typeParameters: List<DartTypeParameterElement>
}

// Called "ClassMemberElement" in Dart analyzer package.
@Serializable
sealed interface DartInterfaceMemberElement : DartDeclarationElement, DartAbstractableElement {
    val isStatic: Boolean
}

@Serializable
sealed interface DartInterfaceElement : DartDeclarationElement, DartTypeParameterizedElement {
    override val name: DartSimpleIdentifier

    //val accessors: List<DartPropertyAccessorElement> TODO
    val constructors: List<DartConstructorElement>
    val properties: List<DartPropertyElement>
    //val methods: List<DartMethodElement> TODO

    //val interfaces: List<DartInterfaceType> TODO
    //val mixins: List<DartMixinType>  TODO

}

@Serializable
data class DartClassElement(
    override val location: DartElementLocation,
    override val name: DartSimpleIdentifier,
    override val typeParameters: List<DartTypeParameterElement>,
    override val isAbstract: Boolean,
    override val constructors: List<DartConstructorElement>,
    override val properties: List<DartPropertyElement> = emptyList(),
) : DartInterfaceElement, DartAbstractableElement

@Serializable
sealed interface DartConstableElement : DartElement {
    val isConst: Boolean
}

@Serializable
sealed interface DartVariableElement : DartDeclarationElement, DartConstableElement {
    override val name: DartSimpleIdentifier

    val isFinal: Boolean
    val isLate: Boolean

    val type: DartType
}

@Serializable
data class DartPropertyElement(
    override val location: DartElementLocation,
    override val name: DartSimpleIdentifier,
    override val isAbstract: Boolean,
    val isCovariant: Boolean,
    override val isConst: Boolean,
    override val isFinal: Boolean,
    override val isLate: Boolean,
    override val isStatic: Boolean,
    override val isSynthetic: Boolean,
    override val type: DartType,
    val getter: DartPropertyAccessorElement?,
    val setter: DartPropertyAccessorElement?,
) : DartVariableElement, DartInterfaceMemberElement

@Serializable
data class DartPropertyAccessorElement(
    override val location: DartElementLocation,
    override val name: DartSimpleIdentifier,
    override val type: DartFunctionType,
    override val isAsync: Boolean,
    override val isGenerator: Boolean,
    override val isSynthetic: Boolean,
    override val parameters: List<DartParameterElement>,
    override val typeParameters: List<DartTypeParameterElement>,
    val correspondingPropertyLocation: DartElementLocation,
) : DartExecutableElement

@Serializable
data class DartParameterElement(
    override val location: DartElementLocation,
    /**
     * Can be empty if it represents a parameter of a [DartFunctionType].
     */
    override val name: DartSimpleIdentifier,
    override val type: DartType,
    val isCovariant: Boolean,
    val isNamed: Boolean,
    val isRequired: Boolean,
    /**
     * If this parameter initializes a field (e.g. `this.myField`), this will point to the location of that field.
     */
    val fieldLocation: DartElementLocation? = null,
    /**
     * If this parameter references a super constructor parameter (e.g. `super.myParam`),
     * this will point to the location of that parameter.
     */
    val superConstructorParameterLocation: DartElementLocation? = null,
    /**
     * If the parameter has a default value, this is the Dart code of the default value.
     */
    val defaultValueCode: String? = null,
) : DartVariableElement {
    val isPositional: Boolean
        get() = !isNamed

    val isOptional: Boolean
        get() = !isRequired

    override val isLate: Boolean
        get() = false

    override val isFinal: Boolean
        get() = false

    override val isConst: Boolean
        get() = false

    val hasDefaultValue: Boolean
        get() = defaultValueCode != null
}

@Serializable
sealed interface DartExecutableElement : DartDeclarationElement {
    val type: DartFunctionType
    val isAsync: Boolean
    val isGenerator: Boolean
    val parameters: List<DartParameterElement>
    val typeParameters: List<DartTypeParameterElement>
}

@Serializable
data class DartConstructorElement(
    override val location: DartElementLocation,
    override val name: DartSimpleIdentifier,
    override val isConst: Boolean,
    override val type: DartFunctionType,
    override val parameters: List<DartParameterElement> = emptyList()
) : DartInterfaceMemberElement, DartExecutableElement, DartConstableElement {
    @Transient
    override val isAbstract = false
    @Transient
    override val isStatic = false
    @Transient
    override val isAsync = false
    @Transient
    override val isGenerator = false
    @Transient
    override val typeParameters: List<DartTypeParameterElement> = emptyList()
}

@Serializable
data class DartFunctionElement(
    override val location: DartElementLocation,
    override val name: DartSimpleIdentifier,
    override val isAsync: Boolean,
    override val isGenerator: Boolean,
    override val isAbstract: Boolean,
    override val isStatic: Boolean,
    val isOperator: Boolean,
    override val parameters: List<DartParameterElement> = emptyList(),
    override val typeParameters: List<DartTypeParameterElement> = emptyList(),
    override val type: DartFunctionType
) : DartInterfaceMemberElement, DartExecutableElement