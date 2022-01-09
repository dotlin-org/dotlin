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
            import 'package:meta/meta.dart';

            @sealed
            class TestIterator implements Iterator<int> {
              @nonVirtual
              void doImportantThing() {}
              @override
              int next() {
                this.doImportantThing();
                return this.current = 3;
              }

              @override
              bool hasNext() {
                return true;
              }

              @nonVirtual
              @override
              late int _${'$'}currentBackingField;
              @nonVirtual
              @override
              int get current => this._${'$'}currentBackingField;
              @protected
              @nonVirtual
              @override
              void set current(int value) => this._${'$'}currentBackingField = value;
              @nonVirtual
              @override
              bool moveNext() {
                final bool hasNext = this.hasNext();
                if (hasNext) {
                  this.current = this.next();
                }
                return hasNext;
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
            import 'package:meta/meta.dart';

            @sealed
            class TestIterator implements ListIterator<int> {
              @nonVirtual
              void doImportantThing() {}
              @override
              int previous() {
                this.doImportantThing();
                return this.current = 123;
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
                return this.current = 3;
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
              @override
              late int _${'$'}currentBackingField;
              @nonVirtual
              @override
              int get current => this._${'$'}currentBackingField;
              @protected
              @nonVirtual
              @override
              void set current(int value) => this._${'$'}currentBackingField = value;
              @nonVirtual
              @override
              bool moveNext() {
                final bool hasNext = this.hasNext();
                if (hasNext) {
                  this.current = this.next();
                }
                return hasNext;
              }

              @nonVirtual
              @override
              bool movePrevious() {
                final bool hasPrevious = this.hasPrevious();
                if (hasPrevious) {
                  this.current = this.previous();
                }
                return hasPrevious;
              }
            }
            """
        )
    }
}