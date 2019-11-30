package org.zwobble.hod.compiler.tests.parser

import com.natpryce.hamkrest.allOf
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.ast.ImportPath
import org.zwobble.hod.compiler.ast.ModuleNode
import org.zwobble.hod.compiler.parser.parse
import org.zwobble.hod.compiler.tests.isIdentifier
import org.zwobble.hod.compiler.tests.isSequence

class ParseModuleTests {
    @Test
    fun minimalModuleIsEmptyString() {
        val source = "".trimIndent()

        val node = parse("<string>", source)

        assertThat(node, allOf(
            has(ModuleNode::imports, equalTo(listOf())),
            has(ModuleNode::body, equalTo(listOf()))
        ))
    }

    @Test
    fun moduleCanHaveExports() {
        val source = """
            export x, y;
        """.trimIndent()

        val node = parse("<string>", source)

        assertThat(node, has(ModuleNode::exports, isSequence(
            isExport("x"),
            isExport("y")
        )))
    }

    @Test
    fun moduleCanHaveImports() {
        val source = """
            import y from .x.y;
        """.trimIndent()

        val node = parse("<string>", source)

        assertThat(node, has(ModuleNode::imports, isSequence(
            isImport(
                target = isTargetVariable("y"),
                path = equalTo(ImportPath.relative(listOf("x", "y")))
            )
        )))
    }

    @Test
    fun moduleCanHaveStatements() {
        val source = """
            fun f() -> Unit {
            }

            fun g() -> Unit {
            }
        """.trimIndent()

        val node = parse("<string>", source)

        assertThat(node, allOf(
            has(ModuleNode::body, isSequence(
                isFunctionDeclaration(name = isIdentifier("f")),
                isFunctionDeclaration(name = isIdentifier("g"))
            ))
        ))
    }
}
