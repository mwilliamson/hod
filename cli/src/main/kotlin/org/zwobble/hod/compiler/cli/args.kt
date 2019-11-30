package org.zwobble.hod.compiler.cli

import com.xenomachina.argparser.ArgParser
import org.zwobble.hod.compiler.backends.Backend


private val backends = mapOf(
    "python" to org.zwobble.hod.compiler.backends.python.backend,
    "javascript" to org.zwobble.hod.compiler.backends.javascript.backend
)

internal fun ArgParser.hodSource(): ArgParser.Delegate<String> {
    return this.positional("SOURCE", help = "path to source root")
}

internal fun ArgParser.hodBackends(): ArgParser.Delegate<Backend> {
    return this.choices("--backend", argName = "BACKEND", help = "backend to generate code with", choices = backends)
}


internal fun <T> ArgParser.choices(
    vararg names: String,
    argName: String,
    help: String,
    choices: Map<String, T>
): ArgParser.Delegate<T> {
    return option<T>(
        *names,
        help = choices.keys.joinToString("|") + "\n" + help,
        argNames = listOf(argName),
        handler = {
            choices[arguments.first()]!!
        }
    )
}
