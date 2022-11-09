package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.dotlin
import org.dotlin.compiler.backend.kotlin
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.renderer.DescriptorRendererOptions
import org.jetbrains.kotlin.resolve.TypeResolver
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.KotlinTypeRefiner

/**
 * Ideally this would've inherit [TypeResolver] and transform types through there. But the class
 * is closed, so we use [DotlinTypeTransformer] as a [TypeResolver.TypeTransformerForTests].
 */
class DotlinTypeTransformer : TypeResolver.TypeTransformerForTests() {
    private fun KotlinType.toDotlinTypeOrNull(): KotlinType? {
        if (fqNameOrNull() == dotlin.intrinsics.Flex) {
            return DotlinFlexibleType(
                KotlinTypeFactory.flexibleType(
                    lowerBound = arguments.first().type as SimpleType,
                    upperBound = arguments.last().type as SimpleType
                ) as FlexibleType
            )
        }

        return null
    }

    override fun transformType(kotlinType: KotlinType) = kotlinType.toDotlinTypeOrNull()
}

/**
 * Types representing `dotlin.intrinsics.Flex`.
 */
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

    override fun replaceAnnotations(newAnnotations: Annotations) = original.replaceAnnotations(newAnnotations)
}

private fun KotlinType.isAnyList(): Boolean = fqNameOrNull() == dotlin.intrinsics.AnyList
private fun KotlinType.isAnySet(): Boolean = fqNameOrNull() == dotlin.intrinsics.AnySet
private fun KotlinType.isAnyMap(): Boolean = fqNameOrNull() == dotlin.intrinsics.AnyMap
private fun KotlinType.isList(): Boolean = fqNameOrNull() == kotlin.collections.List
private fun KotlinType.isSet(): Boolean = fqNameOrNull() == kotlin.collections.Set
private fun KotlinType.isMap(): Boolean = fqNameOrNull() == kotlin.collections.Map

private fun KotlinType.fqNameOrNull(): FqName? = constructor.declarationDescriptor?.fqNameOrNull()