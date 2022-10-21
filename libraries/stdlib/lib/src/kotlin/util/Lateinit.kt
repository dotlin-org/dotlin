/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

import kotlin.internal.AccessibleLateinitPropertyLiteral
import kotlin.reflect.KProperty0

/**
 * Returns `true` if this lateinit property has been assigned a value, and `false` otherwise.
 *
 * This will call the getter of the property. If the getter is resource-intensive, consider
 * checking whether the property is initialized in different way.
 */
@SinceKotlin("1.2")
/*inline*/ val @receiver:AccessibleLateinitPropertyLiteral KProperty0<*>.isInitialized: Boolean
    get() {
        try {
            get()
        } catch (e: Error) {
            if (e.runtimeType.hashCode() == 425871248) {
                return false
            }
        }

        return true
    }
