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

package compile.dialect

import BaseTest
import assertCompile
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Dialect: Static")
class Static : BaseTest {
    @Test
    fun `explicit external companion object static method call`() = assertCompile {
        kotlin(
            """
            external interface DateTime {
                external companion object {
                    fun now(): DateTime
                }
            }

            fun main() {
                DateTime.now()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              DateTime.now();
            }
            """
        )
    }

    @Test
    fun `explicit external companion object static property access`() = assertCompile {
        kotlin(
            """
            external interface DateTime {
                external companion object {
                    val now: DateTime
                }
            }

            fun main() {
                DateTime.now
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              DateTime.now;
            }
            """
        )
    }

    // TODO: Will change when loading from Dart code directly
    @Disabled
    @Test
    fun `explicit external method in non-external companion object static call`() = assertCompile {
        kotlin(
            """
            external interface DateTime {
                companion object {
                    external fun now(): DateTime
                }
            }

            fun main() {
                DateTime.now()
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              DateTime.now();
            }

            @sealed
            class DateTime${'$'}Companion {
              const DateTime${'$'}Companion._();
              static const DateTime${'$'}Companion ${'$'}instance = const DateTime${'$'}Companion._();
            }
            """
        )
    }

    @Disabled
    @Test
    fun `explicit external companion object static property access of dependency`() = assertCompile {
        kotlin(
            """
            import dart.typed_data.Int16Array

            fun main() {
                val x = Int16Array.bytesPerElement
            }
            """
        )

        dart(
            """
            import "package:meta/meta.dart";

            void main() {
              final int x = Int16List.bytesPerElement;
            }
            """
        )
    }
}