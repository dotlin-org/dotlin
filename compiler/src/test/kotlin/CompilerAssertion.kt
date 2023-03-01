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

import kotlinx.coroutines.runBlocking
import org.dotlin.compiler.CompilationResult
import org.dotlin.compiler.KotlinToDartCompiler
import org.dotlin.compiler.backend.DotlinCompilationException
import org.dotlin.compiler.backend.bin.DotlinGenerator
import org.dotlin.compiler.backend.bin.dart
import org.dotlin.compiler.backend.util.LazyVar
import org.dotlin.compiler.factories
import org.dotlin.compiler.warnings
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.*
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.text.RegexOption.MULTILINE

abstract class DartTestProject {
    companion object {
        private val pubCachePath: Path = System.getenv()["PUB_CACHE"]?.let { Path(it) }
            ?: Path(System.getProperty("user.home")).resolve(".pub-cache")

        @Language("yaml")
        private fun defaultPubspec(name: String): String =
            """
            name: $name
            version: 1.0.0
            
            environment:
              sdk: '>=2.18.0 <3.0.0'

            publish_to: none
    
            dependencies:
              dotlin:
                path: ${stdlibPath.toRealPath()}
                
            dev_dependencies:
              dotlin_generator:
                path: ${DotlinGenerator.projectPath.toRealPath()}
            """.trimIndent()

        private fun defaultPackageConfig(name: String): String {
            fun pubCache(path: String) = "file://${pubCachePath.resolve("hosted/pub.dartlang.org").resolve(path)}"
            fun relative(path: String) = "file://${Path(path).toRealPath()}"

            // language=json
            @Suppress("JsonStandardCompliance")
            return """
            {
              "configVersion": 2,
              "packages": [
                {
                  "name": "_fe_analyzer_shared",
                  "rootUri": "${pubCache("_fe_analyzer_shared-52.0.0")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.17"
                },
                {
                  "name": "analyzer",
                  "rootUri": "${pubCache("analyzer-5.4.0")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.17"
                },
                {
                  "name": "async",
                  "rootUri": "${pubCache("async-2.10.0")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.18"
                },
                {
                  "name": "characters",
                  "rootUri": "${pubCache("characters-1.2.1")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "clock",
                  "rootUri": "${pubCache("clock-1.1.1")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "collection",
                  "rootUri": "${pubCache("collection-1.17.1")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.18"
                },
                {
                  "name": "convert",
                  "rootUri": "${pubCache("convert-3.1.1")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.18"
                },
                {
                  "name": "crypto",
                  "rootUri": "${pubCache("crypto-3.0.2")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.14"
                },
                {
                  "name": "dartx",
                  "rootUri": "${pubCache("dartx-1.1.0")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "dotlin",
                  "rootUri": "${relative(stdlibPath.toString())}",
                  "packageUri": "lib/",
                  "languageVersion": "2.18"
                },
                {
                  "name": "dotlin_generator",
                  "rootUri": "${relative(DotlinGenerator.projectPath.toString())}",
                  "packageUri": "lib/",
                  "languageVersion": "2.18"
                },
                {
                  "name": "file",
                  "rootUri": "${pubCache("file-6.1.4")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "fixnum",
                  "rootUri": "${pubCache("fixnum-1.0.1")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "glob",
                  "rootUri": "${pubCache("glob-2.1.1")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.15"
                },
                {
                  "name": "meta",
                  "rootUri": "${pubCache("meta-1.8.0")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "package_config",
                  "rootUri": "${pubCache("package_config-2.1.0")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "path",
                  "rootUri": "${pubCache("path-1.8.3")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "protobuf",
                  "rootUri": "${pubCache("protobuf-2.1.0")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "pub_semver",
                  "rootUri": "${pubCache("pub_semver-2.1.3")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.17"
                },
                {
                  "name": "source_span",
                  "rootUri": "${pubCache("source_span-1.9.1")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.14"
                },
                {
                  "name": "string_scanner",
                  "rootUri": "${pubCache("string_scanner-1.2.0")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.18"
                },
                {
                  "name": "term_glyph",
                  "rootUri": "${pubCache("term_glyph-1.2.1")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "time",
                  "rootUri": "${pubCache("time-2.1.3")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "typed_data",
                  "rootUri": "${pubCache("typed_data-1.3.1")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "watcher",
                  "rootUri": "${pubCache("watcher-1.0.2")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.14"
                },
                {
                  "name": "yaml",
                  "rootUri": "${pubCache("yaml-3.1.1")}",
                  "packageUri": "lib/",
                  "languageVersion": "2.12"
                },
                {
                  "name": "$name",
                  "rootUri": "../",
                  "packageUri": "lib/",
                  "languageVersion": "2.18"
                }
              ],
              "generated": "${LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)}",
              "generator": "pub",
              "generatorVersion": "2.18.2"
            }
            """
        }
    }

