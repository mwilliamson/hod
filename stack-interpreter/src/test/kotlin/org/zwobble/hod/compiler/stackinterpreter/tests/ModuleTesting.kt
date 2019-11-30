package org.zwobble.hod.compiler.stackinterpreter.tests

import kotlinx.collections.immutable.persistentListOf
import org.zwobble.hod.compiler.Module
import org.zwobble.hod.compiler.ModuleSet
import org.zwobble.hod.compiler.ast.Identifier
import org.zwobble.hod.compiler.findRoot
import org.zwobble.hod.compiler.readPackage
import org.zwobble.hod.compiler.stackinterpreter.*
import org.zwobble.hod.compiler.tests.moduleType

internal fun callFunction(
    moduleName: List<Identifier>,
    functionName: String,
    arguments: List<InterpreterValue>,
    world: World = NullWorld
): InterpreterValue {
    val instructions = persistentListOf(
        InitModule(moduleName),
        LoadModule(moduleName),
        FieldAccess(Identifier(functionName))
    )
        .addAll(arguments.map { argument -> PushValue(argument) })
        .add(Call(positionalArgumentCount = arguments.size, namedArgumentNames = listOf()))

    val optionsModules = readPackage(
        base = findRoot().resolve("stdlib"),
        name = listOf(Identifier("Stdlib"), Identifier("Options"))
    ).modules

    val moduleSet = ModuleSet(optionsModules + listOf(
        Module.Native(name = moduleName, type = moduleType())
    ))
    val image = loadModuleSet(moduleSet)

    return executeInstructions(instructions, image = image, world = world)
}
