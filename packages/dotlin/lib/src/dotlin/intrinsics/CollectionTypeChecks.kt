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

//#region Collection checks
@DartExtensionName("DotlinTypeIntrinsics")
inline /*internal*/ fun <T> Any?.isCollection(): Boolean =
    this is Collection<T> || this is AnyList<T> || this is AnySet<T>

@DartExtensionName("DotlinTypeIntrinsics")
inline /*internal*/ fun <T> Any?.isMutableCollection(): Boolean =
    this is MutableCollection<T> || isMutableList<T>() || isMutableSet<T>()
//#endregion

//#region List checks
@DartExtensionName("DotlinTypeIntrinsics")
inline /*internal*/ fun <T> Any?.isImmutableList(): Boolean = this is AnyList<T> &&
        (this is ImmutableListMarker || (!isWriteable() && !isGrowable()))

@DartExtensionName("DotlinTypeIntrinsics")
inline /*internal*/ fun <T> Any?.isWriteableList(): Boolean = this is AnyList<T> &&
        (this is WriteableListMarker || isWriteable())

@DartExtensionName("DotlinTypeIntrinsics")
inline /*internal*/ fun <T> Any?.isFixedSizeList(): Boolean = this is AnyList<T> &&
        (this is FixedSizeListMarker || (isWriteable() && !isGrowable()))

@DartExtensionName("DotlinTypeIntrinsics")
inline /*internal*/ fun <T> Any?.isMutableList(): Boolean = this is AnyList<T> &&
        (this is MutableListMarker || (isWriteable() && isGrowable()))
//#endregion

//#region Set checks
@DartExtensionName("DotlinTypeIntrinsics")
inline /*internal*/ fun <T> Any?.isImmutableSet(): Boolean = this is AnySet<T> &&
        (this is ImmutableSetMarker || !isGrowable())

@DartExtensionName("DotlinTypeIntrinsics")
inline /*internal*/ fun <T> Any?.isMutableSet(): Boolean = this is AnySet<T> &&
        (this is MutableSetMarker || isGrowable())
//#region

//#region Map checks
inline /*internal*/ fun <K, V> Any?.isImmutableMap(): Boolean = this is AnyMap<K, V> &&
        (this is ImmutableMapMarker || !isGrowable())

@DartExtensionName("DotlinTypeIntrinsics")
inline /*internal*/ fun <K, V> Any?.isMutableMap(): Boolean = this is AnyMap<K, V> &&
        (this is MutableMapMarker || isGrowable())
//#region

@PublishedApi
@DartExtensionName("ListTypes")
inline internal fun AnyList<dynamic>.isGrowable(): Boolean {
    try {
        add(removeLast())
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
@DartExtensionName("ListTypes")
inline internal fun AnyList<dynamic>.isWriteable(): Boolean {
    try {
        if (isEmpty()) {
            sort()
        } else {
            val item = this[0]
            this[0] = item
        }
    } catch (e: UnsupportedError) {
        return false
    }

    return true
}

@PublishedApi
@DartExtensionName("SetTypes")
inline internal fun AnySet<dynamic>.isGrowable(): Boolean {
    try {
        retainIf { true }
    } catch (e: UnsupportedError) {
        return false
    }

    return true
}

@PublishedApi
@DartExtensionName("MapTypes")
inline internal fun AnyMap<dynamic, dynamic>.isGrowable(): Boolean {
    try {
        // TODO: Support `_` param name
        removeIf { key, value -> false }
    } catch (e: UnsupportedError) {
        return false
    }

    return true
}