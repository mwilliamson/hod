package org.shedlang.compiler.backends.python.tests

import org.shedlang.compiler.backends.python.ast.*
import org.shedlang.compiler.tests.typechecker.anySource

fun pythonModule(body: List<PythonStatementNode>)
    = PythonModuleNode(body = body, source = anySource())

fun pythonFunction(
    name: String,
    arguments: List<String> = listOf(),
    body: List<PythonStatementNode> = listOf()
) = PythonFunctionNode(
    name = name,
    arguments = arguments,
    body = body,
    source = anySource()
)

fun pythonReturn(expression: PythonExpressionNode)
    = PythonReturnNode(expression, source = anySource())

fun pythonExpressionStatement(expression: PythonExpressionNode)
    = PythonExpressionStatementNode(expression, source = anySource())

fun pythonIf(
    condition: PythonExpressionNode,
    trueBranch: List<PythonStatementNode>,
    falseBranch: List<PythonStatementNode> = listOf()
) = PythonIfStatementNode(
    condition = condition,
    trueBranch = trueBranch,
    falseBranch = falseBranch,
    source = anySource()
)

fun pythonLiteralBoolean(value: Boolean)
    = PythonBooleanLiteralNode(value, source = anySource())

fun pythonLiteralInt(value: Int)
    = PythonIntegerLiteralNode(value, source = anySource())

fun pythonVariableReference(name: String)
    = PythonVariableReferenceNode(name, source = anySource())

fun pythonBinaryOperation(
    operator: PythonOperator,
    left: PythonExpressionNode,
    right: PythonExpressionNode
) = PythonBinaryOperationNode(
    operator = operator,
    left = left,
    right = right,
    source = anySource()
)

fun pythonFunctionCall(
    function: PythonExpressionNode,
    arguments: List<PythonExpressionNode>
) = PythonFunctionCallNode(
    function = function,
    arguments = arguments,
    source = anySource()
)
