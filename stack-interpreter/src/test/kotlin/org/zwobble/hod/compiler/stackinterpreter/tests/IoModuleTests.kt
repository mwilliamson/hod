package org.zwobble.hod.compiler.stackinterpreter.tests

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.ast.Identifier
import org.zwobble.hod.compiler.stackinterpreter.InterpreterString

class IoModuleTests {
    private val moduleName = listOf(Identifier("Core"), Identifier("Io"))

    @Test
    fun printBuiltinWritesToStdout() {
        val world = InMemoryWorld()

        callFunction(
            moduleName = moduleName,
            functionName = "print",
            arguments = listOf(
                InterpreterString("hello")
            ),
            world = world
        )

        assertThat(world.stdout, equalTo("hello"))
    }
}
