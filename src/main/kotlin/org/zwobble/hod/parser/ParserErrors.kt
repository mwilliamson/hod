package org.zwobble.hod.parser

import org.zwobble.hod.Source
import org.zwobble.hod.SourceError

internal open class ParseError(message: String, val location: Source): SourceError(message, location)
