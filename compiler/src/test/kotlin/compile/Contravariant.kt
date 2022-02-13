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

package compile

import BaseTest
import assertCompile
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Compile: Contravariant")
class Contravariant : BaseTest {
    @Test
    fun `variable initialization with type that has contravariant type parameter`() = assertCompile {
        kotlin(
            """
            class HasContravariant<in T>

            open class A
            class B : A()

            fun main() {
                val x: HasContravariant<B> = HasContravariant<A>()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasContravariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            void main() {
              final HasContravariant<dynamic> x = HasContravariant<A>();
            }
            """
        )
    }

    @Test
    fun `function with generic type with bound that has contravariant type parameter`() = assertCompile {
        kotlin(
            """
            class HasContravariant<in T>

            open class A
            class B : A()

            fun <T : HasContravariant<T>> test() {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasContravariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            void test<T extends HasContravariant<dynamic>>() {}
            """
        )
    }

    @Test
    fun `class with generic type with bound that has contravariant type parameter`() = assertCompile {
        kotlin(
            """
            class HasContravariant<in T>

            open class A
            class B : A()

            class Test<T : HasContravariant<T>>
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasContravariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            @sealed
            class Test<T extends HasContravariant<dynamic>> {}
            """
        )
    }

    @Test
    fun `class with nested generic type with bound that has contravariant type parameter`() = assertCompile {
        kotlin(
            """
            class HasContravariant<in T>

            open class A
            class B : A()

            class Other<T>

            class Test<T : Other<HasContravariant<T>>>
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasContravariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            @sealed
            class Other<T> {}

            @sealed
            class Test<T extends Other<HasContravariant<dynamic>>> {}
            """
        )
    }

    @Test
    fun `class with regular generic type and generic type with bound that has contravariant type parameter`() =
        assertCompile {
            kotlin(
                """
                class HasContravariant<in T>

                open class A
                class B : A()

                class Test<T : HasContravariant<T>, A : Number>
                """
            )

            dart(
                """
                import 'package:meta/meta.dart';

                @sealed
                class HasContravariant<T> {}

                class A {}

                @sealed
                class B extends A {}

                @sealed
                class Test<T extends HasContravariant<dynamic>, A extends num> {}
                """
            )
    }

    @Test
    fun `function with value parameter that has a type with a contravariant type parameter`() = assertCompile {
        kotlin(
            """
            class HasContravariant<in T>

            open class A
            class B : A()

            fun test(x: HasContravariant<A>) {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasContravariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            void test(HasContravariant<dynamic> x) {}
            """
        )
    }
}