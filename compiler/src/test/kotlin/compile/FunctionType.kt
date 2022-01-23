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
import DefaultValue
import assertCompile
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Compile: Function Type")
class FunctionType : BaseTest {
    @Test
    fun `function type`() = assertCompile {
        kotlin(
            """
            val function: () -> Int = { 0 }
            """
        )
        dart(
            """
            import 'package:meta/meta.dart';

            final int Function() function = () {
              return 0;
            };
            """
        )
    }

    @Test
    fun `function type with Unit return type`() = assertCompile {
        kotlin(
            """
            val function: () -> Unit = {}
            """
        )
        dart(
            """
            import 'package:meta/meta.dart';

            final void Function() function = () {
              return;
            };
            """
        )
    }

    @Test
    fun `function type with value parameters`() = assertCompile {
        kotlin(
            """
            val function: (Int, Int) -> Unit = { a, b -> }
            """
        )
        dart(
            """
            import 'package:meta/meta.dart';

            final void Function(
              int,
              int,
            ) function = (
              int a,
              int b,
            ) {
              return;
            };
            """
        )
    }

    @Test
    fun `generic function type with Unit return type`() = assertCompile {
        kotlin(
            """
            fun <T> test(block: (T) -> Unit) {}
            """
        )
        dart(
            """
            import 'package:meta/meta.dart';

            void test<T>(void Function(T) block) {}
            """
        )
    }

    @Test
    fun `function type with named value parameters`() = assertCompile {
        kotlin(
            """
            val function: (first: Int, second: Int) -> Unit = { a, b -> }
            """
        )
        dart(
            """
            import 'package:meta/meta.dart';

            final void Function(
              int first,
              int second,
            ) function = (
              int a,
              int b,
            ) {
              return;
            };
            """
        )
    }
}