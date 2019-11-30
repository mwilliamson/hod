package org.zwobble.hod.compiler.tests.parser

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.parser.parseStaticExpression
import org.zwobble.hod.compiler.tests.isSequence

class ParseTupleTypeTests {
    @Test
    fun emptyTupleType() {
        val source = "#()"
        val node = parseString(::parseStaticExpression, source)

        assertThat(node, isTupleTypeNode(isSequence()))
    }

    @Test
    fun singletonTupleType() {
        val source = "#(X)"
        val node = parseString(::parseStaticExpression, source)

        assertThat(node, isTupleTypeNode(isSequence(
            isStaticReference("X")
        )))
    }

    @Test
    fun tupleTypeWithManyElements() {
        val source = "#(X, Y, Z)"
        val node = parseString(::parseStaticExpression, source)

        assertThat(node, isTupleTypeNode(isSequence(
            isStaticReference("X"),
            isStaticReference("Y"),
            isStaticReference("Z")
        )))
    }

    @Test
    fun elementsCanHaveTrailingComma() {
        val source = "#(X,)"
        val node = parseString(::parseStaticExpression, source)

        assertThat(node, isTupleTypeNode(isSequence(
            isStaticReference("X")
        )))
    }
}