    open val name: String = "test"
    open val path = createTempDirectory()

    open var pubspec: String by LazyVar { defaultPubspec(name) }

    @Language("yaml")
    var pubspecLock: String =
        """
        packages:
          _fe_analyzer_shared:
            dependency: transitive
            description:
              name: _fe_analyzer_shared
              url: "https://pub.dartlang.org"
            source: hosted
            version: "52.0.0"
          analyzer:
            dependency: transitive
            description:
              name: analyzer
              url: "https://pub.dartlang.org"
            source: hosted
            version: "5.4.0"
          async:
            dependency: transitive
            description:
              name: async
              url: "https://pub.dartlang.org"
            source: hosted
            version: "2.10.0"
          characters:
            dependency: transitive
            description:
              name: characters
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.2.1"
          clock:
            dependency: transitive
            description:
              name: clock
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.1.1"
          collection:
            dependency: transitive
            description:
              name: collection
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.17.1"
          convert:
            dependency: transitive
            description:
              name: convert
              url: "https://pub.dartlang.org"
            source: hosted
            version: "3.1.1"
          crypto:
            dependency: transitive
            description:
              name: crypto
              url: "https://pub.dartlang.org"
            source: hosted
            version: "3.0.2"
          dartx:
            dependency: transitive
            description:
              name: dartx
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.1.0"
          dotlin:
            dependency: "direct main"
            description:
              path: "${stdlibPath.toRealPath()}"
              relative: false
            source: path
            version: "0.0.1"
          dotlin_generator:
            dependency: "direct dev"
            description:
              path: "${DotlinGenerator.projectPath.toRealPath()}"
              relative: false
            source: path
            version: "0.0.1"
          file:
            dependency: transitive
            description:
              name: file
              url: "https://pub.dartlang.org"
            source: hosted
            version: "6.1.4"
          fixnum:
            dependency: transitive
            description:
              name: fixnum
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.0.1"
          glob:
            dependency: transitive
            description:
              name: glob
              url: "https://pub.dartlang.org"
            source: hosted
            version: "2.1.1"
          meta:
            dependency: transitive
            description:
              name: meta
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.8.0"
          package_config:
            dependency: transitive
            description:
              name: package_config
              url: "https://pub.dartlang.org"
            source: hosted
            version: "2.1.0"
          path:
            dependency: transitive
            description:
              name: path
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.8.3"
          protobuf:
            dependency: transitive
            description:
              name: protobuf
              url: "https://pub.dartlang.org"
            source: hosted
            version: "2.1.0"
          pub_semver:
            dependency: transitive
            description:
              name: pub_semver
              url: "https://pub.dartlang.org"
            source: hosted
            version: "2.1.3"
          source_span:
            dependency: transitive
            description:
              name: source_span
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.9.1"
          string_scanner:
            dependency: transitive
            description:
              name: string_scanner
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.2.0"
          term_glyph:
            dependency: transitive
            description:
              name: term_glyph
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.2.1"
          time:
            dependency: transitive
            description:
              name: time
              url: "https://pub.dartlang.org"
            source: hosted
            version: "2.1.3"
          typed_data:
            dependency: transitive
            description:
              name: typed_data
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.3.1"
          watcher:
            dependency: transitive
            description:
              name: watcher
              url: "https://pub.dartlang.org"
            source: hosted
            version: "1.0.2"
          yaml:
            dependency: transitive
            description:
              name: yaml
              url: "https://pub.dartlang.org"
            source: hosted
            version: "3.1.1"
        sdks:
          dart: ">=2.18.2 <3.0.0"
        """.trimIndent()

