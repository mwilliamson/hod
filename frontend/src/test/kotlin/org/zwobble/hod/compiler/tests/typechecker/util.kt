package org.zwobble.hod.compiler.tests.typechecker

import org.zwobble.hod.compiler.TypesMap
import org.zwobble.hod.compiler.ast.ExpressionNode
import org.zwobble.hod.compiler.ast.ReferenceNode
import org.zwobble.hod.compiler.ast.VariableBindingNode
import org.zwobble.hod.compiler.typechecker.typeCheck
import org.zwobble.hod.compiler.types.Type

internal fun captureTypes(
    expression: ExpressionNode,
    references: Map<ReferenceNode, VariableBindingNode> = mapOf(),
    referenceTypes: Map<ReferenceNode, Type> = mapOf(),
    types: Map<VariableBindingNode, Type> = mapOf()
): TypesMap {
    val expressionTypes = mutableMapOf<Int, Type>()
    val typeContext = typeContext(
        expressionTypes = expressionTypes,
        referenceTypes = referenceTypes,
        references = references,
        types = types
    )

    typeCheck(expression, typeContext)
    typeContext.undefer()

    return TypesMap(
        discriminators = mapOf(),
        expressionTypes = expressionTypes,
        variableTypes = mapOf()
    )
}
