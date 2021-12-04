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

@DisplayName("Compile: Statement")
class Statement : BaseTest {
    @Test
    fun `if else`() = assertCompile {
        kotlin(
            """
            fun main() {
                if (0 == 1) {
                    main()
                } else {
                    main()
                }
            }
            """
        )

        dart(
            """
            void main() {
              if (0 == 1) {
                main();
              } else {
                main();
              }
            }
            """
        )
    }

    @Test
    fun `if else if`() = assertCompile {
        kotlin(
            """
            fun main() {
                if (1 + 1 == 3) {
                    main()
                } else if (3 + 3 == 7){
                    main()
                }
            }
            """
        )

        dart(
            """
            void main() {
              if (1 + 1 == 3) {
                main();
              } else if (3 + 3 == 7) {
                main();
              }
            }
            """
        )
    }

    @Test
    fun `when`() = assertCompile {
        kotlin(
            """
            fun main() {
                when {
                    1 + 1 == 3 -> main()
                    3 + 3 == 7 -> main()
                }
            }
            """
        )

        dart(
            """
            void main() {
              if (1 + 1 == 3) {
                main();
              } else if (3 + 3 == 7) {
                main();
              }
            }
            """
        )
    }

    @Test
    fun `when with else`() = assertCompile {
        kotlin(
            """
            fun main(x: Int) {
                when {
                    1 + 1 == 3 -> main(2)
                    3 + 3 == 7 -> main(6)
                    else -> main(10)
                }
            }
            """
        )

        dart(
            """
            void main(int x) {
              if (1 + 1 == 3) {
                main(2);
              } else if (3 + 3 == 7) {
                main(6);
              } else {
                main(10);
              }
            }
            """
        )
    }

    @Test
    fun `when with subject`() = assertCompile {
        kotlin(
            """
            fun test(y: Int) = y * 3

            fun main(x: Int) {
                when(x) {
                    4 -> test(2)
                    12 -> test(6)
                }
            }
            """
        )

        dart(
            """
            int test(int y) {
              return y * 3;
            }

            void main(int x) {
              {
                final int tmp0_subject = x;
                if (tmp0_subject == 4) {
                  test(2);
                } else if (tmp0_subject == 12) {
                  test(6);
                }
              }
            }
            """
        )
    }
}