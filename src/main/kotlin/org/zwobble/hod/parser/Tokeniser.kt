package org.zwobble.hod.parser;

private fun keyword(tokenType: TokenType, string: String) =
    RegexTokeniser.rule(tokenType, Regex.escape(string) + "(?![A-Za-z0-9])")

private fun symbol(tokenType: TokenType, string: String) = RegexTokeniser.rule(tokenType, Regex.escape(string))

private const val unterminatedStringPattern = "\"(?:[^\\\\\"\n\r]|\\\\.)*"

private const val identifierPattern = "[A-Za-z][A-Za-z0-9]*"

private val tokeniser = RegexTokeniser(
    TokenType.UNKNOWN, listOf(
        keyword(TokenType.KEYWORD_EXPORT, "export"),
        keyword(TokenType.KEYWORD_FALSE, "false"),
        keyword(TokenType.KEYWORD_FROM, "from"),
        keyword(TokenType.KEYWORD_IMPORT, "import"),
        keyword(TokenType.KEYWORD_TEST, "test"),
        keyword(TokenType.KEYWORD_TRUE, "true"),
        keyword(TokenType.KEYWORD_VAL, "val"),

        RegexTokeniser.rule(TokenType.INTEGER, "-?[0-9]+"),

        symbol(TokenType.SYMBOL_BRACE_CLOSE, "}"),
        symbol(TokenType.SYMBOL_BRACE_OPEN, "{"),
        symbol(TokenType.SYMBOL_COMMA, ","),
        symbol(TokenType.SYMBOL_DOT, "."),
        symbol(TokenType.SYMBOL_EQUALS, "="),
        symbol(TokenType.SYMBOL_PAREN_CLOSE, ")"),
        symbol(TokenType.SYMBOL_PAREN_OPEN, "("),
        symbol(TokenType.SYMBOL_SEMICOLON, ";"),

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

    KEYWORD_EXPORT,
    KEYWORD_FALSE,
    KEYWORD_FROM,
    KEYWORD_IMPORT,
    KEYWORD_TEST,
    KEYWORD_TRUE,
    KEYWORD_VAL,

    STRING,

    SYMBOL_BRACE_CLOSE,
    SYMBOL_BRACE_OPEN,
    SYMBOL_COMMA,
    SYMBOL_DOT,
    SYMBOL_EQUALS,
    SYMBOL_PAREN_CLOSE,
    SYMBOL_PAREN_OPEN,
    SYMBOL_SEMICOLON,

    UNKNOWN,

    UNTERMINATED_STRING,

    WHITESPACE
}
