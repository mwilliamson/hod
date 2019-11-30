package org.zwobble.hod.compiler.tests.parser

import com.natpryce.hamkrest.absent
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.present
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.ast.ExpressionStatementNode
import org.zwobble.hod.compiler.parser.parseExpression
import org.zwobble.hod.compiler.tests.isSequence

class ParseWhenTests {
    @Test
    fun conditionsAndBodiesAreRead() {
        val source = """
            when (x) {
                is Some {
                    1
                }
                is None {
                    2
                }
            }
        """
        val node = parseString(::parseExpression, source)
        assertThat(node, isWhen(
            expression = isVariableReference("x"),
            branches = isSequence(
                isWhenBranch(
                    type = isStaticReference("Some"),
                    body = isBlock(
                        isExpressionStatement(expression = isIntLiteral(1), type = equalTo(ExpressionStatementNode.Type.RETURN))
                    )
                ),
                isWhenBranch(
                    type = isStaticReference("None"),
                    body = isBlock(
                        isExpressionStatement(expression = isIntLiteral(2), type = equalTo(ExpressionStatementNode.Type.RETURN))
                    )
                )
            ),
            elseBranch = absent()
        ))
    }

    @Test
    fun elseBranchIsRead() {
        val source = """
            when (x) {
                is Some {
                    1
                }
                else {
                    2
                }
            }
        """
        val node = parseString(::parseExpression, source)
        assertThat(node, isWhen(
            expression = isVariableReference("x"),
            branches = isSequence(
                isWhenBranch(
                    type = isStaticReference("Some"),
                    body = isBlock(
                        isExpressionStatement(expression = isIntLiteral(1), type = equalTo(ExpressionStatementNode.Type.RETURN))
                    )
                )
            ),
            elseBranch = present(isBlock(
                isExpressionStatement(expression = isIntLiteral(2), type = equalTo(ExpressionStatementNode.Type.RETURN))
            ))
        ))
    }
}
