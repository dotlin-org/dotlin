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

package analysis

import BaseTest
import assertCompilesWithError
import org.dotlin.compiler.backend.steps.src2ir.analyze.ir.ErrorsDart.*
import org.jetbrains.kotlin.diagnostics.Errors.UNRESOLVED_REFERENCE
import org.jetbrains.kotlin.diagnostics.Errors.UPPER_BOUND_VIOLATED
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

@DisplayName("Analysis: Interop")
class Interop : BaseTest {
    @Test
    fun `error if using class without const constructor as annotation`() = assertCompilesWithError(UNRESOLVED_REFERENCE) {
        dart(
            """
            class NonAnnotation {}
            """,
            Path("lib/annon.dart")
        )

        kotlin(
            """
            import dev.pub.test.annon.annotations.NonAnnotation

            @NonAnnotation
            class Test
            """
        )
    }

    @Test
    fun `error if using non-const property as annotation`() = assertCompilesWithError(UNRESOLVED_REFERENCE) {
        dart(
            """
            final nonAnnotation = 3;
            """,
            Path("lib/props.dart")
        )

        kotlin(
            """
            import dev.pub.test.props.annotations.nonAnnotation

            @nonAnnotation
            class Test
            """
        )
    }

    @Test
    fun `error if importing Dart declaration from export and non-export`() = assertCompilesWithError(DUPLICATE_IMPORT) {
        dart(
            """
            class BlackBird {}
            """,
            Path("lib/src/black_bird.dart"),
            assert = false
        )

        dart(
            """
            export "src/black_bird.dart";
            """,
            Path("lib/birds.dart"),
            assert = false
        )

        kotlin(
            """
            import pkg.test.birds.BlackBird
            import pkg.test.src.black_bird.BlackBird as SrcBlackBird

            fun main() {
                val bird: SrcBlackBird = BlackBird()
            }
            """
        )
    }


    @Test
    fun `error if using Dart class from export that was hidden`() = assertCompilesWithError(UNRESOLVED_REFERENCE) {
        dart(
            """
            class BlackBird {}
            class BlueBird {}
            """,
            Path("lib/src/birds_impl.dart"),
            assert = false
        )

        dart(
            """
            export "src/birds_impl.dart" hide BlackBird;
            """,
            Path("lib/birds.dart"),
            assert = false
        )

        kotlin(
            """
            import dev.pub.test.birds.BlackBird

            fun main() {
                val myBird = BlackBird()
            }
            """
        )
    }

    @Test
    fun `error if using Dart class from export that wasn't shown`() = assertCompilesWithError(UNRESOLVED_REFERENCE) {
        dart(
            """
            class BlackBird {}
            class BlueBird {}
            """,
            Path("lib/src/birds_impl.dart"),
            assert = false
        )

        dart(
            """
            export "src/birds_impl.dart" show BlueBird;
            """,
            Path("lib/birds.dart"),
            assert = false
        )

        kotlin(
            """
            import dev.pub.test.birds.BlackBird

            fun main() {
                val myBird = BlackBird()
            }
            """
        )
    }

    @Test
    fun `error if using Dart class from export that's shown and hidden`() = assertCompilesWithError(UNRESOLVED_REFERENCE) {
        dart(
            """
            class BlackBird {}
            class BlueBird {}
            """,
            Path("lib/src/birds_impl.dart"),
            assert = false
        )

        dart(
            """
            export "src/birds_impl.dart" show BlackBird hide BlackBird;
            """,
            Path("lib/birds.dart"),
            assert = false
        )

        kotlin(
            """
            import dev.pub.test.birds.BlackBird

            fun main() {
                val myBird = BlackBird()
            }
            """
        )
    }

    @Test
    fun `error if not conforming to Dart type parameter bound`() = assertCompilesWithError(UPPER_BOUND_VIOLATED) {
        dart(
            """
            class Marker

            class MyClass<A extends Marker> {}
            """,
            Path("lib/my_class.dart"),
            assert = false,
        )

        kotlin(
            """
            import pkg.test.my_class.Marker
            import pkg.test.my_class.MyClass

            fun main() {
                val x = MyClass<Int>()
            }
            """
        )
    }

    @Test
    fun `error if initializing const val with Dart named non-const constructor`() =
        assertCompilesWithError(CONST_INITIALIZED_WITH_NON_CONSTANT_VALUE) {
            dart(
                """
                class Alpha {
                  Alpha.named();
                }
                """,
                Path("lib/alphabet.dart"),
                assert = false,
            )

            kotlin(
                """
                import pkg.test.alphabet.Alpha

                fun main() {
                    const val x = Alpha.named()
                }
                """
            )
        }
}