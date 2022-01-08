/*
 * Copyright 2010-2018 JetBrains s.r.o.
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

package kotlin

//import kotlin.contracts.* TODO: Contracts

/**
 * An exception is thrown to indicate that a method body remains to be implemented.
 */
// TODO: Map to UnimplementedError
//class NotImplementedError(message: String = "An operation is not implemented.") : Error(message)

/**
 * Always throws [NotImplementedError] stating that operation is not implemented.
 */

//@kotlin.internal.InlineOnly TODO
inline fun TODO(): Nothing = throw NotImplementedError()

/**
 * Always throws [NotImplementedError] stating that operation is not implemented.
 *
 * @param reason a string explaining why the implementation is missing.
 */
//@kotlin.internal.InlineOnly TODO
inline fun TODO(reason: String): Nothing = throw NotImplementedError("An operation is not implemented: $reason")

/**
 * Calls the specified function [block] and returns its result.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#run).
 */
//@kotlin.internal.InlineOnly
inline fun <R> run(block: () -> R): R {
    /*contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }*/
    return block()
}

/**
 * Calls the specified function [block] with `this` value as its receiver and returns its result.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#run).
 */
//@kotlin.internal.InlineOnly
inline fun <T, R> T.run(block: T.() -> R): R {
    /*contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }*/
    return block()
}

/**
 * Calls the specified function [block] with the given [receiver] as its receiver and returns its result.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#with).
 */
//@kotlin.internal.InlineOnly TODO
inline fun <T, R> with(receiver: T, block: T.() -> R): R {
    /*contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }*/
    return receiver.block()
}

/**
 * Calls the specified function [block] with `this` value as its receiver and returns `this` value.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#apply).
 */
//@kotlin.internal.InlineOnly TODO
inline fun <T> T.apply(block: T.() -> Unit): T {
    /*contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }*/
    block()
    return this
}

/**
 * Calls the specified function [block] with `this` value as its argument and returns `this` value.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#also).
 */
//@kotlin.internal.InlineOnly TODO
@SinceKotlin("1.1")
inline fun <T> T.also(block: (T) -> Unit): T {
    /*contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }*/
    block(this)
    return this
}

/**
 * Calls the specified function [block] with `this` value as its argument and returns its result.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#let).
 */
//@kotlin.internal.InlineOnly TODO
inline fun <T, R> T.let(block: (T) -> R): R {
    /*contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }*/
    return block(this)
}

/**
 * Returns `this` value if it satisfies the given [predicate] or `null`, if it doesn't.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#takeif-and-takeunless).
 */
//@kotlin.internal.InlineOnly TODO
@SinceKotlin("1.1")
inline fun <T> T.takeIf(predicate: (T) -> Boolean): T? {
    /*contract {
        callsInPlace(predicate, InvocationKind.EXACTLY_ONCE)
    }*/
    return if (predicate(this)) this else null
}

/**
 * Returns `this` value if it _does not_ satisfy the given [predicate] or `null`, if it does.
 *
 * For detailed usage information see the documentation for [scope functions](https://kotlinlang.org/docs/reference/scope-functions.html#takeif-and-takeunless).
 */
//@kotlin.internal.InlineOnly TODO
@SinceKotlin("1.1")
inline fun <T> T.takeUnless(predicate: (T) -> Boolean): T? {
    /*contract {
        callsInPlace(predicate, InvocationKind.EXACTLY_ONCE)
    }*/
    return if (!predicate(this)) this else null
}

/**
 * Executes the given function [action] specified number of [times].
 *
 * A zero-based index of current iteration is passed as a parameter to [action].
 *
 * @sample samples.misc.ControlFlow.repeat
 */
//@kotlin.internal.InlineOnly TODO
inline fun repeat(times: Int, action: (Int) -> Unit) {
    //contract { callsInPlace(action) }

    for (index in 0 until times) {
        action(index)
    }
}
