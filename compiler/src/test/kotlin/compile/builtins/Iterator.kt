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

package compile.builtins

import BaseTest
import assertCompile
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Compile: Iterator")
class Iterator : BaseTest {
    @Test
    fun `Iterator subtype`() = assertCompile {
        kotlin(
            """
            class TestIterator : Iterator<Int> {
                fun doImportantThing() {}

                override fun next(): Int {
                    doImportantThing()
                    return 3
                }

                override fun hasNext() = true
            }
            """
        )

        dart(
            """
            import 'dart:core' as core;
            import 'dart:core' hide Iterator;
            import 'package:meta/meta.dart';

            @sealed
            class TestIterator implements Iterator<int>, core.Iterator<int> {
              @nonVirtual
              void doImportantThing() {}
              @override
              int next() {
                this.doImportantThing();
                return this._current = 3;
              }

              @override
              bool hasNext() {
                return true;
              }

              @nonVirtual
              late int _${'$'}currentBackingField;
              @nonVirtual
              @override
              int get current => this._${'$'}currentBackingField;
              @nonVirtual
              void set _current(int value) => this._${'$'}currentBackingField = value;
              @nonVirtual
              @override
              bool moveNext() {
                final bool tmp0_hasNext = this.hasNext();
                if (tmp0_hasNext) {
                  this._current = this.next();
                }
                return tmp0_hasNext;
              }
            }
            """
        )
    }

    @Test
    fun `ListIterator subtype`() = assertCompile {
        kotlin(
            """
            class TestIterator : ListIterator<Int> {
                fun doImportantThing() {}

                override fun previous(): Int{
                    doImportantThing()
                    return 123
                }

                override fun hasPrevious() = true
                override fun previousIndex() = 0

                override fun next(): Int{
                    doImportantThing()
                    return 3
                }

                override fun hasNext() = true
                override fun nextIndex() = 0
            }
            """
        )

        dart(
            """
            import 'dart:core' as core;
            import 'dart:core' hide BidirectionalIterator;
            import 'package:meta/meta.dart';

            @sealed
            class TestIterator
                implements ListIterator<int>, core.BidirectionalIterator<int> {
              @nonVirtual
              void doImportantThing() {}
              @override
              int previous() {
                this.doImportantThing();
                return this._current = 123;
              }

              @override
              bool hasPrevious() {
                return true;
              }

              @override
              int previousIndex() {
                return 0;
              }

              @override
              int next() {
                this.doImportantThing();
                return this._current = 3;
              }

              @override
              bool hasNext() {
                return true;
              }

              @override
              int nextIndex() {
                return 0;
              }

              @nonVirtual
              late int _${'$'}currentBackingField;
              @nonVirtual
              @override
              int get current => this._${'$'}currentBackingField;
              @nonVirtual
              void set _current(int value) => this._${'$'}currentBackingField = value;
              @nonVirtual
              @override
              bool moveNext() {
                final bool tmp0_hasNext = this.hasNext();
                if (tmp0_hasNext) {
                  this._current = this.next();
                }
                return tmp0_hasNext;
              }

              @nonVirtual
              @override
              bool movePrevious() {
                final bool tmp0_hasPrevious = this.hasPrevious();
                if (tmp0_hasPrevious) {
                  this._current = this.previous();
                }
                return tmp0_hasPrevious;
              }
            }
            """
        )
    }

    @Test
    fun `Iterator subtype with middle interface`() = assertCompile {
        kotlin(
            """
            interface IntIterator : Iterator<Int> {
                override fun next(): Int = nextInt()
                fun nextInt(): Int
            }

            class TestIterator : IntIterator {
                override fun nextInt(): Int = 0

                override fun hasNext() = true
            }
            """
        )

        dart(
            """
            import 'dart:core' as core;
            import 'dart:core' hide Iterator;
            import 'package:meta/meta.dart';

            abstract class IntIterator implements Iterator<int>, core.Iterator<int> {
              @override
              int next() {
                return this._current = this.nextInt();
              }

              int nextInt();
              @nonVirtual
              late int _${'$'}currentBackingField;
              @nonVirtual
              @override
              int get current => this._${'$'}currentBackingField;
              @nonVirtual
              void set _current(int value) => this._${'$'}currentBackingField = value;
              @nonVirtual
              @override
              bool moveNext() {
                final bool tmp0_hasNext = this.hasNext();
                if (tmp0_hasNext) {
                  this._current = this.next();
                }
                return tmp0_hasNext;
              }
            }

            @sealed
            class TestIterator implements IntIterator, core.Iterator<int> {
              @override
              int nextInt() {
                return 0;
              }

              @override
              bool hasNext() {
                return true;
              }

              @override
              int next() {
                return this._current = this.nextInt();
              }

              @nonVirtual
              late int _${'$'}currentBackingField;
              @nonVirtual
              @override
              int get current => this._${'$'}currentBackingField;
              @nonVirtual
              void set _current(int value) => this._${'$'}currentBackingField = value;
              @nonVirtual
              @override
              bool moveNext() {
                final bool tmp0_hasNext = this.hasNext();
                if (tmp0_hasNext) {
                  this._current = this.next();
                }
                return tmp0_hasNext;
              }
            }
            """
        )
    }

    @Test
    fun `Iterator subtype with middle open class`() = assertCompile {
        kotlin(
            """
            open class IntIterator : Iterator<Int> {
                override fun hasNext() = true
                final override fun next(): Int = nextInt()
                open fun nextInt(): Int = -1
            }

            class TestIterator : IntIterator() {
                override fun hasNext() = true
                override fun nextInt(): Int = 0
            }
            """
        )

        dart(
            """
            import 'dart:core' as core;
            import 'dart:core' hide Iterator;
            import 'package:meta/meta.dart';

            class IntIterator implements Iterator<int>, core.Iterator<int> {
              @override
              bool hasNext() {
                return true;
              }

              @nonVirtual
              @override
              int next() {
                return this._current = this.nextInt();
              }

              int nextInt() {
                return -1;
              }

              @nonVirtual
              late int _${'$'}currentBackingField;
              @nonVirtual
              @override
              int get current => this._${'$'}currentBackingField;
              @nonVirtual
              void set _current(int value) => this._${'$'}currentBackingField = value;
              @nonVirtual
              @override
              bool moveNext() {
                final bool tmp0_hasNext = this.hasNext();
                if (tmp0_hasNext) {
                  this._current = this.next();
                }
                return tmp0_hasNext;
              }
            }

            @sealed
            class TestIterator extends IntIterator {
              @override
              bool hasNext() {
                return true;
              }

              @override
              int nextInt() {
                return 0;
              }
            }
            """
        )
    }
}