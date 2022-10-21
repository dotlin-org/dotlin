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

@file:Suppress("IMPLEMENTING_FUNCTION_INTERFACE")
package kotlin.reflect

/**
 * Represents a property, such as a named `val` or `var` declaration.
 * Instances of this class are obtainable by the `::` operator.
 * 
 * See the [Kotlin language documentation](https://kotlinlang.org/docs/reference/reflection.html)
 * for more information.
 *
 * @param V the type of the property value.
 */
interface KProperty<out V> : KCallable<V>

/**
 * Represents a property declared as a `var`.
 */
interface KMutableProperty<V> : KProperty<V>


/**
 * Represents a property without any kind of receiver.
 * Such property is either originally declared in a receiverless context such as a package,
 * or has the receiver bound to it.
 */
interface KProperty0<out V> : KProperty<V>, () -> V {
    /**
     * Returns the current value of the property.
     */
    fun get(): V
}

/**
 * Represents a `var`-property without any kind of receiver.
 */
interface KMutableProperty0<V> : KProperty0<V>, KMutableProperty<V> {
    /**
     * Modifies the value of the property.
     *
     * @param value the new value to be assigned to this property.
     */
    fun set(value: V)
}


/**
 * Represents a property, operations on which take one receiver as a parameter.
 *
 * @param T the type of the receiver which should be used to obtain the value of the property.
 * @param V the type of the property value.
 */
interface KProperty1<T, out V> : KProperty<V>, (T) -> V {
    /**
     * Returns the current value of the property.
     *
     * @param receiver the receiver which is used to obtain the value of the property.
     *                 For example, it should be a class instance if this is a member property of that class,
     *                 or an extension receiver if this is a top level extension property.
     */
    fun get(receiver: T): V
}

/**
 * Represents a `var`-property, operations on which take one receiver as a parameter.
 */
interface KMutableProperty1<T, V> : KProperty1<T, V>, KMutableProperty<V> {
    /**
     * Modifies the value of the property.
     *
     * @param receiver the receiver which is used to modify the value of the property.
     *                 For example, it should be a class instance if this is a member property of that class,
     *                 or an extension receiver if this is a top level extension property.
     * @param value the new value to be assigned to this property.
     */
    fun set(receiver: T, value: V)
}


/**
 * Represents a property, operations on which take two receivers as parameters,
 * such as an extension property declared in a class.
 *
 * @param D the type of the first receiver. In case of the extension property in a class this is
 *        the type of the declaring class of the property, or any subclass of that class.
 * @param E the type of the second receiver. In case of the extension property in a class this is
 *        the type of the extension receiver.
 * @param V the type of the property value.
 */
interface KProperty2<D, E, out V> : KProperty<V>, (D, E) -> V {
    /**
     * Returns the current value of the property. In case of the extension property in a class,
     * the instance of the class should be passed first and the instance of the extension receiver second.
     *
     * @param receiver1 the instance of the first receiver.
     * @param receiver2 the instance of the second receiver.
     */
    fun get(receiver1: D, receiver2: E): V
}

/**
 * Represents a `var`-property, operations on which take two receivers as parameters.
 */
interface KMutableProperty2<D, E, V> : KProperty2<D, E, V>, KMutableProperty<V> {
    /**
     * Modifies the value of the property.
     *
     * @param receiver1 the instance of the first receiver.
     * @param receiver2 the instance of the second receiver.
     * @param value the new value to be assigned to this property.
     */
    fun set(receiver1: D, receiver2: E, value: V)
}
