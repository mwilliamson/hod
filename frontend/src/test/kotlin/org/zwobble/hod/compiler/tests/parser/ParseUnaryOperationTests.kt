package org.zwobble.hod.compiler.tests.parser

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.ast.UnaryOperator
import org.zwobble.hod.compiler.parser.parseExpression

class ParseUnaryOperationTests {
    @Test
    fun canParseUnaryMinus() {
        val source = "-x"
        val node = parseString(::parseExpression, source)
        assertThat(node, isUnaryOperation(
            UnaryOperator.MINUS,
            isVariableReference("x")
        ))
    }

    @Test
    fun canParseNotOperation() {
        val source = "not x"
        val node = parseString(::parseExpression, source)
        assertThat(node, isUnaryOperation(
            UnaryOperator.NOT,
            isVariableReference("x")
        ))
    }
}
