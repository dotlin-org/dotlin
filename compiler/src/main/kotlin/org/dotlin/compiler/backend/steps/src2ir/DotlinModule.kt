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

package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.DartPackage
import org.dotlin.compiler.backend.DartProject
import org.dotlin.compiler.backend.descriptors.DartDescriptorContext
import org.dotlin.compiler.backend.descriptors.DartPackageFragmentProvider
import org.jetbrains.kotlin.backend.common.serialization.metadata.KlibMetadataModuleDescriptorFactory
import org.jetbrains.kotlin.builtins.functions.functionInterfacePackageFragmentProvider
import org.jetbrains.kotlin.descriptors.ModuleCapability
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.CompositePackageFragmentProvider
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.descriptors.konan.DeserializedKlibModuleOrigin
import org.jetbrains.kotlin.descriptors.konan.KlibModuleOrigin
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.parseModuleHeader
import org.jetbrains.kotlin.library.unresolvedDependencies
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.DeserializationConfiguration
import org.jetbrains.kotlin.storage.StorageManager


/**
 * In Dart terms, a module is a Dart _package_. A Dart package contains multiple Dart libraries, which are most of
 * the time a single file.
 *
 * Most Dart packages have a single Dart library file that exports all relevant "mini libraries".
 *
 * A Dotlin package is a Dart package that has Dart and Kotlin files. Both Dart packages and Dotlin packages
 * are represented by [DotlinModule].
 */
class DotlinModule private constructor(
    project: DartProject,
    val dartPackage: DartPackage,
    val dartElementLocator: DartElementLocator,
    val klib: KotlinLibrary?,
    klibModuleFactory: KlibMetadataModuleDescriptorFactory,
    storageManager: StorageManager,
    val impl: ModuleDescriptorImpl,
) : ModuleDescriptor by impl {
    constructor(
        project: DartProject,
        dartPackage: DartPackage,
        dartElementLocator: DartElementLocator,
        builtIns: DotlinBuiltIns,
        klib: KotlinLibrary?,
        klibModuleFactory: KlibMetadataModuleDescriptorFactory,
        storageManager: StorageManager,
    ) : this(
        project,
        dartPackage,
        dartElementLocator,
        klib,
        klibModuleFactory,
        storageManager,
        ModuleDescriptorImpl(
            Name.special("<${dartPackage.name}>"),
            storageManager,
            builtIns,
            DartPlatform,
            capabilities = buildMap {
                this[CAPABILITY] = Capability()

                if (klib != null) {
                    this[KlibModuleOrigin.CAPABILITY] = DeserializedKlibModuleOrigin(klib)
                }
            },
            stableName = Name.special("<${dartPackage.name}>")
        )
    )

    val isStdlib = dartPackage.let { it.name == "dotlin" && it.publisher == "dotlin.org" /* && it.host == pub.dev*/ }

    init {
        if (isStdlib) {
            builtIns.builtInsModule = impl
        }

        impl.initialize(
            CompositePackageFragmentProvider(
                providers = listOfNotNull(
                    run {
                        when {
                            isStdlib -> functionInterfacePackageFragmentProvider(storageManager, this)
                            else -> null
                        }
                    },
                    DartPackageFragmentProvider(
                        project,
                        DartDescriptorContext(this, dartPackage, dartElementLocator, storageManager),
                    ),
                    klib?.let {
                        klibModuleFactory.createPackageFragmentProvider(
                            klib,
                            packageAccessHandler = null,
                            parseModuleHeader(klib.moduleHeaderData).packageFragmentNameList,
                            storageManager,
                            moduleDescriptor = this,
                            DeserializationConfiguration.Default,
                            compositePackageFragmentAddend = null,
                            LookupTracker.DO_NOTHING,
                        )
                    }

                ),
                debugName = "DartPackageModule($name)"
            )
        )

        impl.getCapability(CAPABILITY)!!.dotlinModule = this
    }

    /**
     * Select and set the dependencies the given collection of [modules].
     */
    fun selectDependenciesFrom(
        modules: List<DotlinModule>,
        builtInsModule: ModuleDescriptorImpl? = null
    ) {
        // We use Set, because if `impl` is the `builtInsModule`, we'd get duplicates.
        val dependencies = mutableSetOf(impl, builtInsModule ?: impl.builtIns.builtInsModule)

        // TODO: Set dependencies from pubspec

        klib?.unresolvedDependencies?.mapTo(dependencies) { klibDep ->
            modules.first { it.name.asStringStripSpecialMarkers() == klibDep.path }.impl
        }

        impl.setDependencies(dependencies.toList())
    }

    companion object {
        val CAPABILITY = ModuleCapability<Capability>("DartPackage")
    }

    class Capability(var dotlinModule: DotlinModule? = null)

    override fun getOriginal(): DotlinModule = this

    override fun toString() = "${this::class.simpleName}: ${dartPackage.name}"
}

fun List<DotlinModule>.setDependencies(builtInsModule: ModuleDescriptorImpl? = null) {
    forEach {
        it.selectDependenciesFrom(this, builtInsModule)
    }
}

val ModuleDescriptor.dotlinModule: DotlinModule?
    get() = when (this) {
        is DotlinModule -> this
        else -> getCapability(DotlinModule.CAPABILITY)?.dotlinModule
    }

// TODO: Move to utils file
val ModuleDescriptor.klib: KotlinLibrary?
    get() = (getCapability(KlibModuleOrigin.CAPABILITY) as? DeserializedKlibModuleOrigin)?.library