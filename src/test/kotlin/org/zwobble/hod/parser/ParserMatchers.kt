package org.zwobble.hod.parser

import com.natpryce.hamkrest.*
import org.zwobble.hod.Source
import org.zwobble.hod.StringSource

internal fun isStringSource(contents: String, index: Int): Matcher<Source> {
    return cast(
        allOf(
            has(StringSource::contents, equalTo(contents)),
            has(StringSource::characterIndex, equalTo(index))
        )
    )
}