    private var packageConfig: String by LazyVar { defaultPackageConfig(name) }

    @Language("yaml")
    private val analysisOptions: String =
        """
        analyzer:
          language:
            strict-casts: true
            strict-inference: true
            strict-raw-types: true
          errors:
            unnecessary_non_null_assertion: info
        """.trimIndent()

    fun writeConfigFiles(writeLockFiles: Boolean = true) {
        path.resolve("pubspec.yaml").writeText(pubspec)
        path.resolve("analysis_options.yaml").writeText(analysisOptions)

        if (writeLockFiles) {
            if (pubspecLock.isNotBlank()) {
                path.resolve("pubspec.lock").writeText(pubspecLock)
            }

            if (packageConfig.isNotBlank()) {
                path.resolve(".dart_tool")
                    .createDirectories()
                    .resolve("package_config.json")
                    .writeText(packageConfig)
            }
        }
    }

    abstract fun kotlin(@Language("kotlin") kotlin: String, path: Path? = null)
    abstract fun dart(@Language("dart") dart: String, path: Path? = null)

    protected val dependencies = mutableListOf<Dependency>()

    fun dependency(block: Dependency.() -> Unit) {
        val dependency = Dependency()
        block(dependency)

        dependency.writeConfigFiles()

        // pubspec.lock and dart_tool/package_config.json are invalidated.
        // TODO: Update them accordingly to prevent a "dart pub get" instead of clearing
        pubspecLock = ""
        packageConfig = ""

        pubspec = pubspec.replace(
            Regex("^dependencies:\n", MULTILINE),
            // language=yaml
            """
            dependencies:
              ${dependency.name}:
                path: ${dependency.path}

            """.trimIndent(),
        )

        dependencies.add(dependency)
    }

    class Dependency : DartTestProject() {
        private val nameRegex = Regex("^name: *([A-Za-z_]+)")

        override var name: String = super.name
            set(value) {
                field = value
                pubspec = nameRegex.replace(pubspec, "name: $value")
            }

        var needsCompile: Boolean = false
            private set

        private fun Path?.writeSource(source: String, extension: String) {
            val projectPath = this@Dependency.path
            val filePath = this ?: Path("lib/${System.currentTimeMillis()}$extension")

            projectPath.resolve(filePath).apply {
                parent.createDirectories()
                writeText(source)
            }
        }

        override fun kotlin(@Language("kotlin") kotlin: String, path: Path?) {
            path.writeSource(kotlin, ".kt")
            needsCompile = true
        }

        override fun dart(@Language("dart") dart: String, path: Path?) = path.writeSource(dart, ".dart")
    }
}

sealed class CompilerAssertion : DartTestProject() {
    protected val kotlinSources = mutableMapOf<Path, String>()
    protected val dartSources = mutableMapOf<Path, String>()

    private var unnamedKotlinFiles = 0
    private var unnamedDartFiles = 0

    override fun kotlin(@Language("kotlin") kotlin: String, path: Path?) {
        val p = path ?: Path("lib/${unnamedKotlinFiles++}.kt")
        kotlinSources[p] = kotlin.trimIndent()
    }

    override fun dart(@Language("dart") dart: String, path: Path?) {
        val p = path ?: Path("lib/${unnamedDartFiles++}.dt.g.dart")
        dartSources[p] = dart.trimIndent()
    }

    fun dart(@Language("dart") dart: String, path: Path, assert: Boolean) {
        dart(dart, path)

        if (!assert) {
            this.path.resolve(path).apply {
                parent.createDirectories()
                writeText(dart)
            }
        }
    }

