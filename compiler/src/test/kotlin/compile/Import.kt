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
import org.junit.jupiter.api.Test

@DisplayName("Compile: Import")
class Import : BaseTest {
    @Test
    fun `import as`() = assertCompile {
        kotlin(
            """
            import kotlin.Int as Int64

            fun main() {
                val x: Int64 = 3
            }
            """
        )

        dart(
            """
            import 'dart:core' as Int64 show int;
            import 'dart:core' hide int;
            import 'package:meta/meta.dart';

            void main() {
              final Int64.int x = 3;
            }
            """
        )
    }

    @Test
    fun `using class from Dart's typed_data`() = assertCompile {
        kotlin(
            """
            import dart.typeddata.ByteData

            fun main() {
                val buffer = ByteData(8)
            }
            """
        )

        dart(
            """
            import 'dart:typed_data';
            import 'package:meta/meta.dart';

            void main() {
              final ByteData buffer = ByteData(8);
            }
            """
        )
    }
}