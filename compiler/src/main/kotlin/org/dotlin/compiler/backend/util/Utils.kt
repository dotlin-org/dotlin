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

package org.dotlin.compiler.backend.util

import kotlin.reflect.KProperty

fun Boolean?.falseIfNull() = this ?: false

fun <T> MutableList<T>.replace(old: T, new: T) {
    add(indexOf(old), new)
    remove(old)
}

fun <T> Iterable<T>.toPair(): Pair<T, T> {
    if (this.count() != 2) throw IllegalStateException("There must be exactly 2 elements to convert to a Pair")

    return first() to last()
}

fun String.sentenceCase() = this[0].uppercaseChar() + drop(1)

operator fun <E> List<E>.component6(): E = this[5]
operator fun <E> List<E>.component7(): E = this[6]
operator fun <E> List<E>.component8(): E = this[7]
operator fun <E> List<E>.component9(): E = this[8]
operator fun <E> List<E>.component10(): E = this[9]

fun <T, V, R> T.runWith(value: V, block: T.(V) -> R): R = block(this, value)

class LazyVar<T : Any>(private val initializer: () -> T) {
    var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = value ?: initializer().also { value = it }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}