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

@DisplayName("Compile: Object")
class Object : BaseTest {
    private val instance = "\$instance"

    @Test
    fun `object`() = assertCompile {
        kotlin(
            """
            object Test
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
              static final Test $instance = Test._();
            }
            """
        )
    }

    @Test
    fun `object with method`() = assertCompile {
        kotlin(
            """
            object Test {
                fun compute(): Int {
                    return 343
                }
            }
            """
        )

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              Test._() : super();
              @nonVirtual
              int compute() {
                return 343;
              }
            
              static final Test $instance = Test._();
            }
            """
        )
    }

    @Test
    fun `companion object`() = assertCompile {
        kotlin(
            """
            class Test {
                companion object
            }
            """
        )

        @Suppress("LocalVariableName")
        val TestCompanion = "\$TestCompanion"
        val companion = "\$companion"

        dart(
            """
            import 'package:meta/meta.dart';

            @sealed
            class Test {
              static final $TestCompanion $companion = $TestCompanion.$instance;
            }
            
            @sealed
            class $TestCompanion {
              $TestCompanion._() : super();
              static final $TestCompanion $instance = $TestCompanion._();
            }
            """
        )
    }
}