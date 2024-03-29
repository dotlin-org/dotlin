/*
 * Copyright 2023 Wilko Manger
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

package org.dotlin.compiler.backend.bin

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.schema.ProtoBufSchemaGenerator
import org.dotlin.compiler.backend.DotlinGeneratorException
import org.dotlin.compiler.dart.element.*
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.reflect.KClass

/**
 * Generates protobuf elements for a Dart project.
 */
@OptIn(ExperimentalSerializationApi::class)
object DotlinGenerator {
    val projectPath = Path("../packages/dotlin_generator")

    suspend fun generate(paths: List<Pair<Path, Path>>, workingDirectory: Path) {
        val flattenedPaths = run {
            val result = mutableListOf<String>()
            for (pathPair in paths) {
                result.add(pathPair.first.toString())
                result.add(pathPair.second.toString())
            }
            result.toTypedArray()
        }

        val exitCode = dart.run("dotlin_generator:generate", *flattenedPaths, workingDirectory = workingDirectory)

        if (exitCode != 0) {
            throw DotlinGeneratorException("Failed to generate Dart elements")
        }
    }

    fun generateSchema() {
        val schema = ProtoBufSchemaGenerator.generateSchemaText(
            listOf(
                DartPackageElement.serializer().descriptor,
                DartLibraryElement.serializer().descriptor,
                DartCompilationUnitElement.serializer().descriptor,
                DartClassElement.serializer().descriptor,
                DartEnumElement.serializer().descriptor,
                DartPropertyElement.serializer().descriptor,
                DartPropertyAccessorElement.serializer().descriptor,
                DartFunctionElement.serializer().descriptor,
                DartConstructorElement.serializer().descriptor,
                DartParameterElement.serializer().descriptor,
                DartType.serializer().descriptor,
            )
        ).let {
            // We have to patch the schema: Because we're using contextual serializers, all fields which use
            // those serializers have become `bytes`. We must revert that back to their original types.
            Regex("(required|optional|repeated) bytes ([a-zA-Z]+)").replace(it) { match ->
                val keyword = match.groupValues[1]
                val fieldName = match.groupValues[2]
                val type = matchType(
                    fieldName,
                    "librar" to DartLibraryElement::class,
                    "unit" to DartCompilationUnitElement::class,
                    "class" to DartClassElement::class,
                    "enum" to DartEnumElement::class,
                    "propert" to DartPropertyElement::class,
                    "getter" to DartPropertyAccessorElement::class,
                    "setter" to DartPropertyAccessorElement::class,
                    "constructor" to DartConstructorElement::class,
                    "function" to DartFunctionElement::class,
                    "method" to DartFunctionElement::class,
                    "parameter" to DartParameterElement::class,
                    "typeParameter" to DartTypeParameterElement::class,
                ) ?: "bytes" // If there's no match, just keep it as "bytes"
                "$keyword $type $fieldName"
            }
        }

        val protoFile = projectPath.resolve("proto/elements.proto")
        protoFile.writeText(schema)
    }

    private fun matchType(fieldName: String, vararg mapping: Pair<String, KClass<*>>): String? {
        return mapping.toMap().entries.firstNotNullOfOrNull { (term, klass) ->
            when (fieldName.startsWith(term)) {
                true -> klass.simpleName
                else -> null
            }
        }
    }
}