    open fun compile(): CompilationResult {
        writeConfigFiles()

        dependencies.forEach {
            if (it.needsCompile) {
                println("Compiling dependency: ${it.name} (${it.path})")
                KotlinToDartCompiler.compile(it.path)
            }
        }

        kotlinSources.forEach { (sourcePath, source) ->
            path.resolve(sourcePath).apply {
                parent.createDirectories()
                writeText(source)
            }
        }

        println("Compiling project: $path")

        return KotlinToDartCompiler.compile(
            path,
            format = true
        )
    }

    fun assertDartAnalysis() =
        runBlocking { assertEquals(0, dart.analyze(workingDirectory = path), message = "Dart analysis errors") }

    abstract fun assert()
}

class AssertCompilesTo : CompilerAssertion() {
    override fun assert() {
        assertDoesNotThrow { compile() }

        val compiledDartSources =
            Files.walk(path)
                .filter { it.name.endsWith(".dt.g.dart") && it.isRegularFile() }
                .toList()
                .map { it to it.readText().removeSuffix("\n") }

        val projectPath = this.path

        compiledDartSources.forEach { (path, source) ->
            assertEquals(dartSources[path.relativeTo(projectPath)], source)
        }

        assertDartAnalysis()
    }
}

class AssertCanCompileProject : CompilerAssertion() {
    public override lateinit var path: Path

    override var pubspec: String
        get() = throw UnsupportedOperationException()
        set(_) = throw UnsupportedOperationException()

    override fun kotlin(kotlin: String, path: Path?) = throw UnsupportedOperationException()
    override fun dart(dart: String, path: Path?) = throw UnsupportedOperationException()

    override fun compile() = KotlinToDartCompiler.compile(path)

    override fun assert() {
        assertDoesNotThrow { compile() }
        assertDartAnalysis()
    }
}

sealed class AssertCompilesWithDiagnostics : CompilerAssertion() {
    abstract var diagnostics: List<DiagnosticFactory<*>>
}

class AssertCompilesWithError : AssertCompilesWithDiagnostics() {
    override lateinit var diagnostics: List<DiagnosticFactory<*>>

    override fun assert() {
        val error = assertThrows<DotlinCompilationException> { compile() }
        diagnostics.forEach {
            assertContains(error.errors.factories, it)
        }
    }
}

class AssertCompilesWithWarning : AssertCompilesWithDiagnostics() {
    override lateinit var diagnostics: List<DiagnosticFactory<*>>

    override fun assert() {
        val (_, diagnostics) = assertDoesNotThrow { compile() }
        this.diagnostics.forEach {
            assertContains(diagnostics.warnings.factories, it)
        }
    }
}

class AssertCanCompile : CompilerAssertion() {
    override fun assert() {
        assertDoesNotThrow { compile() }
        assertDartAnalysis()
    }
}

inline fun assertCompile(block: AssertCompilesTo.() -> Unit) = AssertCompilesTo().let {
    block(it)
    it.assert()
}

inline fun assertCanCompile(block: AssertCanCompile.() -> Unit) = AssertCanCompile().let {
    block(it)
    it.assert()
}

inline fun assertCanCompileProject(block: AssertCanCompileProject.() -> Unit) = AssertCanCompileProject().let {
    block(it)
    it.assert()
}

inline fun assertCompilesWithError(factory: DiagnosticFactory<*>, block: AssertCompilesWithError.() -> Unit) =
    assertCompilesWithErrors(factory, block = block)

inline fun assertCompilesWithErrors(vararg factories: DiagnosticFactory<*>, block: AssertCompilesWithError.() -> Unit) =
    AssertCompilesWithError().let {
        require(factories.isNotEmpty())
        it.diagnostics = factories.toList()
        block(it)
        it.assert()
    }

inline fun assertCompilesWithWarning(factory: DiagnosticFactory<*>, block: AssertCompilesWithWarning.() -> Unit) =
    assertCompilesWithWarnings(factory, block = block)

inline fun assertCompilesWithWarnings(
    vararg factories: DiagnosticFactory<*>,
    block: AssertCompilesWithWarning.() -> Unit
) =
    AssertCompilesWithWarning().let {
        require(factories.isNotEmpty())
        it.diagnostics = factories.toList()
        block(it)
        it.assert()
    }