package org.zwobble.hod.parser

import org.zwobble.hod.*
import java.nio.CharBuffer
import java.util.regex.Pattern

internal fun parseCompilationUnit(tokens: TokenIterator<TokenType>): CompilationUnitNode {
    val source = tokens.location()

    val imports = parseImports(tokens)

    return CompilationUnitNode(
        imports = imports,
        statements = listOf(),
        source = source
    )
}

private fun parseImports(tokens: TokenIterator<TokenType>): List<ImportNode> {
    return parseMany(
        parseElement = { parseImport(tokens) },
        isEnd = { !tokens.isNext(TokenType.KEYWORD_IMPORT) },
        allowZero = true
    )
}

private fun parseImport(tokens: TokenIterator<TokenType>): ImportNode {
    val source = tokens.location()

    tokens.skip(TokenType.KEYWORD_IMPORT)
    val target = parseTarget(tokens)
    tokens.skip(TokenType.KEYWORD_FROM)
    val path = parseImportPath(tokens)
    tokens.skip(TokenType.SYMBOL_SEMICOLON)

    return ImportNode(
        target = target,
        path = path,
        source = source
    )
}

internal fun parseImportPath(tokens: TokenIterator<TokenType>): String {
    return parseMany(
        parseElement = { tokens.nextValue(TokenType.IDENTIFIER) },
        parseSeparator = { tokens.skip(TokenType.SYMBOL_DOT) },
        isEnd = { tokens.isNext(TokenType.SYMBOL_SEMICOLON) },
        allowZero = false,
        allowTrailingSeparator = false
    ).joinToString(".")
}

internal fun parseCompilationUnitStatement(tokens: TokenIterator<TokenType>): CompilationUnitStatementNode {
    val source = tokens.location()
    tokens.skip(TokenType.KEYWORD_VAL)
    val target = parseTarget(tokens)
    tokens.skip(TokenType.SYMBOL_EQUALS)
    val expression = parseExpression(tokens)
    tokens.skip(TokenType.SYMBOL_SEMICOLON)
    return ValNode(
        target = target,
        expression = expression,
        source = source
    )
}

internal fun parseExpression(tokens: TokenIterator<TokenType>): ExpressionNode {
    val source = tokens.location()

    if (tokens.trySkip(TokenType.KEYWORD_TRUE)) {
        return BoolLiteralNode(value = true, source = source)
    } else if (tokens.trySkip(TokenType.KEYWORD_FALSE)) {
        return BoolLiteralNode(value = false, source = source)
    } else if (tokens.isNext(TokenType.IDENTIFIER)) {
        val name = tokens.nextValue(TokenType.IDENTIFIER)
        return ReferenceNode(name, source)
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

private fun parseTarget(tokens: TokenIterator<TokenType>): String {
    return tokens.nextValue(TokenType.IDENTIFIER)
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

private fun <T> parseMany(
    parseElement: () -> T,
    parseSeparator: () -> Unit = { },
    isEnd: () -> Boolean,
    allowTrailingSeparator: Boolean = false,
    allowZero: Boolean
) : List<T> {
    return parseMany(
        parseElement = parseElement,
        parseSeparator = parseSeparator,
        isEnd = isEnd,
        allowTrailingSeparator = allowTrailingSeparator,
        allowZero = allowZero,
        initial = mutableListOf<T>(),
        reduce = { elements, element -> elements.add(element); elements }
    )
}

private fun <T, R> parseMany(
    parseElement: () -> T,
    parseSeparator: () -> Unit,
    isEnd: () -> Boolean,
    allowTrailingSeparator: Boolean,
    allowZero: Boolean,
    initial: R,
    reduce: (R, T) -> R
) : R {
    if (allowZero && isEnd()) {
        return initial
    }

    var result = initial

    while (true) {
        result = reduce(result, parseElement())
        if (isEnd()) {
            return result
        }
        parseSeparator()
        if (allowTrailingSeparator && isEnd()) {
            return result
        }
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
