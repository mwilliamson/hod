package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.tests.*
import org.zwobble.hod.compiler.typechecker.UnexpectedTypeError
import org.zwobble.hod.compiler.typechecker.typeCheckFunctionDeclaration
import org.zwobble.hod.compiler.typechecker.typeCheckFunctionStatement
import org.zwobble.hod.compiler.types.BoolType
import org.zwobble.hod.compiler.types.MetaType
import org.zwobble.hod.compiler.types.Type
import org.zwobble.hod.compiler.types.UnitType

class TypeCheckExpressionStatementTests {
    @Test
    fun expressionIsTypeChecked() {
        val functionReference = variableReference("f")
        val node = expressionStatement(call(functionReference))
        assertThat(
            { typeCheckFunctionStatement(node, typeContext(referenceTypes = mapOf(functionReference to UnitType))) },
            throws(has(UnexpectedTypeError::actual, equalTo<Type>(UnitType)))
        )
    }

    @Test
    fun nonReturningExpressionStatementHasUnitType() {
        val node = expressionStatementNoReturn(literalBool())
        val type = typeCheckFunctionStatement(node, typeContext())
        assertThat(type, isUnitType)
    }

    @Test
    fun returningExpressionStatementHasTypeOfExpression() {
        val node = expressionStatementReturn(literalBool())
        val type = typeCheckFunctionStatement(node, typeContext())
        assertThat(type, isBoolType)
    }

    @Test
    fun tailrecExpressionStatementHasTypeOfExpression() {
        val functionReference = variableReference("f")
        val expressionStatement = expressionStatementTailRecReturn(
            call(receiver = functionReference)
        )
        val boolReference = staticReference("Bool")
        val boolDeclaration = declaration("Bool")
        val functionDeclaration = function(
            name = "f",
            body = listOf(
                expressionStatement
            ),
            returnType = boolReference
        )

        val context = typeContext(
            references = mapOf(
                functionReference to functionDeclaration,
                boolReference to boolDeclaration
            ),
            types = mapOf(
                boolDeclaration to MetaType(BoolType)
            )
        )
        typeCheckFunctionDeclaration(functionDeclaration, context)
        context.undefer()
    }
}
