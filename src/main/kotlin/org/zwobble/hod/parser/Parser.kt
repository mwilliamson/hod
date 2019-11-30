package org.zwobble.hod.parser

import org.zwobble.hod.BoolLiteralNode
import org.zwobble.hod.ExpressionNode
import org.zwobble.hod.StringLiteralNode
import org.zwobble.hod.StringSource
import java.nio.CharBuffer
import java.util.regex.Pattern

internal fun parseExpression(tokens: TokenIterator<TokenType>): ExpressionNode {
    val source = tokens.location()

    if (tokens.trySkip(TokenType.KEYWORD_TRUE)) {
        return BoolLiteralNode(value = true, source = source)
    } else if (tokens.trySkip(TokenType.KEYWORD_FALSE)) {
        return BoolLiteralNode(value = false, source = source)
    } else if (tokens.isNext(TokenType.STRING)) {
        val token = tokens.next()
        val value = decodeCodePointToken(token.value, source = source)
        return StringLiteralNode(value, source)
    } else {
        throw UnexpectedTokenException(
            expected = "expression",
            actual = tokens.peek().describe(),
            location = tokens.location()
        )
    }
}

private fun decodeCodePointToken(value: String, source: StringSource): String {
    return decodeEscapeSequence(value.substring(1, value.length - 1), source = source)
}

private fun decodeEscapeSequence(value: String, source: StringSource): String {
    return decodeEscapeSequence(CharBuffer.wrap(value), source = source)
}

private val STRING_ESCAPE_PATTERN = Pattern.compile("\\\\(.)")

private fun decodeEscapeSequence(value: CharBuffer, source: StringSource): String {
    val matcher = STRING_ESCAPE_PATTERN.matcher(value)
    val decoded = StringBuilder()
    var lastIndex = 0
    while (matcher.find()) {
        decoded.append(value.subSequence(lastIndex, matcher.start()))
        val code = matcher.group(1)
        if (code == "u") {
            if (value[matcher.end()] != '{') {
                throw InvalidCodePointError(
                    source = source.at(matcher.end() + 1),
                    message = "Expected opening brace"
                )
            }
            val startIndex = matcher.end() + 1
            val endIndex = value.indexOf("}", startIndex = startIndex)
            if (endIndex == -1) {
                throw InvalidCodePointError(
                    source = source.at(matcher.end() + 1),
                    message = "Could not find closing brace"
                )
            }
            val hex = value.subSequence(startIndex, endIndex).toString()
            val codePoint = hex.toInt(16)
            decoded.appendCodePoint(codePoint)
            lastIndex = endIndex + 1
        } else {
            decoded.append(escapeSequence(code, source = source))
            lastIndex = matcher.end()
        }
    }
    decoded.append(value.subSequence(lastIndex, value.length))
    return decoded.toString()
}

private fun escapeSequence(code: String, source: StringSource): Char {
    when (code) {
        "n" -> return '\n'
        "r" -> return '\r'
        "t" -> return '\t'
        "\"" -> return '"'
        "'" -> return '\''
        "\\" -> return '\\'
        else -> throw UnrecognisedEscapeSequenceError("\\" + code, source = source)
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
