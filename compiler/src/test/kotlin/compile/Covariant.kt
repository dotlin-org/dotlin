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

@DisplayName("Compile: Covariant")
class Covariant : BaseTest {
    @Test
    fun `covariant variable initialization`() = assertCompile {
        kotlin(
            """
            class HasCovariant<in T>

            open class A
            class B : A()

            fun main() {
                val x: HasCovariant<B> = HasCovariant<A>()
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasCovariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            void main() {
              final HasCovariant<dynamic> x = HasCovariant<A>();
            }
            """
        )
    }

    @Test
    fun `covariant function generic bound`() = assertCompile {
        kotlin(
            """
            class HasCovariant<in T>

            open class A
            class B : A()

            fun <T : HasCovariant<T>> test() {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasCovariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            void test<T extends HasCovariant<dynamic>>() {}
            """
        )
    }

    @Test
    fun `covariant class generic bound`() = assertCompile {
        kotlin(
            """
            class HasCovariant<in T>

            open class A
            class B : A()

            class Test<T : HasCovariant<T>>
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasCovariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            @sealed
            class Test<T extends HasCovariant<dynamic>> {}
            """
        )
    }

    @Test
    fun `covariant class generic bound and invariant bound`() = assertCompile {
        kotlin(
            """
            class HasCovariant<in T>

            open class A
            class B : A()

            class Test<T : HasCovariant<T>, A : Number>
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasCovariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            @sealed
            class Test<T extends HasCovariant<dynamic>, A extends num> {}
            """
        )
    }

    @Test
    fun `covariant function parameter`() = assertCompile {
        kotlin(
            """
            class HasCovariant<in T>

            open class A
            class B : A()

            fun test(x: HasCovariant<A>) {}
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasCovariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            void test(HasCovariant<dynamic> x) {}
            """
        )
    }

    @Test
    fun `covariant class nested generic bound`() = assertCompile {
        kotlin(
            """
            class HasCovariant<in T>

            open class A
            class B : A()

            class Other<T>

            class Test<T : Other<HasCovariant<T>>>
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class HasCovariant<T> {}

            class A {}

            @sealed
            class B extends A {}

            @sealed
            class Other<T> {}

            @sealed
            class Test<T extends Other<HasCovariant<dynamic>>> {}
            """
        )
    }
}