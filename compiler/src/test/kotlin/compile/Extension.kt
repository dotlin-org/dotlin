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

    @Suppress("PropertyName")
    val TestExt = "\$TestExt"

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
            class Test {}
            
            extension $TestExt on Test {
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
            class Test {}
            
            extension $TestExt on Test {
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
            class Test {}

            extension $TestExt on Test {
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
            class Test<T> {}

            extension $TestExt<T> on Test<T> {
              void doIt() {}
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
            class Test<T> {}

            extension $TestExt<A> on Test<A> {
              void doIt<B>() {}
            }
            """
        )
    }
}