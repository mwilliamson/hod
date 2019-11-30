package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.tests.*
import org.zwobble.hod.compiler.typechecker.NoSuchFieldError
import org.zwobble.hod.compiler.typechecker.inferType
import org.zwobble.hod.compiler.types.IntType
import org.zwobble.hod.compiler.types.UnitType

class TypeCheckFieldAccessTests {
    @Test
    fun typeOfFieldAccessIsTypeOfField() {
        val receiver = variableReference("x")
        val node = fieldAccess(receiver = receiver, fieldName = "y")
        val shapeType = shapeType(name = "X", fields = listOf(field("y", IntType)))

        val typeContext = typeContext(referenceTypes = mapOf(receiver to shapeType))
        val type = inferType(node, typeContext)

        assertThat(type, isIntType)
    }

    @Test
    fun whenShapeHasNoSuchFieldThenErrorIsThrown() {
        val receiver = variableReference("x")
        val node = fieldAccess(receiver = receiver, fieldName = "y")
        val shapeType = shapeType(name = "X")

        val typeContext = typeContext(referenceTypes = mapOf(receiver to shapeType))

        assertThat(
            { inferType(node, typeContext) },
            throws(has(NoSuchFieldError::fieldName, isIdentifier("y")))
        )
    }

    @Test
    fun whenReceiverIsNotShapeThenErrorIsThrown() {
        val receiver = variableReference("x")
        val node = fieldAccess(receiver = receiver, fieldName = "y")
        val shapeType = UnitType

        val typeContext = typeContext(referenceTypes = mapOf(receiver to shapeType))

        assertThat(
            { inferType(node, typeContext) },
            throws(has(NoSuchFieldError::fieldName, isIdentifier("y")))
        )
    }
}
