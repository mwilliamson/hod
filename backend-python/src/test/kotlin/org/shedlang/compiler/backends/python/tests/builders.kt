package org.shedlang.compiler.backends.python.tests

import org.shedlang.compiler.backends.python.ast.*
import org.shedlang.compiler.tests.typechecker.anySource

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
