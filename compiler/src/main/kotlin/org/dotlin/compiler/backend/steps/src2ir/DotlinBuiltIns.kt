package org.dotlin.compiler.backend.steps.src2ir

import org.dotlin.compiler.backend.dotlin
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.storage.getValue
import org.jetbrains.kotlin.types.*

/**
 * Primitive arrays are mapped to the typed array, e.g. `IntArray` to `Array<Int>`.
 *
 * Also includes some other Dotlin intrinsic types.
 */
// TODO: Map all other number primitives to Int and Double
class DotlinBuiltIns : KotlinBuiltIns(LockBasedStorageManager("DotlinBuiltIns")) {
    private val getBuiltInClass = storageManager.createMemoizedFunction<FqName, ClassDescriptor> {
        getBuiltInClassByFqName(it)
    }

    val anyList by storageManager.createLazyValue { getBuiltInClass(dotlin.intrinsics.AnyList) }
    val anySet by storageManager.createLazyValue { getBuiltInClass(dotlin.intrinsics.AnySet) }
    val anyMap by storageManager.createLazyValue { getBuiltInClass(dotlin.intrinsics.AnyMap) }

    private val primitiveTypes by storageManager.createLazyValue {
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

    private val primitiveToSimpleTypes by storageManager.createLazyValue {
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