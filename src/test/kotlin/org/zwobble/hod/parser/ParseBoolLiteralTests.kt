package org.zwobble.hod.parser

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.isBoolLiteral

class ParseBoolLiteralTests {
    @Test
    fun canParseTrue() {
        val source = "true"

        val node = parseString(::parseExpression, source)

        assertThat(node, isBoolLiteral(true))
    }

    @Test
    fun canParseFalse() {
        val source = "false"

        val node = parseString(::parseExpression, source)

        assertThat(node, isBoolLiteral(false))
    }
}
