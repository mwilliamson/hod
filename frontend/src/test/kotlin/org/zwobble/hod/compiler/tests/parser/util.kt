package org.zwobble.hod.compiler.tests.parser

import org.zwobble.hod.compiler.parser.TokenIterator
import org.zwobble.hod.compiler.parser.TokenType
import org.zwobble.hod.compiler.parser.parserTokenise


internal fun <T> parseString(parser: (TokenIterator<TokenType>) -> T, input: String): T {
    val tokens = tokeniseWithoutWhitespace(input)
    return parser(tokens)
}

private fun tokeniseWithoutWhitespace(input: String): TokenIterator<TokenType> {
    return parserTokenise(filename = "<filename>", input = input)
}
