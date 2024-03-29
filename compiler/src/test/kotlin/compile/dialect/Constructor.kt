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
import kotlin.io.path.Path

@DisplayName("Compile: Dialect: Constructor")
class Constructor : BaseTest {
    @Test
    fun `@DartConstructor in external companion object`() = assertCompile {
        kotlin(
            """
            @DartLibrary("test.dart")
            external class Test {
                external companion object {
                    @DartConstructor
                    fun create(): Test
                }
            }

            fun main() {
                Test.create()
            }
            """
        )

        dart(
            """
            class Test {
              Test.create();
            }
            """,
            Path("lib/test.dart"),
            assert = false
        )

        dart(
            """
            import "test.dart" show Test;

            void main() {
              Test.create();
            }
            """
        )
    }

    @Test
    fun `const @DartConstructor call`() = assertCompile {
        kotlin(
            """
            @DartLibrary("test.dart")
            external class Test {
                companion object {
                    @DartConstructor
                    const external fun create(): Test
                }
            }

            fun main() {
                @const Test.create()
            }
            """
        )

        dart(
            """
            class Test {
              const Test.create();
            }
            """,
            Path("lib/test.dart"),
            assert = false
        )

        dart(
            """
            import "test.dart" show Test;
            import "package:meta/meta.dart" show sealed;

            void main() {
              const Test.create();
            }

            @sealed
            class Test${'$'}Companion {
              const Test${'$'}Companion._();
              static const Test${'$'}Companion ${'$'}instance = const Test${'$'}Companion._();
            }
            """
        )
    }

    // TODO: The external Dart code doesn't make much sense now. Would make more sense
    // with a future Dart @nested annotation.
    @Test
    fun `const @DartConstructor call in nested class`() = assertCompile {
        kotlin(
            """
            @DartLibrary("test.dart")
            external class TestContainer {
                @DartLibrary("test.dart")
                external class Test {
                    companion object {
                        @DartConstructor
                        const external fun create(): Test
                    }
                }
            }

            fun main() {
                @const TestContainer.Test.create()
            }
            """
        )

        dart(
            """
            class TestContainer${'$'}Test {
              const TestContainer${'$'}Test.create();
            }
            """,
            Path("lib/test.dart"),
            assert = false
        )

        dart(
            """
            import "test.dart" show TestContainer${'$'}Test;

            void main() {
              const TestContainer${'$'}Test.create();
            }
            """
        )
    }

    @Test
    fun `@DartConstructor call with value arguments`() = assertCompile {
        kotlin(
            """
            @DartLibrary("test.dart")
            external class Test {
                companion object {
                    @DartConstructor
                    external fun create(a: Int, b: String): Test
                }
            }

            fun main() {
                Test.create(10, "abc")
            }
            """
        )

        dart(
            """
            class Test {
              const Test.create(int a, String b);
            }
            """,
            Path("lib/test.dart"),
            assert = false
        )

        dart(
            """
            import "test.dart" show Test;
            import "package:meta/meta.dart" show sealed;

            void main() {
              Test.create(10, "abc");
            }

            @sealed
            class Test${'$'}Companion {
              const Test${'$'}Companion._();
              static const Test${'$'}Companion ${'$'}instance = const Test${'$'}Companion._();
            }
            """
        )
    }

    @Test
    fun `@DartConstructor call with type arguments`() = assertCompile {
        kotlin(
            """
            @DartLibrary("markers.dart")
            abstract external class Marker

            @DartLibrary("markers.dart")
            external class MarkerImplementor : Marker()

            @DartLibrary("markers.dart")
            external class Test<A, B : Marker> {
                companion object {
                    @DartConstructor
                    external fun <A, B : Marker> create(a: A, b: B): Test<A, B>
                }
            }

            fun main() {
                Test.create(10, MarkerImplementor())
            }
            """
        )

        dart(
            """
            abstract class Marker {}

            class MarkerImplementor extends Marker {}

            class Test<A, B extends Marker> {
              Test.create(A a, B b);
            }
            """,
            Path("lib/markers.dart"),
            assert = false
        )

        dart(
            """
            import "markers.dart" show MarkerImplementor, Test;
            import "package:meta/meta.dart" show sealed;

            void main() {
              Test<int, MarkerImplementor>.create(10, MarkerImplementor());
            }

            @sealed
            class Test${'$'}Companion {
              const Test${'$'}Companion._();
              static const Test${'$'}Companion ${'$'}instance = const Test${'$'}Companion._();
            }
            """
        )
    }

    // TODO: Will be different
    @Disabled
    @Test
    fun `@DartConstructor call from dependency`() = assertCompile {
        kotlin(
            """
            import dart.typeddata.Float32Array

            fun main() {
                Float32Array.fromArray(arrayOf(0.1, 0.2, 0.3))
            }
            """
        )

        dart(
            """
            import "dart:typed_data";
            import "dart:core" as core;
            import "dart:core" hide List;
            void main() {
              Float32List.fromList(<double>[0.1, 0.2, 0.3]);
            }
            """
        )
    }

    // TODO: Will be different
    @Disabled
    @Test
    fun `@DartConstructor call from dependency with type arguments`() = assertCompile {
        kotlin(
            """
            import dart.typeddata.Float32Array

            fun main() {
                val array = Array.generate<Int>(10) { it }
            }
            """
        )

        dart(
            """
            import "dart:core" as core;
            import "dart:core" hide List;
            void main() {
              final core.List<int> array = core.List<int>.generate(10, (int it) {
                return it;
              });
            }
            """
        )
    }

    // TODO: Will be different
    @Disabled
    @Test
    fun `const @DartConstructor call from dependency`() = assertCompile {
        kotlin(
            """
            fun main() {
                val logging = @const Boolean.fromEnvironment("logging")
            }
            """
        )

        dart(
            """
            void main() {
              final bool logging = const bool.fromEnvironment("logging");
            }
            """
        )
    }
}

