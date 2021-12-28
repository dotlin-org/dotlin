/*
 * Copyright 2021 Wilko Manger
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

package compile

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Extension")
class Extension : BaseTest {
    @Test
    fun extension() = assertCompile {
        kotlin(
            """
            class Test

            fun Test.doIt() {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {}

            extension ${'$'}TestExtensions on Test {
              void doIt() {}
            }
            """
        )
    }

    @Test
    fun `extension getter`() = assertCompile {
        kotlin(
            """
            class Test

            val Test.number: Int get() = 3
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {}

            extension ${'$'}TestExtensions on Test {
              int get number {
                return 3;
              }
            }
            """
        )
    }

    @Test
    fun `extension getter and setter`() = assertCompile {
        kotlin(
            """
            class Test

            var Test.number: Int
                get() = 3
                set(value: Int) {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {}

            extension ${'$'}TestExtensions on Test {
              int get number {
                return 3;
              }

              void set number(int value) {}
            }
            """
        )
    }

    @Test
    fun `extension on type with type parameter`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T> Test<T>.doIt() {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test<T> {}

            extension ${'$'}TestExtensions<T> on Test<T> {
              void doIt() {}
            }
            """
        )
    }

    @Test
    fun `two extensions on type with type parameter`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T> Test<T>.doIt() {}

            fun <T> Test<T>.doItAgain() {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test<T> {}

            extension ${'$'}TestExtensions<T> on Test<T> {
              void doIt() {}
              void doItAgain() {}
            }
            """
        )
    }

    @Test
    fun `two extensions on type with type parameter with different type arguments`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <T> Test<T>.doIt() {}

            fun Test<Int>.doItAgain() {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test<T> {}

            extension ${'$'}TestExtensions<T> on Test<T> {
              void doIt() {}
            }

            extension ${'$'}TestIntExtensions on Test<int> {
              void doItAgain() {}
            }
            """
        )
    }

    @Test
    fun `extension with type parameter on type with type parameter`() = assertCompile {
        kotlin(
            """
            class Test<T>

            fun <A, B> Test<A>.doIt() {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test<T> {}

            extension ${'$'}TestExtensions<A> on Test<A> {
              void doIt<B>() {}
            }
            """
        )
    }

    @Test
    fun `extension on type from other file`() = assertCompile {
        kotlin(
            """
            fun String.titlecase() {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            extension ${'$'}KotlinStringExtensions on String {
              void titlecase() {}
            }
            """
        )
    }
}