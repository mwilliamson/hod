package org.zwobble.hod.compiler.stackinterpreter.tests

import kotlinx.collections.immutable.PersistentList
import org.zwobble.hod.compiler.EMPTY_TYPES
import org.zwobble.hod.compiler.ResolvedReferences
import org.zwobble.hod.compiler.Types
import org.zwobble.hod.compiler.ast.Block
import org.zwobble.hod.compiler.ast.ExpressionNode
import org.zwobble.hod.compiler.backends.CodeInspector
import org.zwobble.hod.compiler.backends.SimpleCodeInspector
import org.zwobble.hod.compiler.stackinterpreter.*
import org.zwobble.hod.compiler.typechecker.ResolvedReferencesMap

internal fun loader(
    inspector: CodeInspector = SimpleCodeInspector(),
    references: ResolvedReferences = ResolvedReferencesMap.EMPTY,
    types: Types = EMPTY_TYPES
): Loader {
    return Loader(inspector = inspector, references = references, types = types)
}

internal fun evaluateBlock(block: Block, references: ResolvedReferences): InterpreterValue {
    val instructions = loader(references = references).loadBlock(block)
    return executeInstructions(instructions)
}

internal fun evaluateExpression(node: ExpressionNode, types: Types = EMPTY_TYPES): InterpreterValue {
    val instructions = loader(types = types).loadExpression(node)
    return executeInstructions(instructions)
}

internal fun executeInstructions(
    instructions: PersistentList<Instruction>,
    image: Image = Image.EMPTY,
    variables: Map<Int, InterpreterValue> = mapOf(),
    world: World = NullWorld
): InterpreterValue {
    val finalState = org.zwobble.hod.compiler.stackinterpreter.executeInstructions(
        instructions,
        image = image,
        defaultVariables = variables,
        world = world
    )
    return finalState.popTemporary().second
}
