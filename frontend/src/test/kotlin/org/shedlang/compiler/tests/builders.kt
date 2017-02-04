package org.shedlang.compiler.tests

import org.shedlang.compiler.ast.*

fun anySource(): Source {
    return StringSource("<string>", 0)
}

fun ifStatement(
    condition: ExpressionNode = literalBool(true),
    trueBranch: List<StatementNode> = listOf(),
    falseBranch: List<StatementNode> = listOf()
): IfStatementNode {
    return IfStatementNode(condition, trueBranch, falseBranch, anySource())
}

fun expressionStatement(expression: ExpressionNode = expression())
    = ExpressionStatementNode(expression, anySource())

fun expression() = literalString("<expression>")
fun literalBool(value: Boolean) = BooleanLiteralNode(value, anySource())
fun literalInt(value: Int = 0) = IntegerLiteralNode(value, anySource())
fun literalString(value: String) = StringLiteralNode(value, anySource())
fun variableReference(name: String) = VariableReferenceNode(name, anySource())
fun returns(expression: ExpressionNode = expression())
    = ReturnNode(expression, anySource())

fun binaryOperation(
    operator: Operator,
    left: ExpressionNode,
    right: ExpressionNode
) = BinaryOperationNode(
    operator = operator,
    left = left,
    right = right,
    source = anySource()
)

fun functionCall(
    function: ExpressionNode,
    arguments: List<ExpressionNode> = listOf()
) = FunctionCallNode(
    function = function,
    arguments = arguments,
    source = anySource()
)

fun function(
    name: String = "f",
    arguments: List<ArgumentNode> = listOf(),
    returnType: TypeNode = typeReference("Unit"),
    body: List<StatementNode> = listOf()
) = FunctionNode(
    name = name,
    arguments = arguments,
    returnType = returnType,
    body = body,
    source = anySource()
)

fun argument(
    name: String,
    type: TypeNode = typeReference("Int")
) = ArgumentNode(
    name = name,
    type = type,
    source = anySource()
)

fun module(
    body: List<FunctionNode>
) = ModuleNode(
    name = "",
    body = body,
    source = anySource()
)

fun typeReference(name: String) = TypeReferenceNode(name, anySource())