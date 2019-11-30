package org.zwobble.hod.compiler.tests.parser

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.parser.parseExpression

class ParseIsTests {
    @Test
    fun canParseIsExpression() {
        val source = "x is X"
        val node = parseString(::parseExpression, source)
        assertThat(node, isIsOperation(
            expression = isVariableReference("x"),
            type = isStaticReference("X")
        ))
    }
}
