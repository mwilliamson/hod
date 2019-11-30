package org.zwobble.hod.parser

import org.zwobble.hod.BoolLiteralNode
import org.zwobble.hod.ExpressionNode
import org.zwobble.hod.StringSource

internal fun parseExpression(tokens: TokenIterator<TokenType>): ExpressionNode {
    val source = tokens.location()

    if (tokens.trySkip(TokenType.KEYWORD_TRUE)) {
        return BoolLiteralNode(value = true, source = source)
    } else if (tokens.trySkip(TokenType.KEYWORD_FALSE)) {
        return BoolLiteralNode(value = false, source = source)
    } else {
        throw UnexpectedTokenException(
            expected = "expression",
            actual = tokens.peek().describe(),
            location = tokens.location()
        )
    }
}

internal fun parserTokenise(filename: String, input: String): TokenIterator<TokenType> {
    val tokens = tokenise(input)
        .filter { token -> token.tokenType != TokenType.WHITESPACE && token.tokenType != TokenType.COMMENT }
    return TokenIterator(
        locate = { characterIndex ->
            StringSource(
                filename = filename,
                contents = input,
                characterIndex = characterIndex
            )
        },
        tokens = tokens,
        end = Token(input.length, TokenType.END, "")
    )
}
