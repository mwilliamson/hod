package org.zwobble.hod.parser

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.isReference

class ParseReferenceTests {
    @Test
    fun canParseReference() {
        val source = "value"

        val node = parseString(::parseExpression, source)

        assertThat(node, isReference("value"))
    }
}
