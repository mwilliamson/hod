package org.zwobble.hod.compiler.tests.parser

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.parser.parseModuleStatement
import org.zwobble.hod.compiler.tests.isIdentifier

class ParseTypeAliasTests {
    @Test
    fun canParseTypeAlias() {
        val source = "type Size = Int;"
        val node = parseString(::parseModuleStatement, source)
        assertThat(node, isTypeAlias(
            name = isIdentifier("Size"),
            expression = isStaticReference("Int")
        ))
    }
}
