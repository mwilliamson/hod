package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.cast
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.tests.*
import org.zwobble.hod.compiler.typechecker.inferType

class TypeCheckTupleTests {
    @Test
    fun emptyTupleHasEmptyTupleType() {
        val node = tupleNode(listOf())
        val type = inferType(node, typeContext())
        assertThat(type, cast(isTupleType(isSequence())))
    }

    @Test
    fun tupleWithElementsHasTupleType() {
        val node = tupleNode(listOf(literalInt(), literalString()))
        val type = inferType(node, typeContext())
        assertThat(type, cast(isTupleType(isSequence(isIntType, isStringType))))
    }
}
