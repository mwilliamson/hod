package org.zwobble.hod.compiler.stackinterpreter

import org.zwobble.hod.compiler.ast.Identifier

internal val intToStringModule = createNativeModule(
    name = listOf(Identifier("Core"), Identifier("IntToString")),
    dependencies = listOf(),
    fields = listOf(
        Identifier("intToString") to InterpreterBuiltinFunction { state, arguments ->
            val int = (arguments[0] as InterpreterInt).value
            state.pushTemporary(InterpreterString(int.toString()))
        }
    )
)
