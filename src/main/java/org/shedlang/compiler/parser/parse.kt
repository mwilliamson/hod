package org.shedlang.compiler.parser

import org.shedlang.compiler.ast.*

internal fun parse(filename: String, input: String): ModuleNode {
    val tokens = tokenise(input)
        .filter { token -> token.tokenType != TokenType.WHITESPACE }
        .plus(Token(input.length, TokenType.END, ""))
    val tokenIterator = TokenIterator(filename, tokens)
    val module = ::parseModule.parse(tokenIterator)
    tokenIterator.skip(TokenType.END)
    return module
}

internal fun <T> ((SourceLocation, TokenIterator<TokenType>) -> T).parse(tokens: TokenIterator<TokenType>): T {
    val location = tokens.location()
    return this(location, tokens)
}

internal fun parseModule(location: SourceLocation, tokens: TokenIterator<TokenType>): ModuleNode {
    val moduleName = parseModuleNameDeclaration(tokens)
    val body = parseManyNodes(
        ::tryParseFunction,
        tokens
    )
    return ModuleNode(moduleName, body, location)
}

private fun parseModuleNameDeclaration(tokens: TokenIterator<TokenType>): String {
    tokens.skip(TokenType.KEYWORD, "module")
    val moduleName = parseModuleName(tokens)
    tokens.skip(TokenType.SYMBOL, ";")
    return moduleName
}

internal fun parseModuleName(tokens: TokenIterator<TokenType>): String {
    return parseOneOrMoreWithSeparator(
        { tokens -> tokens.nextValue(TokenType.IDENTIFIER) },
        { tokens -> tokens.trySkip(TokenType.SYMBOL, ".") },
        tokens
    ).joinToString(".")
}

internal fun tryParseFunction(location: SourceLocation, tokens: TokenIterator<TokenType>): FunctionNode? {
    if (!tokens.trySkip(TokenType.KEYWORD, "fun")) {
        return null
    }

    val name = tokens.nextValue(TokenType.IDENTIFIER)

    tokens.skip(TokenType.SYMBOL, "(")
    val arguments = parseZeroOrMoreNodes(
        parseElement = ::parseFormalArgument,
        parseSeparator = {tokens -> tokens.skip(TokenType.SYMBOL, ",")},
        isEnd = { tokens.isNext(TokenType.SYMBOL, ")") },
        tokens = tokens
    )
    tokens.skip(TokenType.SYMBOL, ")")
    tokens.skip(TokenType.SYMBOL, ":")
    val returnType = ::parseType.parse(tokens)
    tokens.skip(TokenType.SYMBOL, "{")
    val body = parseMany(
        ::tryParseFunctionStatement,
        tokens
    )
    tokens.skip(TokenType.SYMBOL, "}")

    return FunctionNode(
        name = name,
        arguments = arguments,
        returnType = returnType,
        body = body,
        location = location
    )
}

private fun parseFormalArgument(location: SourceLocation, tokens: TokenIterator<TokenType>) : ArgumentNode {
    val name = tokens.nextValue(TokenType.IDENTIFIER)
    tokens.skip(TokenType.SYMBOL, ":")
    val type = ::parseType.parse(tokens)
    return ArgumentNode(name, type, location)
}

private fun tryParseFunctionStatement(tokens: TokenIterator<TokenType>) : StatementNode? {
    return ::tryParseReturn.parse(tokens)
}

internal fun tryParseReturn(location: SourceLocation, tokens: TokenIterator<TokenType>) : ReturnNode? {
    if (tokens.trySkip(TokenType.KEYWORD, "return")) {
        val expression = parseExpression(tokens)
        tokens.skip(TokenType.SYMBOL, ";")
        return ReturnNode(expression, location)
    } else {
        return null
    }
}

internal fun parseExpression(tokens: TokenIterator<TokenType>) : ExpressionNode {
    return parseExpression(tokens, precedence = Int.MIN_VALUE)
}

