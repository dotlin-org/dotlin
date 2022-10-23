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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Dialect: Constructor")
class Constructor : BaseTest {
    @Test
    fun `@DartConstructor in external companion object`() = assertCompile {
        kotlin(
            """
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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

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

    @Test
    fun `const @DartConstructor call in nested class`() = assertCompile {
        kotlin(
            """
            external class TestContainer {
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
            import 'package:meta/meta.dart';

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
            import 'package:meta/meta.dart';

            void main() {
              Test.create(10, 'abc');
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
            interface Marker

            class MarkerImplementor : Marker

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
            import 'package:meta/meta.dart';
            
            abstract class Marker {}

            @sealed
            class MarkerImplementor implements Marker {}

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
}

