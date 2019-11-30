package org.zwobble.hod.compiler.tests.parser

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.ast.ExpressionStatementNode
import org.zwobble.hod.compiler.frontend.tests.throwsException
import org.zwobble.hod.compiler.parser.parseFunctionStatement
import org.zwobble.hod.compiler.typechecker.SourceError

class ParseExpressionStatementTests {
    @Test
    fun expressionWithTrailingSemiColonIsReadAsNonReturningExpressionStatement() {
        val source = "4;"
        val node = parseString(::parseFunctionStatement, source)
        assertThat(node, isExpressionStatement(
            isIntLiteral(4),
            type = equalTo(ExpressionStatementNode.Type.NO_RETURN)
        ))
    }

    @Test
    fun expressionWithoutTrailingSemiColonIsReadAsReturningExpressionStatement() {
        val source = "4"
        val node = parseString(::parseFunctionStatement, source)
        assertThat(node, isExpressionStatement(
            isIntLiteral(4),
            type = equalTo(ExpressionStatementNode.Type.RETURN)
        ))
    }

    @Test
    fun expressionWithTailrecPrefixIsReadAsTailrecReturningExpressionStatement() {
        val source = "tailrec f()"
        val node = parseString(::parseFunctionStatement, source)
        assertThat(node, isExpressionStatement(
            isCall(),
            type = equalTo(ExpressionStatementNode.Type.TAILREC_RETURN)
        ))
    }

    @Test
    fun whenAllBranchesOfIfReturnThenIfExpressionIsReadAsReturning() {
        val source = "if (true) { 1 } else { 2 }"
        val node = parseString(::parseFunctionStatement, source)
        assertThat(node.isReturn, equalTo(true))
    }

    @Test
    fun whenAllBranchesOfIfDoNotReturnThenIfExpressionIsReadAsNotReturning() {
        val source = "if (true) { 1; } else { 2; }"
        val node = parseString(::parseFunctionStatement, source)
        assertThat(node.isReturn, equalTo(false))
    }

    @Test
    fun whenSomeBranchesOfIfReturnThenErrorIsThrown() {
        val source = "if (true) { 1 } else { 2; }"
        assertThat({
            parseString(::parseFunctionStatement, source)
        }, throwsException<SourceError>(has(SourceError::message, equalTo("Some branches do not provide a value"))))
    }

    @Test
    fun whenAllBranchesOfWhenReturnThenWhenExpressionIsReadAsReturning() {
        val source = "when (x) { is T1 { 1 } is T2 { 2 } }"
        val node = parseString(::parseFunctionStatement, source)
        assertThat(node.isReturn, equalTo(true))
    }

    @Test
    fun whenAllBranchesOfWhenDoNotReturnThenWhenExpressionIsReadAsNotReturning() {
        val source = "when (x) { is T1 { 1; } is T2 { 2; } }"
        val node = parseString(::parseFunctionStatement, source)
        assertThat(node.isReturn, equalTo(false))
    }

    @Test
    fun whenSomeBranchesOfWhenReturnThenErrorIsThrown() {
        val source = "when (x) { is T1 { 1 } is T2 { 2; } }"
        assertThat({
            parseString(::parseFunctionStatement, source)
        }, throwsException<SourceError>(has(SourceError::message, equalTo("Some branches do not provide a value"))))
    }
}
