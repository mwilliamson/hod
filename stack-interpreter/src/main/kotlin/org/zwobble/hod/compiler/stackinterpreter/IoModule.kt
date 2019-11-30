package org.zwobble.hod.compiler.stackinterpreter

import org.zwobble.hod.compiler.ast.Identifier

internal val ioModule = createNativeModule(
    name = listOf(Identifier("Core"), Identifier("Io")),
    dependencies = listOf(),
    fields = listOf(
        Identifier("print") to InterpreterBuiltinFunction { state, arguments ->
            val value = (arguments[0] as InterpreterString).value
            state.print(value)
            state.pushTemporary(InterpreterUnit)
        }
    )
)
