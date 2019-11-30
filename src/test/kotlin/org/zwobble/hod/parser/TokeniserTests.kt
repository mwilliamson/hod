package org.zwobble.hod.parser

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class TokeniserTests {
    @TestFactory
    fun keywordsAreTokenised(): List<DynamicTest> {
        return listOf(
            "false" to TokenType.KEYWORD_FALSE,
            "true" to TokenType.KEYWORD_TRUE
        ).map { keyword ->
            DynamicTest.dynamicTest(keyword.first) {
                assertEquals(
                    listOf(Token(0, keyword.second, keyword.first)),
                    tokenise(keyword.first)
                )
            }
        }
    }

    @TestFactory
    fun identifiersAreTokenised(): List<DynamicTest> {
        return listOf("x", "one", "x1", "ONE").map { identifier ->
            DynamicTest.dynamicTest(identifier) {
                assertEquals(
                    listOf(Token(0, TokenType.IDENTIFIER, identifier)),
                    tokenise(identifier)
                )
            }
        }
    }

    @Test
    fun identifierWithKeywordAsPrefixIsTokenisedAsIdentifier() {
        assertEquals(
            listOf(Token(0, TokenType.IDENTIFIER, "value")),
            tokenise("value")
        )
    }

    @TestFactory
    fun symbolsAreTokenised(): List<DynamicTest> {
        return listOf(
            "." to TokenType.SYMBOL_DOT,
            "," to TokenType.SYMBOL_COMMA
        ).map { symbol ->
            DynamicTest.dynamicTest(symbol.first) {
                assertEquals(
                    listOf(Token(0, symbol.second, symbol.first)),
                    tokenise(symbol.first)
                )
            }
        }
    }

    @TestFactory
    fun integersAreTokenised(): List<DynamicTest> {
        return listOf("0", "1", "-1", "42").map { symbol ->
            DynamicTest.dynamicTest(symbol) {
                assertEquals(
                    listOf(Token(0, TokenType.INTEGER, symbol)),
                    tokenise(symbol)
                )
            }
        }
    }

    @TestFactory
    fun stringsAreTokenised(): List<DynamicTest> {
        return listOf(
            "\"\"",
            "\"abc\"",
            "\"\\n\"",
            "\"\\\"\"",
            "\"\\u{1B}\""
        ).map { string ->
            DynamicTest.dynamicTest(string) {
                assertEquals(
                    listOf(Token(0, TokenType.STRING, string)),
                    tokenise(string)
                )
            }
        }
    }

    @Test
    fun doubleQuoteTerminatesString() {
        assertEquals(
            listOf(
                Token(0, TokenType.STRING, "\"a\""),
                Token(3, TokenType.STRING, "\"b\"")
            ),
            tokenise("\"a\"\"b\"")
        )
    }

    @TestFactory
    fun unterminatedStringsAreTokenised(): List<DynamicTest> {
        return listOf(
            "\"",
            "\"abc"
        ).map { string ->
            DynamicTest.dynamicTest(string) {
                assertEquals(
                    listOf(Token(0, TokenType.UNTERMINATED_STRING, string)),
                    tokenise(string)
                )
            }
        }
    }

    @Test
    fun unescapedNewlineCannotAppearInString() {
        assertEquals(
            listOf(
                Token(0, TokenType.UNTERMINATED_STRING, "\""),
                Token(1, TokenType.WHITESPACE, "\n"),
                Token(2, TokenType.UNTERMINATED_STRING, "\"")
            ),
            tokenise("\"\n\"")
        )
    }

    @TestFactory
    fun codePointsAreTokenised(): List<DynamicTest> {
        return listOf(
            "'a'",
            "'\\n'",
            "'\\\"'",
            "'\\u{1B}'"
        ).map { string ->
            DynamicTest.dynamicTest(string) {
                assertEquals(
                    listOf(Token(0, TokenType.CODE_POINT, string)),
                    tokenise(string)
                )
            }
        }
    }

    data class WhitespaceTestCase(val input: String, val description: String)

    @TestFactory
    fun whitespaceIsTokenised(): List<DynamicTest> {
        return listOf(
            WhitespaceTestCase("\n", "line feed"),
            WhitespaceTestCase("\r", "carriage return"),
            WhitespaceTestCase("\t", "tab"),
            WhitespaceTestCase(" ", "space")
        ).map { case ->
            DynamicTest.dynamicTest(case.description) {
                assertEquals(
                    listOf(Token(0, TokenType.WHITESPACE, case.input)),
                    tokenise(case.input)
                )
            }
        }
    }

    @Test
    fun doubleSlashStartsLineComment() {
        assertEquals(
            listOf(
                Token(0, TokenType.INTEGER, "1"),
                Token(1, TokenType.WHITESPACE, " "),
                Token(2, TokenType.COMMENT, "// blah"),
                Token(9, TokenType.WHITESPACE, "\n"),
                Token(10, TokenType.INTEGER, "2")
            ),
            tokenise("1 // blah\n2")
        )
    }
}
