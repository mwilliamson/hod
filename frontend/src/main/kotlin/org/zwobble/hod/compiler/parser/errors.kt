package org.zwobble.hod.compiler.parser

import org.zwobble.hod.compiler.ast.Source
import org.zwobble.hod.compiler.typechecker.SourceError

internal open class ParseError(message: String, val location: Source): SourceError(message, location)
