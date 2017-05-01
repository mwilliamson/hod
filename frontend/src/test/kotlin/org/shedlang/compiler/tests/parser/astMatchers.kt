package org.shedlang.compiler.tests.parser

import com.natpryce.hamkrest.*
import org.shedlang.compiler.ast.*
import org.shedlang.compiler.tests.allOf
import org.shedlang.compiler.tests.isSequence

internal fun isImport(path: Matcher<ImportPath>) = has(ImportNode::path, path)

inline internal fun <reified T: ExpressionNode> isReturn(
    expression: Matcher<T>
): Matcher<StatementNode> {
    return cast(has(ReturnNode::expression, cast(expression)))
}

inline internal fun <reified T: ExpressionNode> isExpressionStatement(
    expression: Matcher<T>
): Matcher<StatementNode> {
    return cast(has(ExpressionStatementNode::expression, cast(expression)))
}

inline internal fun <reified T: ExpressionNode> isVal(
    name: Matcher<String>,
    expression: Matcher<T>
): Matcher<StatementNode> {
    return cast(allOf(
        has(ValNode::name, name),
        has(ValNode::expression, cast(expression))
    ))
}

internal fun isShape(
    name: Matcher<String> = anything,
    fields: Matcher<List<ShapeFieldNode>>
): Matcher<ModuleStatementNode> {
    return cast(allOf(
        has(ShapeNode::name, name),
        has(ShapeNode::fields, fields)
    ))
}

internal fun isShapeField(
    name: Matcher<String>,
    type: Matcher<TypeNode>
) = allOf(
    has(ShapeFieldNode::name, name),
    has(ShapeFieldNode::type, type)
)

internal fun isUnion(
    name: Matcher<String> = anything,
    members: Matcher<List<TypeNode>>
): Matcher<ModuleStatementNode> {
    return cast(allOf(
        has(UnionNode::name, name),
        has(UnionNode::members, members)
    ))
}

internal fun isFunction(name: Matcher<String>): Matcher<ModuleStatementNode> {
    return cast(has(FunctionNode::name, name))
}

internal fun isTypeParameter(name: Matcher<String>): Matcher<TypeParameterNode> {
    return has(TypeParameterNode::name, name)
}

internal fun isBinaryOperation(
    operator: Operator,
    left: Matcher<ExpressionNode>,
    right: Matcher<ExpressionNode>
) : Matcher<ExpressionNode> {
    return cast(allOf(
        has(BinaryOperationNode::operator, equalTo(operator)),
        has(BinaryOperationNode::left, left),
        has(BinaryOperationNode::right, right)
    ))
}

internal fun isIsOperation(
    expression: Matcher<ExpressionNode>,
    type: Matcher<TypeNode>
) : Matcher<ExpressionNode> {
    return cast(allOf(
        has(IsNode::expression, expression),
        has(IsNode::type, type)
    ))
}

internal fun isCall(
    receiver: Matcher<ExpressionNode>,
    positionalArguments: Matcher<List<ExpressionNode>>,
    namedArguments: Matcher<List<CallNamedArgumentNode>> = isSequence()
) : Matcher<ExpressionNode> {
    return cast(allOf(
        has(CallNode::receiver, receiver),
        has(CallNode::positionalArguments, positionalArguments),
        has(CallNode::namedArguments, namedArguments)
    ))
}

internal fun isCallNamedArgument(
    name: Matcher<String>,
    expression: Matcher<ExpressionNode>
) = allOf(
    has(CallNamedArgumentNode::name, name),
    has(CallNamedArgumentNode::expression, expression)
)

internal fun isFieldAccess(
    receiver: Matcher<ExpressionNode>,
    fieldName: Matcher<String>
): Matcher<ExpressionNode> = cast(allOf(
    has(FieldAccessNode::receiver, receiver),
    has(FieldAccessNode::fieldName, fieldName)
))

internal fun isVariableReference(name: String) = cast(has(VariableReferenceNode::name, equalTo(name)))

internal fun isTypeReference(name: String) = cast(has(TypeReferenceNode::name, equalTo(name)))
