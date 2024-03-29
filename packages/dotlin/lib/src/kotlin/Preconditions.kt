/*
 * Copyright 2010-2018 JetBrains s.r.o.
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

package kotlin

// TODO: Contracts

/**
 * Throws an [IllegalArgumentException] if the [value] is false.
 *
 * @sample samples.misc.Preconditions.failRequireWithLazyMessage
 */
//@kotlin.internal.InlineOnly
inline fun require(value: Boolean): Unit {
    //contract {
    //    returns() implies value
    //}
    require(value) { "Failed requirement." }
}

/**
 * Throws an [IllegalArgumentException] with the result of calling [lazyMessage] if the [value] is false.
 *
 * @sample samples.misc.Preconditions.failRequireWithLazyMessage
 */
//@kotlin.internal.InlineOnly
inline fun require(value: Boolean, lazyMessage: () -> Any): Unit {
    //contract {
    //    returns() implies value
    //}
    if (!value) {
        val message = lazyMessage()
        throw ArgumentError(message.toString())
    }
}

/**
 * Throws an [IllegalArgumentException] if the [value] is null. Otherwise returns the not null value.
 */
//@kotlin.internal.InlineOnly
inline fun <T : Any> requireNotNull(value: T?): T {
    //contract {
    //    returns() implies (value != null)
    //}
    return requireNotNull(value) { "Required value was null." }
}

/**
 * Throws an [IllegalArgumentException] with the result of calling [lazyMessage] if the [value] is null. Otherwise
 * returns the not null value.
 *
 * @sample samples.misc.Preconditions.failRequireNotNullWithLazyMessage
 */
//@kotlin.internal.InlineOnly
inline fun <T : Any> requireNotNull(value: T?, lazyMessage: () -> Any): T {
    //contract {
    //    returns() implies (value != null)
    //}

    if (value == null) {
        val message = lazyMessage()
        throw ArgumentError(message.toString())
    } else {
        return value
    }
}

/**
 * Throws an [IllegalStateException] if the [value] is false.
 *
 * @sample samples.misc.Preconditions.failCheckWithLazyMessage
 */
//@kotlin.internal.InlineOnly
inline fun check(value: Boolean): Unit {
    //contract {
    //    returns() implies value
    //}
    check(value) { "Check failed." }
}

/**
 * Throws an [IllegalStateException] with the result of calling [lazyMessage] if the [value] is false.
 *
 * @sample samples.misc.Preconditions.failCheckWithLazyMessage
 */
//@kotlin.internal.InlineOnly
inline fun check(value: Boolean, lazyMessage: () -> Any): Unit {
    //contract {
    //    returns() implies value
    //}
    if (!value) {
        val message = lazyMessage()
        throw StateError(message.toString())
    }
}

/**
 * Throws an [IllegalStateException] if the [value] is null. Otherwise
 * returns the not null value.
 *
 * @sample samples.misc.Preconditions.failCheckWithLazyMessage
 */
//@kotlin.internal.InlineOnly
inline fun <T : Any> checkNotNull(value: T?): T {
    //contract {
    //    returns() implies (value != null)
    //}
    return checkNotNull(value) { "Required value was null." }
}

/**
 * Throws an [IllegalStateException] with the result of calling [lazyMessage]  if the [value] is null. Otherwise
 * returns the not null value.
 *
 * @sample samples.misc.Preconditions.failCheckWithLazyMessage
 */
//@kotlin.internal.InlineOnly
inline fun <T : Any> checkNotNull(value: T?, lazyMessage: () -> Any): T {
    //contract {
    //    returns() implies (value != null)
    //}

    if (value == null) {
        val message = lazyMessage()
        throw StateError(message.toString())
    } else {
        return value
    }
}


/**
 * Throws an [ArgumentError] with the given [message].
 *
 * @sample samples.misc.Preconditions.failWithError
 */
//@kotlin.internal.InlineOnly
inline fun error(message: Any): Nothing = throw StateError(message.toString())
