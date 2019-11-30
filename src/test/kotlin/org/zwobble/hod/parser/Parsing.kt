package org.zwobble.hod.parser

internal fun <T> parseString(parser: (TokenIterator<TokenType>) -> T, input: String): T {
    val tokens = tokeniseWithoutWhitespace(input)
    return parser(tokens)
}

private fun tokeniseWithoutWhitespace(input: String): TokenIterator<TokenType> {
    return parserTokenise(filename = "<filename>", input = input)
}
