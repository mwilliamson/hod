package org.zwobble.hod.parser

import org.zwobble.hod.Source
import org.zwobble.hod.SourceError

internal open class ParseError(message: String, source: Source): SourceError(message, source)

internal open class InvalidCodePointError(
    message: String,
    source: Source
): ParseError(message, source = source)

internal class UnrecognisedEscapeSequenceError(
    val escapeSequence: String,
    source: Source
): InvalidCodePointError(
    source = source,
    message = "Unrecognised escape sequence"
)
