package org.dotlin.compiler.backend

import com.google.common.base.Objects
import java.nio.file.Path

open class DartPackage(
    val name: String,
    /**
     * Absolute, real (resolved symlinks) path.
     */
    path: Path
) {
    val path: Path = path.toRealPath().toAbsolutePath()

    val dotlinPath: Path = this.path.resolve(".dotlin")
    val klibPath: Path = dotlinPath.resolve("klib")

    override fun equals(other: Any?) = other is DartPackage && other.name == this.name && other.path == this.path

    override fun hashCode() = Objects.hashCode(name, path)
}

class DartProject(
    name: String,
    path: Path,
    val isLibrary: Boolean,
    val dependencies: Set<DartPackage>,
) : DartPackage(name, path)
