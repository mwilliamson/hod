package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.present
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.tests.isIntType
import org.zwobble.hod.compiler.tests.variableReference
import org.zwobble.hod.compiler.typechecker.inferType
import org.zwobble.hod.compiler.types.IntType
import org.zwobble.hod.compiler.types.Type

class InferTypeTests {
    @Test
    fun typesOfInferredExpressionsAreStored() {
        val reference = variableReference("x")

        val expressionTypes = mutableMapOf<Int, Type>()
        val context = typeContext(
            expressionTypes = expressionTypes,
            referenceTypes = mapOf(reference to IntType)
        )

        inferType(reference, context)

        assertThat(expressionTypes[reference.nodeId], present(isIntType))
    }
}
