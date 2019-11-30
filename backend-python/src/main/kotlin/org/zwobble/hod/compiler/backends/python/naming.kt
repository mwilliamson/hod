package org.zwobble.hod.compiler.backends.python

import org.zwobble.hod.compiler.ast.Identifier

internal fun pythoniseName(originalName: Identifier): String {
    return pythoniseName(originalName.value)
}

private fun pythoniseName(originalName: String): String {
    val casedName = if (originalName[0].isUpperCase()) {
        originalName
    } else {
        camelCaseToSnakeCase(originalName)
        // TODO: remove $ and . from identifiers
    }.replace("$", "_").replace(".", "_")
    return if (isKeyword(casedName)) {
        casedName + "_"
    } else {
        casedName
    }
}


private fun camelCaseToSnakeCase(name: String): String {
    return Regex("\\p{javaUpperCase}").replace(name, { char -> "_" + char.value.toLowerCase() })
}

private fun isKeyword(name: String): Boolean {
    return reservedNames.contains(name)
}

private val pythonKeywords = setOf(
    "False",
    "None",
    "True",
    "and",
    "as",
    "assert",
    "break",
    "class",
    "continue",
    "def",
    "del",
    "elif",
    "else",
    "except",
    "exec",
    "finally",
    "for",
    "from",
    "global",
    "if",
    "import",
    "in",
    "is",
    "lambda",
    "nonlocal",
    "not",
    "or",
    "pass",
    "raise",
    "return",
    "try",
    "while",
    "with",
    "yield"
)

private val pythonGlobals = setOf(
    "object",
    "str"
)

private val reservedNames = pythonKeywords + pythonGlobals
