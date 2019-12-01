package org.zwobble.hod.parser

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.zwobble.hod.isCompilationUnit
import org.zwobble.hod.isImport
import org.zwobble.hod.util.isSequence

class ParseCompilationUnitTests {
    @Test
    fun emptyCompilationUnitHasNoImports() {
        val source = ""

        val node = parseString(::parseCompilationUnit, source)

        assertThat(node, isCompilationUnit(
            imports = isSequence()
        ))
    }

    @Test
    fun canParseImports() {
        val source = """
            import One from Example.One;
            import Other from Example.Two;
        """.trimIndent()

        val node = parseString(::parseCompilationUnit, source)

        assertThat(node, isCompilationUnit(
            imports = isSequence(
                isImport(target = equalTo("One"), path = equalTo("Example.One")),
                isImport(target = equalTo("Other"), path = equalTo("Example.Two"))
            )
        ))
    }
}
