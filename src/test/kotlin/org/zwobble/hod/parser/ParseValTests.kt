package org.zwobble.hod.parser

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.zwobble.hod.isBoolLiteral
import org.zwobble.hod.isVal

class ParseValTests {
    @Test
    fun canParseInternalVal() {
        val source = """
            val value = true;
        """.trimIndent()

        val node = parseString(::parseCompilationUnitStatement, source)

        assertThat(node, isVal(
            target = equalTo("value"),
            expression = isBoolLiteral(true)
        ))
    }
}
