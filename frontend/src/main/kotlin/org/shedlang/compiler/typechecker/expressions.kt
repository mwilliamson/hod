package org.shedlang.compiler.typechecker

import org.shedlang.compiler.ast.*
import org.shedlang.compiler.frontend.types.union
import org.shedlang.compiler.types.*

private data class OperationType(val operator: Operator, val left: Type, val right: Type)

internal fun typeCheck(expression: ExpressionNode, context: TypeContext): Unit {
    inferType(expression, context)
}

internal fun verifyType(expression: ExpressionNode, context: TypeContext, expected: Type) {
    val type = inferType(expression, context)
    verifyType(expected = expected, actual = type, source = expression.source)
}

internal fun inferType(expression: ExpressionNode, context: TypeContext, hint: Type? = null) : Type {
    val type = expression.accept(object : ExpressionNode.Visitor<Type> {
        override fun visit(node: UnitLiteralNode) = UnitType
        override fun visit(node: BooleanLiteralNode) = BoolType
        override fun visit(node: IntegerLiteralNode) = IntType
        override fun visit(node: StringLiteralNode) = StringType
        override fun visit(node: CharacterLiteralNode) = CharType
        override fun visit(node: VariableReferenceNode) = inferReferenceType(node, context)

        override fun visit(node: BinaryOperationNode): Type {
            return inferBinaryOperationType(node, context)
        }

        override fun visit(node: IsNode): Type {
            return inferIsExpressionType(node, context)
        }

        override fun visit(node: CallNode): Type {
            return inferCallType(node, context)
        }

        override fun visit(node: PartialCallNode): Type {
            return inferPartialCallType(node, context)
        }

        override fun visit(node: FieldAccessNode): Type {
            return inferFieldAccessType(node, context)
        }

        override fun visit(node: FunctionExpressionNode): Type {
            return typeCheckFunction(node, context, hint = hint)
        }

        override fun visit(node: IfNode): Type {
            return inferIfExpressionType(node, context)
        }

        override fun visit(node: WhenNode): Type {
            return inferWhenExpressionType(node, context)
        }
    })
    context.addExpressionType(expression, type)
    return type
}

private fun inferBinaryOperationType(node: BinaryOperationNode, context: TypeContext): Type {
    val leftType = inferType(node.left, context)
    val rightType = inferType(node.right, context)

    return when (OperationType(node.operator, leftType, rightType)) {
        OperationType(Operator.EQUALS, IntType, IntType) -> BoolType
        OperationType(Operator.ADD, IntType, IntType) -> IntType
        OperationType(Operator.SUBTRACT, IntType, IntType) -> IntType
        OperationType(Operator.MULTIPLY, IntType, IntType) -> IntType

        OperationType(Operator.EQUALS, StringType, StringType) -> BoolType
        OperationType(Operator.ADD, StringType, StringType) -> StringType

        OperationType(Operator.EQUALS, CharType, CharType) -> BoolType
        OperationType(Operator.LESS_THAN, CharType, CharType) -> BoolType
        OperationType(Operator.LESS_THAN_OR_EQUAL, CharType, CharType) -> BoolType
        OperationType(Operator.GREATER_THAN, CharType, CharType) -> BoolType
        OperationType(Operator.GREATER_THAN_OR_EQUAL, CharType, CharType) -> BoolType

        OperationType(Operator.EQUALS, BoolType, BoolType) -> BoolType

        else -> throw InvalidOperationError(
            operator = node.operator,
            operands = listOf(leftType, rightType),
            source = node.source
        )
    }
}

private fun inferIsExpressionType(node: IsNode, context: TypeContext): BoolType {
    // TODO: test expression and type checking

    val expressionType = checkTypePredicateOperand(node.expression, context)
    evalType(node.type, context)

    // TODO: for this to be valid, the type must have a tag value
    // TODO: given generics are erased, when node.type is generic we
    // should make sure no other instantiations of that generic type
    // are possible e.g. if the expression has type Cons[T] | Nil,
    // then checking the type to be Cons[U] is valid iff T <: U

    return BoolType
}

private fun inferFieldAccessType(node: FieldAccessNode, context: TypeContext): Type {
    val receiverType = inferType(node.receiver, context)
    if (receiverType is HasFieldsType) {
        val fieldType = receiverType.fields[node.fieldName]
        if (fieldType == null) {
            throw NoSuchFieldError(
                fieldName = node.fieldName,
                source = node.source
            )
        } else {
            return fieldType
        }
    } else {
        throw NoSuchFieldError(
            fieldName = node.fieldName,
            source = node.source
        )
    }
}

private fun inferIfExpressionType(node: IfNode, context: TypeContext): Type {
    val conditionalBranchTypes = node.conditionalBranches.map { branch ->
        verifyType(branch.condition, context, expected = BoolType)

        val trueContext = context.enterScope()
        val condition = branch.condition

        if (condition is IsNode) {
            val conditionExpression = condition.expression
            if (conditionExpression is VariableReferenceNode) {
                val conditionType = evalType(condition.type, context)
                trueContext.addVariableType(conditionExpression, conditionType)
            }
        }

        typeCheck(branch.body, trueContext)
    }
    val elseBranchType = typeCheck(node.elseBranch, context)
    val branchTypes = conditionalBranchTypes + listOf(elseBranchType)

    return branchTypes.reduce(::union)
}

private fun inferWhenExpressionType(node: WhenNode, context: TypeContext): Type {
    val expressionType = checkTypePredicateOperand(node.expression, context)

    val branchResults = node.branches.map { branch ->
        // TODO: check conditionType is a member of the union
        val conditionType = evalType(branch.type, context)
        val branchContext = context.enterScope()
        val expression = node.expression
        if (expression is VariableReferenceNode) {
            branchContext.addVariableType(expression, conditionType)
        }
        val type = typeCheck(branch.body, branchContext)
        Pair(type, conditionType)
    }

    val caseTypes = branchResults.map { result -> result.second }
    val unhandledMembers = expressionType.members.filter { member ->
        caseTypes.all { caseType -> !isEquivalentType(caseType, member) }
    }
    if (unhandledMembers.isNotEmpty()) {
        throw WhenIsNotExhaustiveError(unhandledMembers, source = node.source)
    }

    val branchTypes = branchResults.map { result -> result.first }
    return branchTypes.reduce(::union)
}

private fun checkTypePredicateOperand(expression: ExpressionNode, context: TypeContext): UnionType {
    val expressionType = inferType(expression, context)
    if (expressionType is UnionType) {
        return expressionType
    } else {
        throw UnexpectedTypeError(
            expected = UnionTypeGroup,
            actual = expressionType,
            source = expression.source
        )
    }
}

internal fun inferReferenceType(reference: ReferenceNode, context: TypeContext): Type {
    val targetNode = context.resolveReference(reference)
    return context.typeOf(targetNode)
}
