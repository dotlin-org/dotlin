/*
 * Copyright 2022 Wilko Manger
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

package dotlin.intrinsics

// TODO: @DartPublic

//#region Collection checks
inline /*internal*/ fun <T> isCollection(obj: Any?): Boolean =
    obj is Collection<T> || obj is AnyList<T> || obj is AnySet<T>

inline /*internal*/ fun <T> isMutableCollection(obj: Any?): Boolean =
    obj is MutableCollection<T> || isMutableList<T>(obj) || isMutableSet<T>(obj)
//#endregion

//#region List checks
inline /*internal*/ fun <T> isImmutableList(obj: Any?): Boolean = obj is AnyList<T> &&
        (obj is ImmutableListMarker || (!isWriteable(obj) && !isGrowable(obj)))

inline /*internal*/ fun <T> isWriteableList(obj: Any?): Boolean = obj is AnyList<T> &&
        (obj is WriteableListMarker || isWriteable(obj))

inline /*internal*/ fun <T> isFixedSizeList(obj: Any?): Boolean = obj is AnyList<T> &&
        (obj is FixedSizeListMarker || (isWriteable(obj) && !isGrowable(obj)))

inline /*internal*/ fun <T> isMutableList(obj: Any?): Boolean = obj is AnyList<T> &&
        (obj is MutableListMarker || (isWriteable(obj) && isGrowable(obj)))
//#endregion

//#region Set checks
inline /*internal*/ fun <T> isImmutableSet(obj: Any?): Boolean = obj is AnySet<T> &&
        (obj is ImmutableSetMarker || !isGrowable(obj))

inline /*internal*/ fun <T> isMutableSet(obj: Any?): Boolean = obj is AnySet<T> &&
        (obj is MutableSetMarker || isGrowable(obj))
//#region

//#region Map checks
inline /*internal*/ fun <K, V> isImmutableMap(obj: Any?): Boolean = obj is AnyMap<K, V> &&
        (obj is ImmutableMapMarker || !isGrowable(obj))

inline /*internal*/ fun <K, V> isMutableMap(obj: Any?): Boolean = obj is AnyMap<K, V> &&
        (obj is MutableMapMarker || isGrowable(obj))
//#region

@PublishedApi
inline internal fun isGrowable(list: AnyList<dynamic>): Boolean {
    try {
        list.add(list.removeLast())
    } catch (e: UnsupportedError) {
        return false
    } catch (e: RangeError) {
        // The collection is empty, but can be mutated
        // (otherwise [UnsupportedError] would've been thrown).
        return true
    }

    return true
}

@PublishedApi
inline internal fun isWriteable(list: AnyList<dynamic>): Boolean {
    try {
        if (list.isEmpty()) {
            list.sort()
        } else {
            val item = list[0]
            list[0] = item
        }
    } catch (e: UnsupportedError) {
        return false
    }

    return true
}

@PublishedApi
inline internal fun isGrowable(set: AnySet<dynamic>): Boolean {
    try {
        set.retainIf { true }
    } catch (e: UnsupportedError) {
        return false
    }

    return true
}

@PublishedApi
inline internal fun isGrowable(map: AnyMap<dynamic, dynamic>): Boolean {
    try {
        // TODO: Support `_` param name
        map.removeIf { key, value -> false }
    } catch (e: UnsupportedError) {
        return false
    }

    return true
}