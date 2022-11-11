package org.dotlin.compiler.backend.steps.src2ir

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.types.*

/**
 * Primitive arrays are mapped to the typed array, e.g. `IntArray` to `Array<Int>`.
 */
// TODO: Map all other number primitives to Int and Double
class DotlinBuiltIns : KotlinBuiltIns(LockBasedStorageManager("DotlinBuiltIns")) {
    private val primitiveTypes by lazy {
        setOf(
            booleanType,
            charType,
            byteType,
            shortType,
            intType,
            longType,
            floatType,
            doubleType
        )
    }

    private val primitiveToSimpleTypes by lazy {
        mapOf(
            PrimitiveType.BOOLEAN to booleanType,
            PrimitiveType.CHAR to charType,
            PrimitiveType.BYTE to byteType,
            PrimitiveType.SHORT to shortType,
            PrimitiveType.INT to intType,
            PrimitiveType.LONG to longType,
            PrimitiveType.FLOAT to floatType,
            PrimitiveType.DOUBLE to doubleType,
        )
    }

    override fun getArrayElementType(arrayType: KotlinType): KotlinType = arrayType.arguments.single().type

    override fun getArrayType(projectionType: Variance, argument: KotlinType, annotations: Annotations): SimpleType {
        return KotlinTypeFactory.simpleType(
            baseType = array.defaultType,
            annotations = TypeAttributes.create(listOf(AnnotationsTypeAttribute(annotations))),
            arguments = listOf(TypeProjectionImpl(projectionType, argument)),
        )
    }

    override fun getPrimitiveArrayClassDescriptor(type: PrimitiveType) = array

    override fun getPrimitiveArrayKotlinType(primitiveType: PrimitiveType) =
        getArrayType(Variance.INVARIANT, primitiveToSimpleTypes[primitiveType]!!)

    override fun getPrimitiveArrayKotlinTypeByPrimitiveKotlinType(kotlinType: KotlinType): SimpleType? {
        if (kotlinType !in primitiveTypes) return null

        return getArrayType(Variance.INVARIANT, kotlinType)
    }
}