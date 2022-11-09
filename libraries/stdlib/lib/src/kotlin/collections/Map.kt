/*
 * Copyright 2010-2020 JetBrains s.r.o.
 * Copyright 2021-2022 Wilko Manger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlin.collections

import kotlin.collections.Iterator as KtIterator

import kotlin.internal.PlatformDependent

/**
 * A collection that holds pairs of objects (keys and values) and supports efficiently retrieving
 * the value corresponding to each key. Map keys are unique; the map holds only one value for each key.
 * Methods in this interface support only read-only access to the map; read-write access is supported through
 * the [MutableMap] interface.
 * @param K the type of map keys. The map is invariant in its key type, as it
 *          can accept key as a parameter (of [containsKey] for example) and return it in [keys] set.
 * @param V the type of map values. The map is covariant in its value type.
 */
@DartLibrary("dart:core")
external interface Map<K, out V> {
    /*override*/ fun <RK, RV> cast(): Map<RK, RV>

    fun containsValue(value: Any?): Boolean

    fun containsKey(value: Any?): Boolean

    operator fun get(key: Any?): V?

    // Function because in Kotlin the equivalent property returns a MutableSet.
    @DartGetter
    fun entries(): Iterable<Map.Entry<K, V>>

    fun <K2, V2> map(
        @DartName("convert") transform: (key: K, value: V) -> Map<K2, V2>
    ): Map<K2, V2>

    fun forEach(action: (key: K, value: V) -> Unit): Unit

    // Function because in Kotlin the equivalent property returns a MutableSet.
    @DartGetter
    fun keys(): Iterable<K>

    @DartGetter
    fun values(): Iterable<V>

    @DartName("length")
    val size: Int

    @DartGetter
    fun isEmpty(): Boolean

    @DartGetter
    fun isNotEmpty(): Boolean

    @DartName("MapEntry")
    @DartLibrary("dart:core")
    interface Entry<out K, out V> {
        /**
         * Returns the key of this key/value pair.
         */
        val key: K

        /**
         * Returns the value of this key/value pair.
         */
        val value: V
    }
}

/**
 * A generic immutable collection that holds pairs of objects (keys and values) and supports efficiently retrieving
 * the value corresponding to each key. Map keys are unique; the map holds only one value for each key.
 * Methods in this interface support only read-only access to the immutable map.
 *
 * Implementors of this interface take responsibility to be immutable.
 * Once constructed they must contain the same elements in the same order.
 *
 * @param K the type of map keys. The map is invariant on its key type, as it
 *          can accept key as a parameter (of [containsKey] for example) and return it in [keys] set.
 * @param V the type of map values. The map is covariant on its value type.
 */
@DartName("Map")
@DartLibrary("dart:core")
external interface ImmutableMap<K, out V>: Map<K, V>

/**
 * A modifiable collection that holds pairs of objects (keys and values) and supports efficiently retrieving
 * the value corresponding to each key. Map keys are unique; the map holds only one value for each key.
 * @param K the type of map keys. The map is invariant in its key type.
 * @param V the type of map values. The mutable map is invariant in its value type.
 */
@DartName("Map")
@DartLibrary("dart:core")
external interface MutableMap<K, V> : Map<K, V> {
    override fun <RK, RV> cast(): MutableMap<RK, RV>

    operator fun set(key: K, value: V): V

    fun addEntries(@DartName("newEntries") entries: Iterable<Map.Entry<K, V>>): Unit

    @DartName("update")
    fun compute(key: K, @DartIndex(2) ifAbsent: (() -> V)? = null, @DartName("update") compute: (value: V) -> V): Unit

    @DartName("updateAll")
    fun computeAll(compute: (key: K, value: V) -> V)

    @DartName("removeWhere")
    fun removeIf(@DartName("test") predicate: (key: K, value: V) -> Boolean): Unit

    fun putIfAbsent(key: K, ifAbsent: () -> V): V

    fun addAll(other: Map<K, V>): Unit

    fun remove(key: Any?): V?

    fun clear(): Unit

    /**
     * Represents a key/value pair held by a [MutableMap].
     */
    interface MutableEntry<K, V> : Map.Entry<K, V> {
        /**
         * Changes the value associated with the key of this entry.
         *
         * @return the previous value corresponding to the key.
         */
        fun setValue(newValue: V): V
    }
}
