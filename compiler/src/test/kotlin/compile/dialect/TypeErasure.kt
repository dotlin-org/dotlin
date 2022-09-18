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

@DisplayName("Compile: Dialect: Type Erasure")
class TypeErasure : BaseTest {
    @Test
    fun `type check on type with generic parameters`() = assertCompile {
        kotlin(
            """
            class MyClass<T>

            fun exec(something: MyClass<String>) {}

            fun test(arg: Any) {
                if (arg is MyClass<String>) {
                    exec(arg)
                }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class MyClass<T> {}
            
            void exec(MyClass<String> something) {}
            void test(Object arg) {
              if (arg is MyClass<String>) {
                exec(arg as MyClass<String>);
              }
            }
            """
        )
    }

    @Test
    fun `type check on type parameter`() = assertCompile {
        kotlin(
            """
            fun exec() {}

            fun <T> test(arg: Any) {
                if (arg is T) {
                    exec()
                }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            void exec() {}
            void test<T>(Object arg) {
              if (arg is T) {
                exec();
              }
            }
            """
        )
    }

    @Test
    fun `type check on type parameter used as type argument`() = assertCompile {
        kotlin(
            """
            class MyClass<T>

            fun exec() {}

            fun <T> test(arg: Any) {
                if (arg is MyClass<T>) {
                    exec()
                }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class MyClass<T> {}

            void exec() {}
            void test<T>(Object arg) {
              if (arg is MyClass<T>) {
                exec();
              }
            }
            """
        )
    }
}