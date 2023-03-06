package org.dotlin.compiler.backend.descriptors

import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.kotlin
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.renderer.DescriptorRendererOptions
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.types.FlexibleType
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeAttributes
import org.jetbrains.kotlin.types.TypeRefinement
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner

class DotlinFlexibleType(private val original: FlexibleType) : FlexibleType(original.lowerBound, original.upperBound) {
    override val delegate = original.delegate

    override fun makeNullableAsSpecified(newNullability: Boolean) = original.makeNullableAsSpecified(newNullability)

    @TypeRefinement
    override fun refine(kotlinTypeRefiner: KotlinTypeRefiner) = original.refine(kotlinTypeRefiner)

    override fun render(renderer: DescriptorRenderer, options: DescriptorRendererOptions): String {
        val mainPart = when {
            lowerBound.isAnyList() && upperBound.isList() -> "(Immutable|Mutable|FixedSize)List"
            lowerBound.isAnySet() && upperBound.isSet() -> "(Immutable|Mutable)Set"
            lowerBound.isAnyMap() && upperBound.isMap() -> "(Immutable|Mutable)Set"
            else -> return original.render(renderer, options)
        }

        val argument = renderer.renderType(lowerBound.arguments.single().type)
        val questionMark = when {
            lowerBound.isMarkedNullable && upperBound.isMarkedNullable -> "?"
            else -> ""
        }

        return "$mainPart<$argument>$questionMark"
    }

    override fun replaceAttributes(newAttributes: TypeAttributes) = delegate.replaceAttributes(newAttributes)
}

private fun KotlinType.isAnyList(): Boolean = fqNameOrNull() == dotlin.intrinsics.AnyList
private fun KotlinType.isAnySet(): Boolean = fqNameOrNull() == dotlin.intrinsics.AnySet
private fun KotlinType.isAnyMap(): Boolean = fqNameOrNull() == dotlin.intrinsics.AnyMap
private fun KotlinType.isList(): Boolean = fqNameOrNull() == kotlin.collections.List
private fun KotlinType.isSet(): Boolean = fqNameOrNull() == kotlin.collections.Set
private fun KotlinType.isMap(): Boolean = fqNameOrNull() == kotlin.collections.Map

private fun KotlinType.fqNameOrNull(): FqName? = constructor.declarationDescriptor?.fqNameOrNull()