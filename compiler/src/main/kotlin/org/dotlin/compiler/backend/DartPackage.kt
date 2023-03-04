package org.dotlin.compiler.backend

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.yamlMap
import com.google.common.base.Objects
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.network.sockets.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.dotlin.compiler.backend.bin.dart
import org.dotlin.compiler.backend.util.PathSerializer
import org.dotlin.compiler.backend.util.URISerializer
import java.net.URI
import java.net.URL
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.*

open class DartPackage(
    val name: String,
    val publisher: String,
    val repository: URL?,
    path: Path,
    packagePath: Path,
) {
    /**
     * Path to where the Dart package is stored locally.
     *
     * Absolute, real (resolved symlinks) path.
     */
    val path: Path = path.toRealPath()

    /**
     * Path to where the relevant Dart files are. Most of the time `$path/lib`.
     */
    val packagePath: Path = this.path.resolve(packagePath)
    val dotlinPath: Path = this.path.resolve(".dotlin")
    val klibPath: Path = dotlinPath.resolve("klib")
    val dlibPath: Path = dotlinPath.resolve("dlib")

    override fun equals(other: Any?) = other is DartPackage && other.name == this.name && other.path == this.path
    override fun hashCode() = Objects.hashCode(name, path)

    override fun toString() = "DartPackage(name = $name, publisher = $publisher, path = $path)"
}

class DartProject(
    name: String,
    val dependencies: Set<DartPackage>,
    path: Path,
    publishTo: URL?,
) : DartPackage(
    name,
    publisher = "" /* TODO */,
    repository = publishTo,
    path,
    packagePath = Path("lib") /* TODO */
) {
    /**
     * Whether this project has a repository to publish to (`publish_to` in `pubspec.yaml`).
     */
    val isPublishable: Boolean
        get() = repository != null

    companion object {
        private val http = HttpClient(CIO)

        private val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

        suspend fun from(projectPath: Path): DartProject {
            suspend fun pubGet() = dart.pub.get(workingDirectory = projectPath)

            val pubspecFile = projectPath.resolve("pubspec.yaml")
            val pubspecLockFile = projectPath.resolve("pubspec.lock")

            if (pubspecLockFile.notExists()) {
                pubGet()
            }

            fun parseYaml(path: Path) = Yaml.default.parseToYamlNode(path.readText()).yamlMap

            val pubspec = parseYaml(pubspecFile)
            val pubspecLock = parseYaml(pubspecLockFile)

            val packageConfigFile = projectPath.resolve(".dart_tool/package_config.json")

            if (packageConfigFile.notExists()) {
                pubGet()
            }

            val packageConfig: DartPackageConfig = json.decodeFromString(packageConfigFile.readText())

            val packages = packageConfig.packages.associateBy { it.name }

            return DartProject(
                name = pubspec.get<YamlScalar>("name")!!.content,
                dependencies = pubspecLock.get<YamlMap>("packages")
                    ?.entries
                    ?.mapNotNull { (key, node) ->
                        val nodeMap = node.yamlMap
                        val name = key.content
                        val config = packages[name] ?: return@mapNotNull null
                        val path = config.rootUri.let {
                            // URI.toPath() does not support relative URIs (schemeless).
                            when (it.scheme) {
                                null -> Path(it.path)
                                else -> it.toPath()
                            }
                        }.let {
                            when {
                                // URI is relative to the .dart_tool directory.
                                !it.isAbsolute -> packageConfigFile.parent.resolve(it)
                                else -> it
                            }
                        }

                        val packagePath = config.packagePath

                        // TODO: Read PUB_HOSTED_URL
                        val fallbackRepositoryUrl = URL("https://pub.dev")
                        val repositoryUrl = when (nodeMap.getScalar("source")?.contentToString()) {
                            "hosted" -> {
                                val url = nodeMap
                                    .get<YamlMap>("description")?.yamlMap
                                    ?.getScalar("url")?.content

                                when (url) {
                                    // We use pub.dev instead of pub.dartlang.org.
                                    "https://pub.dartlang.org" -> null
                                    else -> URL(url)
                                }
                            }

                            else -> null
                        } ?: fallbackRepositoryUrl

                        val dotlinConfigPath = path.resolve(".dotlin/config.json")
                        val dotlinConfig: DotlinConfig? = run {
                            when {
                                dotlinConfigPath.exists() -> json.decodeFromString(dotlinConfigPath.readText())
                                else -> null
                            }
                        }

                        suspend fun getPublisher() = Json.parseToJsonElement(
                            http.get("${repositoryUrl}/api/packages/$name/publisher").bodyAsText()
                        ).jsonObject["publisherId"]?.jsonPrimitive?.content ?: ""

                        var tries = 0

                        val publisher = dotlinConfig?.publisher ?: try {
                            getPublisher().let { if (it == "null") "" else it }
                        } catch (e: ConnectTimeoutException) {
                            tries++
                            when (tries) {
                                in 0..3 -> {
                                    delay(5000)
                                    getPublisher()
                                }

                                else -> ""
                            }
                        }

                        if (publisher != dotlinConfig?.publisher) {
                            dotlinConfigPath.apply {
                                parent.createDirectories()
                                writeText(
                                    json.encodeToString(DotlinConfig(publisher))
                                )
                            }
                        }

                        DartPackage(
                            name,
                            publisher,
                            repositoryUrl,
                            path,
                            packagePath,
                        )
                    }
                    ?.toSet()
                    .orEmpty(),
                path = projectPath,
                publishTo = pubspec.get<YamlScalar>("publish_to")?.content
                    ?.let { if (it != "none") it else null }
                    ?.let { URL(it) }
            )
        }
    }
}

@Serializable
private data class DotlinConfig(
    val publisher: String
)

@Serializable
private data class DartPackageConfig(
    val packages: List<Package>
) {
    @Serializable
    data class Package(
        val name: String,

        @Serializable(with = URISerializer::class)
        val rootUri: URI,

        @SerialName("packageUri")
        @Serializable(with = PathSerializer::class)
        val packagePath: Path
    )
}

@OptIn(ExperimentalContracts::class)
fun DartPackage.isCurrent(): Boolean {
    contract {
        returns(true) implies (this@isCurrent is DartProject)
    }

    return this is DartProject
}