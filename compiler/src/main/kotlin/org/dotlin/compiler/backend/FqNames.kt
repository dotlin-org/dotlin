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

    // Annotations
    val const = self.child("const")
    val DartName = self.child("DartName")
    val DartPositional = self.child("DartPositional")
    val DartLibrary = self.child("DartLibrary")
    val DartStatic = self.child("DartStatic")
    val DartExtensionName = self.child("DartExtensionName")
    val DartIndex = self.child("DartIndex")

    // Internal annotations.
    val DartGetter = self.child("DartGetter")
    val DartExtension = self.child("DartExtension")
    val DartHideNameFromCore = self.child("DartHideNameFromCore")

    // Lowering helpers.
    val `$Return` = self.child("\$Return")

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

        val identical = self.child("identical")
        val Iterator = self.child("Iterator")
        val Iterable = self.child("Iterable")
        val UnsupportedError = self.child("UnsupportedError")
    }
}

object kotlin : PackageFqNameWrapper {
    override val self = FqName("kotlin")

    val Lazy = self.child("Lazy")

    object collections : PackageFqNameWrapper {
        override val self = kotlin.self.child("collections")

        val Iterator = self.child("Iterator")
    }

    object ranges : PackageFqNameWrapper {
        override val self = kotlin.self.child("ranges")

        val step = self.child("step")
        val until = self.child("until")
        val downTo = self.child("downTo")
    }
}

private fun FqName.child(name: String) = child(Name.identifier(name))