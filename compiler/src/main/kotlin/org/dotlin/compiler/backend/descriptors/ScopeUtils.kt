package org.dotlin.compiler.backend.descriptors

import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.utils.Printer

context(MemberScope)
fun Printer.printScope() {
    println(this::class.simpleName + "{")
    pushIndent()

    getContributedDescriptors().forEach {
        println(it)
    }

    popIndent()
    println("}")
}