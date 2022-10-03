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

package dotlin.reflect

import kotlin.reflect.*

internal open class KProperty0Impl<out V> const constructor(override val name: String, open val get: () -> V) :
    KProperty0<V> {
    override fun get() = get.invoke()
    override fun invoke() = get.invoke()
}

internal class KMutableProperty0Impl<V> const constructor(
    override val name: String,
    override val get: () -> V,
    open val set: (value: V) -> Unit
) : KProperty0Impl<V>(name, get), KMutableProperty0<V> {
    override fun set(value: V) = set.invoke(value)
}

internal open class KProperty1Impl<T, out V> const constructor(
    override val name: String,
    open val get: (receiver: T) -> V
) : KProperty1<T, V> {
    override fun get(receiver: T) = get.invoke(receiver)
    override fun invoke(receiver: T) = get.invoke(receiver)
}

internal class KMutableProperty1Impl<T, V> const constructor(
    override val name: String,
    override val get: (receiver: T) -> V,
    open val set: (receiver: T, value: V) -> Unit
) : KProperty1Impl<T, V>(name, get), KMutableProperty1<T, V> {
    override fun set(receiver: T, value: V) = set.invoke(receiver, value)
}

internal open class KProperty2Impl<D, E, out V> const constructor(
    override val name: String,
    open val get: (receiver1: D, receiver2: E) -> V
) : KProperty2<D, E, V> {
    override fun get(receiver1: D, receiver2: E) = get.invoke(receiver1, receiver2)
    override fun invoke(receiver1: D, receiver2: E) = get.invoke(receiver1, receiver2)
}

internal class KMutableProperty2Impl<D, E, V> const constructor(
    override val name: String,
    override val get: (receiver1: D, receiver2: E) -> V,
    open val set: (receiver1: D, receiver2: E, value: V) -> Unit
) : KProperty2Impl<D, E, V>(name, get), KMutableProperty2<D, E, V> {
    override fun set(receiver1: D, receiver2: E, value: V) = set.invoke(receiver1, receiver2, value)
}