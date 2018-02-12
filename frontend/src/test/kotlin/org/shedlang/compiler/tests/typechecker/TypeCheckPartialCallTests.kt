package org.shedlang.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.shedlang.compiler.tests.*
import org.shedlang.compiler.typechecker.inferType
import org.shedlang.compiler.types.*


class TypeCheckPartialCallTests {
    @Test
    fun partialCallWithPositionalArgumentReturnsFunction() {
        val functionReference = variableReference("f")
        val node = partialCall(
            receiver = functionReference,
            positionalArguments = listOf(literalInt())
        )

        val typeContext = typeContext(referenceTypes = mapOf(
            functionReference to functionType(
                positionalArguments = listOf(IntType, BoolType),
                namedArguments = mapOf(),
                effect = IoEffect,
                returns = UnitType
            )
        ))
        val type = inferType(node, typeContext)

        assertThat(type, isFunctionType(
            arguments = isSequence(isBoolType),
            namedArguments = isMap(),
            effect = equalTo(IoEffect),
            returnType = isUnitType
        ))
    }

    @Test
    fun partialCallWithNamedArgumentReturnsFunction() {
        val functionReference = variableReference("f")
        val node = partialCall(
            receiver = functionReference,
            namedArguments = listOf(
                callNamedArgument("arg0", literalInt())
            )
        )

        val typeContext = typeContext(referenceTypes = mapOf(
            functionReference to functionType(
                positionalArguments = listOf(BoolType),
                namedArguments = mapOf("arg0" to IntType, "arg1" to StringType),
                effect = IoEffect,
                returns = UnitType
            )
        ))
        val type = inferType(node, typeContext)

        assertThat(type, isFunctionType(
            arguments = isSequence(isBoolType),
            namedArguments = isMap("arg1" to isStringType),
            effect = equalTo(IoEffect),
            returnType = isUnitType
        ))
    }
}