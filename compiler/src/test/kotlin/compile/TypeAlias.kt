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

@DisplayName("Compile: Type Alias")
class TypeAlias : BaseTest {
    @Test
    fun `class type alias`() = assertCompile {
        kotlin(
            """
            class X

            typealias Y = X
            """
        )
        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class X {}

            typedef Y = X;
            """
        )
    }

    @Test
    fun `class type alias with type parameter`() = assertCompile {
        kotlin(
      """
            class X<T>

            typealias Y<Z> = X<Z>
            """
        )
        dart(
        """
            import 'package:meta/meta.dart';
            
            @sealed
            class X<T> {}
            
            typedef Y<Z> = X<Z>;
            """
        )
    }

    @Test
    fun `function type alias`() = assertCompile {
        kotlin(
            """
            typealias Processor= (Int) -> Unit
            """
        )
        dart(
            """
            import 'package:meta/meta.dart';
            
            typedef Processor = void Function(int);
            """
        )
    }

    @Test
    fun `function type alias with named parameter`() = assertCompile {
        kotlin(
            """
            typealias Processor= (number: Int) -> Unit
            """
        )
        dart(
            """
            import 'package:meta/meta.dart';
            
            typedef Processor = void Function(int number);
            """
        )
    }

    @Test
    fun `function type alias with type parameter`() = assertCompile {
        kotlin(
            """
            typealias Predicate<T> = (T) -> Boolean
            """
        )
        dart(
            """
            import 'package:meta/meta.dart';
            
            typedef Predicate<T> = bool Function(T);
            """
        )
    }
}