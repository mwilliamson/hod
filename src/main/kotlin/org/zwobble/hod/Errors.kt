package org.zwobble.hod

internal open class SourceError(message: String?, val source: Source): Exception(message)
