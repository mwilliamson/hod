package org.zwobble.hod.parser;

private fun keyword(tokenType: TokenType, string: String) =
    RegexTokeniser.rule(tokenType, Regex.escape(string) + "(?![A-Za-z0-9])")

private fun symbol(tokenType: TokenType, string: String) = RegexTokeniser.rule(tokenType, Regex.escape(string))

private const val unterminatedStringPattern = "\"(?:[^\\\\\"\n\r]|\\\\.)*"

private const val identifierPattern = "[A-Za-z][A-Za-z0-9]*"

private val tokeniser = RegexTokeniser(
    TokenType.UNKNOWN, listOf(
        keyword(TokenType.KEYWORD_FALSE, "false"),
        keyword(TokenType.KEYWORD_TRUE, "true"),

        RegexTokeniser.rule(TokenType.INTEGER, "-?[0-9]+"),

        symbol(TokenType.SYMBOL_DOT, "."),
        symbol(TokenType.SYMBOL_COMMA, ","),

        RegexTokeniser.rule(TokenType.IDENTIFIER, identifierPattern),
        RegexTokeniser.rule(TokenType.STRING, unterminatedStringPattern + "\""),
        RegexTokeniser.rule(TokenType.UNTERMINATED_STRING, unterminatedStringPattern),
        RegexTokeniser.rule(TokenType.CODE_POINT, "'(?:[^\\\\'\n\r]|\\\\.)*'"),
        RegexTokeniser.rule(TokenType.WHITESPACE, "[\r\n\t ]+"),
        RegexTokeniser.rule(TokenType.COMMENT, "//[^\n]*")
    )
)

internal fun tokenise(value: String): List<Token<TokenType>> {
    return tokeniser.tokenise(value)
}

internal enum class TokenType {
    CODE_POINT,

    COMMENT,

    END,

    IDENTIFIER,

    INTEGER,

    KEYWORD_FALSE,
    KEYWORD_TRUE,

    STRING,

    SYMBOL_COMMA,
    SYMBOL_DOT,

    UNKNOWN,

    UNTERMINATED_STRING,

    WHITESPACE
}
