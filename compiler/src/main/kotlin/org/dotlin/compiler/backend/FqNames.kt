@file:Suppress("ClassName")

package org.dotlin.compiler.backend

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

sealed interface PackageFqNameWrapper {
    val self: FqName
}

object dotlin : PackageFqNameWrapper {
    override val self = FqName("dotlin")

    val dart = self.child("dart")

    val typeOf = self.child("typeOf")

    // Annotations
    val const = self.child("const")
    val DartName = self.child("DartName")
    val DartPositional = self.child("DartPositional")
    val DartStatic = self.child("DartStatic")
    val DartExtensionName = self.child("DartExtensionName")
    val DartIndex = self.child("DartIndex")
    val DartDifferentDefaultValue = self.child("DartDifferentDefaultValue")
    val DartConstructor = self.child("DartConstructor")

    // Internal annotations.
    val DartLibrary = self.child("DartLibrary")
    val DartGetter = self.child("DartGetter")
    val DartExtension = self.child("DartExtension")

    object intrinsics : PackageFqNameWrapper {
        override val self = dotlin.self.child("intrinsics")

        val AnyList = self.child("AnyList")
        val AnySet = self.child("AnySet")
        val AnyMap = self.child("AnyMap")

        // Collections
        val AnyCollection = self.child("AnyCollection")

        val isCollection = self.child("isCollection")
        val isMutableCollection = self.child("isMutableCollection")

        val isImmutableList = self.child("isImmutableList")
        val isWriteableList = self.child("isWriteableList")
        val isFixedSizeList = self.child("isFixedSizeList")
        val isMutableList = self.child("isMutableList")

        val isImmutableSet = self.child("isImmutableSet")
        val isMutableSet = self.child("isMutableSet")

        val isImmutableMap = self.child("isImmutableMap")
        val isMutableMap = self.child("isMutableMap")

        val ImmutableListMarker = self.child("ImmutableListMarker")
        val WriteableListMarker = self.child("WriteableListMarker")
        val MutableListMarker = self.child("MutableListMarker")
        val FixedSizeListMarker = self.child("FixedSizeListMarker")

        val ImmutableSetMarker = self.child("ImmutableSetMarker")
        val MutableSetMarker = self.child("MutableSetMarker")

        val ImmutableMapMarker = self.child("ImmutableMapMarker")
        val MutableMapMarker = self.child("MutableMapMarker")

        val `$Return` = self.child("\$Return")
        val DotlinExternal = self.child("DotlinExternal")
        val SpecialInheritedType = self.child("SpecialInheritedType")

        val NoWhenBranchMatchedError = self.child("NoWhenBranchMatchedError")

        val Dynamic = self.child("Dynamic")

        // This package only exists while compiling.
        object operators : PackageFqNameWrapper {
            override val self = dotlin.intrinsics.self.child("operators")
        }
    }

    object reflect : PackageFqNameWrapper {
        override val self = dotlin.self.child("reflect")

        val KProperty0Impl = self.child("KProperty0Impl")
        val KMutableProperty0Impl = self.child("KMutableProperty0Impl")
        val KProperty1Impl = self.child("KProperty1Impl")
        val KMutableProperty1Impl = self.child("KMutableProperty1Impl")
        val KProperty2Impl = self.child("KProperty2Impl")
        val KMutableProperty2Impl = self.child("KMutableProperty2Impl")
    }
}

object dart : PackageFqNameWrapper {
    override val self = FqName("dart")

    object core : PackageFqNameWrapper {
        override val self = dart.self.child("core")

        val Iterator = self.child("Iterator")
        val Iterable = self.child("Iterable")
        val List = self.child("List")
        val Set = self.child("Set")
        val Map = self.child("Map")
        val MapEntry = self.child("MapEntry")

        val identical = self.child("identical")
        val UnsupportedError = self.child("UnsupportedError")
    }

    object collection : PackageFqNameWrapper {
        override val self = dart.self.child("collection")

        val ImmutableListView = self.child("ImmutableListView")
        val ImmutableSetView = self.child("ImmutableSetView")
    }
}

object kotlin : PackageFqNameWrapper {
    override val self = FqName("kotlin")

    val Lazy = self.child("Lazy")

    val Array = self.child("Array")

    val arrayOf = self.child("arrayOf")
    val arrayOfNulls = self.child("arrayOfNulls")
    val emptyArray = self.child("emptyArray")

    val Enum = self.child("Enum")

    object collections : PackageFqNameWrapper {
        override val self = kotlin.self.child("collections")

        val Iterator = self.child("Iterator")

        val Collection = self.child("Collection")
        val MutableCollection = self.child("MutableCollection")

        val List = self.child("List")
        val ImmutableList = self.child("ImmutableList")
        val WriteableList = self.child("WriteableList")
        val MutableList = self.child("MutableList")

        val Set = self.child("Set")
        val ImmutableSet = self.child("ImmutableSet")
        val MutableSet = self.child("MutableSet")

        val Map = self.child("Map")
        val ImmutableMap = self.child("ImmutableMap")
        val MutableMap = self.child("MutableMap")

        val listOf = self.child("listOf")
        val emptyList = self.child("emptyList")

        val mutableListOf = self.child("mutableListOf")
        val mutableListOfNulls = self.child("mutableListOfNulls")

        val mapOf = self.child("mapOf")
        val mutableMapOf = self.child("mutableMapOf")
        val emptyMap = self.child("emptyMap")

        val setOf = self.child("setOf")
        val mutableSetOf = self.child("mutableSetOf")
        val emptySet = self.child("emptySet")
    }

    object ranges : PackageFqNameWrapper {
        override val self = kotlin.self.child("ranges")

        val step = self.child("step")
        val until = self.child("until")
        val downTo = self.child("downTo")
    }
}

object dev : PackageFqNameWrapper {
    override val self = FqName("dev")

    object dart : PackageFqNameWrapper {
        override val self = dev.self.child("dart")

        object meta : PackageFqNameWrapper {
            override val self = dart.self.child("meta")

            object annotations : PackageFqNameWrapper {
                override val self = meta.self.child("annotations")

                val internal = self.child("internal")
                val protected = self.child("protected")
                val nonVirtual = self.child("nonVirtual")
                val sealed = self.child("sealed")
            }
        }
    }
}

private fun FqName.child(name: String) = child(Name.identifier(name))