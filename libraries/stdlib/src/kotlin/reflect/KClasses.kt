/*
 * Copyright 2010-2019 JetBrains s.r.o.
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
// TODO
/*@file:Suppress("UNCHECKED_CAST")

package kotlin.reflect

import kotlin.internal.LowPriorityInOverloadResolution

/**
 * Casts the given [value] to the class represented by this [KClass] object.
 * Throws an exception if the value is `null` or if it is not an instance of this class.
 *
 * This is an experimental function that behaves as a similar function from kotlin.reflect.full on JVM.
 *
 * @see [KClass.isInstance]
 * @see [KClass.safeCast]
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@LowPriorityInOverloadResolution
fun <T : Any> KClass<T>.cast(value: Any?): T {
    if (!isInstance(value)) throw ClassCastException("Value cannot be cast to $qualifiedOrSimpleName")
    return value as T
}

// TODO: replace with qualifiedName when it is fully supported in K/JS
internal expect val KClass<*>.qualifiedOrSimpleName: String?

/**
 * Casts the given [value] to the class represented by this [KClass] object.
 * Returns `null` if the value is `null` or if it is not an instance of this class.
 *
 * This is an experimental function that behaves as a similar function from kotlin.reflect.full on JVM.
 *
 * @see [KClass.isInstance]
 * @see [KClass.cast]
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@LowPriorityInOverloadResolution
fun <T : Any> KClass<T>.safeCast(value: Any?): T? {
    return if (isInstance(value)) value as T else null
}
*/