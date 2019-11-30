package org.zwobble.hod

internal interface Source {
    fun describe(): String
}

internal object NullSource: Source {
    override fun describe(): String {
        return "null"
    }
}

internal data class StringSource(
    val filename: String,
    val contents: String,
    val characterIndex: Int
) : Source {
    fun at(index: Int): StringSource {
        return StringSource(filename, contents, characterIndex + index)
    }

    override fun describe(): String {
        val lines = contents.splitToSequence("\n")
        var position = 0

        for ((lineIndex, line) in lines.withIndex()) {
            val nextLinePosition = position + line.length + 1
            if (nextLinePosition > characterIndex || nextLinePosition >= contents.length) {
                return context(
                    line,
                    lineIndex = lineIndex,
                    columnIndex = characterIndex - position
                )
            }
            position = nextLinePosition
        }
        throw Exception("should be impossible (but evidently isn't)")
    }

    private fun context(line: String, lineIndex: Int, columnIndex: Int): String {
        return "${filename}:${lineIndex + 1}:${columnIndex + 1}\n${line}\n${" ".repeat(columnIndex)}^"
    }
}
