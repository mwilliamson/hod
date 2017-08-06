package org.shedlang.compiler.tests.types

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.shedlang.compiler.tests.*
import org.shedlang.compiler.types.BoolType
import org.shedlang.compiler.types.IntType
import org.shedlang.compiler.types.TypeParameter
import org.shedlang.compiler.types.applyType

class TypeApplicationTests {
    @Test
    fun applyingTypeToShapeUpdatesTypeArguments() {
        val typeParameter1 = TypeParameter("T")
        val typeParameter2 = TypeParameter("U")
        val shape = parametrizedShapeType(
            "Pair",
            parameters = listOf(typeParameter1, typeParameter2),
            fields = mapOf()
        )
        assertThat(
            applyType(shape, listOf(BoolType, IntType)),
            isShapeType(
                name = equalTo("Pair"),
                typeArguments = isSequence(isBoolType, isIntType)
            )
        )
    }

    @Test
    fun applyingTypeToShapeReplacesTypeParameters() {
        val typeParameter1 = TypeParameter("T")
        val typeParameter2 = TypeParameter("U")
        val shape = parametrizedShapeType(
            "Pair",
            parameters = listOf(typeParameter1, typeParameter2),
            fields = mapOf(
                "first" to typeParameter1,
                "second" to typeParameter2
            )
        )
        assertThat(
            applyType(shape, listOf(BoolType, IntType)),
            isShapeType(fields = listOf(
                "first" to isBoolType,
                "second" to isIntType
            ))
        )
    }

    @Test
    fun applyingTypeToUnionUpdatesTypeArguments() {
        val typeParameter1 = TypeParameter("T")
        val typeParameter2 = TypeParameter("U")
        val union = parametrizedUnionType(
            "Either",
            parameters = listOf(typeParameter1, typeParameter2),
            members = listOf()
        )
        assertThat(
            applyType(union, listOf(BoolType, IntType)),
            isUnionType(
                name = equalTo("Either"),
                typeArguments = isSequence(isBoolType, isIntType)
            )
        )
    }

    @Test
    fun applyingTypeToUnionReplacesTypeParameters() {
        val typeParameter1 = TypeParameter("T")
        val typeParameter2 = TypeParameter("U")
        val union = parametrizedUnionType(
            "Either",
            parameters = listOf(typeParameter1, typeParameter2),
            members = listOf(typeParameter1, typeParameter2)
        )
        assertThat(
            applyType(union, listOf(BoolType, IntType)),
            isUnionType(members = isSequence(isBoolType, isIntType))
        )
    }

    @Test
    fun applyingTypeToUnionReplacesTypeParametersInMembers() {
        val shapeTypeParameter = TypeParameter("U")
        val shapeType = parametrizedShapeType(
            "Shape",
            listOf(shapeTypeParameter)
        )

        val unionTypeParameter = TypeParameter("T")
        val union = parametrizedUnionType(
            "Union",
            parameters = listOf(unionTypeParameter),
            members = listOf(applyType(shapeType, listOf(unionTypeParameter)))
        )

        assertThat(
            applyType(union, listOf(BoolType)),
            isUnionType(members = isSequence(
                isEquivalentType(applyType(shapeType, listOf(BoolType)))
            ))
        )
    }

    @Test
    fun applyingTypeToShapeReplacesTypeParametersInFields() {
        val unionTypeParameter = TypeParameter("T")
        val union = parametrizedUnionType(
            "Union",
            parameters = listOf(unionTypeParameter),
            members = listOf(unionTypeParameter)
        )

        val shapeTypeParameter = TypeParameter("U")
        val shapeType = parametrizedShapeType(
            "Shape",
            parameters = listOf(shapeTypeParameter),
            fields = mapOf("value" to applyType(union, listOf(shapeTypeParameter)))
        )

        assertThat(
            applyType(shapeType, listOf(BoolType)),
            isShapeType(fields = listOf(
                "value" to isEquivalentType(applyType(union, listOf(BoolType)))
            ))
        )
    }
}