private fun parseExpression(tokens: TokenIterator<TokenType>, precedence: Int) : ExpressionNode {
    var left = ::parsePrimaryExpression.parse(tokens)

    while (true) {
        val next = tokens.peek()
        if (next.tokenType == TokenType.SYMBOL) {
            val operator = when (next.value) {
                "==" -> Operator.EQUALS
                "+" -> Operator.ADD
                "-" -> Operator.SUBTRACT
                "*" -> Operator.MULTIPLY
                else -> null
            }
            if (operator == null || operator.precedence < precedence) {
                return left
            } else {
                tokens.skip()
                val right = parseExpression(tokens, operator.precedence + 1)
                left = BinaryOperationNode(operator, left, right, left.location)
            }
        } else {
            return left
        }
    }
}

internal fun parsePrimaryExpression(location: SourceLocation, tokens: TokenIterator<TokenType>) : ExpressionNode {
    val token = tokens.next();
    return when (token.tokenType) {
        TokenType.INTEGER -> IntegerLiteralNode(token.value.toInt(), location)
        TokenType.IDENTIFIER -> VariableReferenceNode(token.value, location)
        else -> throw UnexpectedTokenException(
            location = tokens.location(),
            expected = "primary expression",
            actual = tokens.peek().describe()
        )
    }
}

internal fun parseType(location: SourceLocation, tokens: TokenIterator<TokenType>) : TypeNode {
    val name = tokens.nextValue(TokenType.IDENTIFIER)
    return TypeReferenceNode(name, location)
}

private fun <T> parseManyNodes(
    parseElement: (SourceLocation, TokenIterator<TokenType>) -> T?,
    tokens: TokenIterator<TokenType>
) : List<T> {
    return parseMany(
        { tokens -> parseElement.parse(tokens) },
        tokens
    )
}

private fun <T> parseMany(
    parseElement: (TokenIterator<TokenType>) -> T?,
    tokens: TokenIterator<TokenType>
): List<T> {
    val elements: MutableList<T> = mutableListOf()
    while (true) {
        val element = parseElement(tokens)
        if (element == null) {
            return elements
        } else {
            elements.add(element)
        }
    }
}

private fun <T> parseOneOrMoreNodesWithSeparator(
    parseElement: (SourceLocation, TokenIterator<TokenType>) -> T,
    parseSeparator: (TokenIterator<TokenType>) -> Boolean,
    tokens: TokenIterator<TokenType>
) : List<T> {
    return parseOneOrMoreWithSeparator(
        {tokens -> parseElement.parse(tokens)},
        parseSeparator,
        tokens
    )
}

private fun <T> parseOneOrMoreWithSeparator(
    parseElement: (TokenIterator<TokenType>) -> T,
    parseSeparator: (TokenIterator<TokenType>) -> Boolean,
    tokens: TokenIterator<TokenType>
): List<T> {
    val elements = mutableListOf(parseElement(tokens))
    while (parseSeparator(tokens)) {
        elements.add(parseElement(tokens))
    }
    return elements
}

private fun <T> parseZeroOrMoreNodes(
    parseElement: (SourceLocation, TokenIterator<TokenType>) -> T,
    parseSeparator: (TokenIterator<TokenType>) -> Unit,
    isEnd: (TokenIterator<TokenType>) -> Boolean,
    tokens: TokenIterator<TokenType>
) : List<T> {
    return parseZeroOrMore(
        { tokens -> parseElement.parse(tokens) },
        parseSeparator,
        isEnd,
        tokens
    )
}

private fun <T> parseZeroOrMore(
    parseElement: (TokenIterator<TokenType>) -> T,
    parseSeparator: (TokenIterator<TokenType>) -> Unit,
    isEnd: (TokenIterator<TokenType>) -> Boolean,
    tokens: TokenIterator<TokenType>
) : List<T> {
    val elements = mutableListOf<T>()

    while (!isEnd(tokens)) {
        if (elements.isNotEmpty()) {
            parseSeparator(tokens)
        }
        elements.add(parseElement(tokens))
    }

    return elements
}
