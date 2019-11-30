package org.zwobble.hod.compiler.tests.parser

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.parser.parseExpression
import org.zwobble.hod.compiler.tests.isIdentifier

class ParseFieldAccessTests {
    @Test
    fun canParseFieldAccess() {
        val source = "x.y"
        val node = parseString(::parseExpression, source)
        assertThat(node, isFieldAccess(
            receiver = isVariableReference("x"),
            fieldName = isIdentifier("y"),
            source = isStringSource("x.y", 1)
        ))
    }

    @Test
    fun fieldAccessIsLeftAssociative() {
        val source = "x.y.z"
        val node = parseString(::parseExpression, source)
        assertThat(node, isFieldAccess(
            receiver = isFieldAccess(
                receiver = isVariableReference("x"),
                fieldName = isIdentifier("y")
            ),
            fieldName = isIdentifier("z")
        ))
    }
}
