package org.shedlang.compiler.tests.parser

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.shedlang.compiler.parser.parseExpression

class ParseFieldAccessTests {
    @Test
    fun canParseFieldAccess() {
        val source = "x.y"
        val node = parseString(::parseExpression, source)
        assertThat(node, isFieldAccess(
            receiver = isVariableReference("x"),
            fieldName = equalTo("y")
        ))
    }

    @Test
    fun fieldAccessIsLeftAssociative() {
        val source = "x.y.z"
        val node = parseString(::parseExpression, source)
        assertThat(node, isFieldAccess(
            receiver = isFieldAccess(
                receiver = isVariableReference("x"),
                fieldName = equalTo("y")
            ),
            fieldName = equalTo("z")
        ))
    }
}