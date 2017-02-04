package org.shedlang.compiler.tests.parser

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.junit.jupiter.api.Test
import org.shedlang.compiler.ast.IntegerLiteralNode
import org.shedlang.compiler.parser.parseFunctionStatement

class ParseValTests {
    @Test
    fun expressionIsReadForExpressionStatements() {
        val source = "val x = 4;"
        val node = parseString(::parseFunctionStatement, source)
        assertThat(node, isVal(
            name = equalTo("x"),
            expression = has(IntegerLiteralNode::value, equalTo(4))
        ))
    }
}