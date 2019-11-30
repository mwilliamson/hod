package org.zwobble.hod.parser

import com.natpryce.hamkrest.allOf
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.zwobble.hod.isStringLiteral

class ParseStringLiteralTests {
    @TestFactory
    fun canParseStringLiteral(): List<DynamicTest> {
        fun testCase(name: String, source: String, value: String): DynamicTest {
            return DynamicTest.dynamicTest(name) {
                val node = parseString(::parseExpression, source)

                assertThat(node, isStringLiteral(value))
            }
        }

        return listOf(
            testCase("empty string", "\"\"", ""),
            testCase("string with normal characters", "\"abc\"", "abc"),
            testCase("escaped backslashes are decoded", "\"\\\\\"", "\\"),
            testCase("escaped double-quotes are decoded", "\"\\\"\"", "\""),
            testCase("escaped tabs are decoded", "\"\\t\"", "\t"),
            testCase("escaped newlines are decoded", "\"\\n\"", "\n"),
            testCase("escaped carriage returns are decoded", "\"\\r\"", "\r"),
            testCase("hexadecimal unicode escape sequences are decoded", "\"\\u{1B}\"", "\u001B"),
            testCase("hexadecimal unicode escape sequences outside of BMP are decoded", "\"\\u{1D53C}\"", "\uD835\uDD3C"),
            testCase("code point outside of BMP", "\"\uD835\uDD3C\"", "\uD835\uDD3C")
        )
    }

    @Test
    fun whenUnicodeEscapeSequenceInStringIsMissingOpeningBraceThenErrorIsThrown() {
        assertThat(
            { parseString(::parseExpression, "\"\\u001B\"") },
            throws(
                allOf(
                    has(InvalidCodePointError::source, isStringSource(
                        contents = "\"\\u001B\"",
                        index = 3
                    )),
                    has(InvalidCodePointError::message, equalTo("Expected opening brace"))
                )
            )
        )
    }

    @Test
    fun whenUnicodeEscapeSequenceInStringIsMissingClosingBraceThenErrorIsThrown() {
        assertThat(
            { parseString(::parseExpression, "\"\\u{1B\"") },
            throws(
                allOf(
                    has(InvalidCodePointError::source, isStringSource(
                        contents = "\"\\u{1B\"",
                        index = 3
                    )),
                    has(InvalidCodePointError::message, equalTo("Could not find closing brace"))
                )
            )
        )
    }

    @Test
    fun unicodeEscapeSequenceErrorIndexIsRelativeToEntireSource() {
        assertThat(
            { parseString(::parseExpression, "  \"\\u001B\"") },
            throws(
                allOf(
                    has(InvalidCodePointError::source, isStringSource(
                        contents = "  \"\\u001B\"",
                        index = 5
                    )),
                    has(InvalidCodePointError::message, equalTo("Expected opening brace"))
                )
            )
        )
    }

    @Test
    fun unrecognisedEscapeSequenceThrowsError() {
        val source = "\"a\\pb\""
        assertThat(
            { parseString(::parseExpression, source) },
            throws(has(UnrecognisedEscapeSequenceError::escapeSequence, equalTo("\\p")))
        )
    }
}
