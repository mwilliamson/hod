package org.shedlang.compiler.typechecker

import org.shedlang.compiler.ast.*

interface Type

object UnitType: Type
object BoolType : Type
object IntType : Type
object AnyType : Type
class MetaType(val type: Type): Type

data class FunctionType(val arguments: List<Type>, val returns: Type): Type

class TypeContext(
    val returnType: Type?,
    private val variables: MutableMap<Int, Type>,
    private val resolutionContext: VariableReferences
) {
    fun typeOf(reference: ReferenceNode): Type? = variables[resolutionContext[reference]]

    fun addTypes(types: Map<Int, Type>) {
        variables += types
    }

    fun enterFunction(returnType: Type): TypeContext {
        return TypeContext(
            returnType = returnType,
            variables = variables,
            resolutionContext = resolutionContext
        )
    }
}

open class TypeCheckError(message: String?, val location: Source) : Exception(message)
internal class BadStatementError(location: Source)
    : TypeCheckError("Bad statement", location)
class UnresolvedReferenceError(val name: String, location: Source)
    : TypeCheckError("Unresolved reference: " + name, location)
class UnexpectedTypeError(val expected: Type, val actual: Type, location: Source)
    : TypeCheckError("Expected type $expected but was $actual", location)
class WrongNumberOfArgumentsError(val expected: Int, val actual: Int, location: Source)
    : TypeCheckError("Expected $expected arguments, but got $actual", location)
class ReturnOutsideOfFunctionError(location: Source)
    : TypeCheckError("Cannot return outside of a function", location)

fun typeCheck(module: ModuleNode, context: TypeContext) {
    val functionTypes = module.body.associateBy(
        FunctionNode::nodeId,
        { function -> inferType(function, context) }
    )

    context.addTypes(functionTypes)

    for (statement in module.body) {
        typeCheck(statement, context)
    }
}

fun inferType(function: FunctionNode, context: TypeContext): FunctionType {
    val argumentTypes = function.arguments.map(
        { argument -> evalType(argument.type, context) }
    )
    val returnType = evalType(function.returnType, context)
    return FunctionType(argumentTypes, returnType)
}

fun typeCheck(function: FunctionNode, context: TypeContext) {
    val argumentTypes = function.arguments.associateBy(
        ArgumentNode::nodeId,
        { argument -> evalType(argument.type, context) }
    )
    val returnType = evalType(function.returnType, context)
    context.addTypes(argumentTypes)
    val bodyContext = context.enterFunction(returnType = returnType)
    typeCheck(function.body, bodyContext)
}

fun evalType(type: TypeNode, context: TypeContext): Type {
    return type.accept(object : TypeNode.Visitor<Type> {
        override fun visit(node: TypeReferenceNode): Type {
            val metaType = context.typeOf(node)
            return when (metaType) {
                null -> throw UnresolvedReferenceError(node.name, node.source)
                is MetaType -> metaType.type
                else -> throw UnexpectedTypeError(
                    expected = MetaType(AnyType),
                    actual = metaType,
                    location = node.source
                )
            }
        }
    })
}

fun typeCheck(statement: StatementNode, context: TypeContext) {
    statement.accept(object : StatementNodeVisitor<Unit> {
        override fun visit(node: BadStatementNode) {
            throw BadStatementError(node.source)
        }

        override fun visit(node: IfStatementNode) {
            verifyType(node.condition, context, expected = BoolType)
            typeCheck(node.trueBranch, context)
            typeCheck(node.falseBranch, context)
        }

        override fun visit(node: ReturnNode): Unit {
            if (context.returnType == null) {
                throw ReturnOutsideOfFunctionError(node.source)
            } else {
                verifyType(node.expression, context, expected = context.returnType)
            }
        }

        override fun visit(node: ExpressionStatementNode) {
            typeCheck(node.expression, context)
        }
    })
}

private fun typeCheck(statements: List<StatementNode>, context: TypeContext) {
    for (statement in statements) {
        typeCheck(statement, context)
    }
}

private fun typeCheck(expression: ExpressionNode, context: TypeContext): Unit {
    inferType(expression, context)
}

fun inferType(expression: ExpressionNode, context: TypeContext) : Type {
    return expression.accept(object : ExpressionNodeVisitor<Type> {
        override fun visit(node: BooleanLiteralNode): Type {
            return BoolType
        }

        override fun visit(node: IntegerLiteralNode): Type {
            return IntType
        }

        override fun visit(node: VariableReferenceNode): Type {
            val type = context.typeOf(node)
            if (type == null) {
                throw UnresolvedReferenceError(node.name, node.source)
            } else {
                return type
            }
        }

        override fun visit(node: BinaryOperationNode): Type {
            verifyType(node.left, context, expected = IntType)
            verifyType(node.right, context, expected = IntType)
            return when (node.operator) {
                Operator.EQUALS -> BoolType
                Operator.ADD, Operator.SUBTRACT, Operator.MULTIPLY -> IntType
            }
        }

        override fun visit(node: FunctionCallNode): Type {
            val functionType = inferType(node.function, context)
            return when (functionType) {
                is FunctionType -> {
                    node.arguments.zip(functionType.arguments, { arg, argType -> verifyType(arg, context, expected = argType) })
                    if (functionType.arguments.size != node.arguments.size) {
                        throw WrongNumberOfArgumentsError(
                            expected = functionType.arguments.size,
                            actual = node.arguments.size,
                            location = node.source
                        )
                    }
                    functionType.returns
                }
                else -> {
                    val argumentTypes = node.arguments.map { argument -> inferType(argument, context) }
                    throw UnexpectedTypeError(
                        expected = FunctionType(argumentTypes, AnyType),
                        actual = functionType,
                        location = node.function.source
                    )
                }
            }
        }
    })
}

private fun verifyType(expression: ExpressionNode, context: TypeContext, expected: Type) {
    val type = inferType(expression, context)
    verifyType(expected = expected, actual = type, location = expression.source)
}

private fun verifyType(expected: Type, actual: Type, location: Source) {
    if (actual != expected) {
        throw UnexpectedTypeError(expected = expected, actual = actual, location = location)
    }
}