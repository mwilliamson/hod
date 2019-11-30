package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.ast.Source
import org.zwobble.hod.compiler.tests.*
import org.zwobble.hod.compiler.typechecker.NoSuchFieldError
import org.zwobble.hod.compiler.typechecker.UnexpectedTypeError
import org.zwobble.hod.compiler.typechecker.typeCheckFunctionStatement
import org.zwobble.hod.compiler.types.BoolType
import org.zwobble.hod.compiler.types.IntType
import org.zwobble.hod.compiler.types.Type
import org.zwobble.hod.compiler.types.UnitType

class TypeCheckValTests {
    @Test
    fun expressionIsTypeChecked() {
        val functionReference = variableReference("f")
        val node = valStatement(name = "x", expression = call(functionReference))
        assertThat(
            { typeCheckFunctionStatement(node, typeContext(referenceTypes = mapOf(functionReference to UnitType))) },
            throws(has(UnexpectedTypeError::actual, equalTo<Type>(UnitType)))
        )
    }

    @Test
    fun targetVariableTakesTypeOfExpression() {
        val target = targetVariable(name = "x")
        val node = valStatement(target = target, expression = literalInt())
        val typeContext = typeContext()

        typeCheckFunctionStatement(node, typeContext)

        assertThat(typeContext.typeOf(target), cast(equalTo(IntType)))
    }

    @Test
    fun targetTupleTakesTypeOfExpression() {
        val elementTarget1 = targetVariable("x")
        val elementTarget2 = targetVariable("y")
        val target = targetTuple(elements = listOf(
            elementTarget1,
            elementTarget2
        ))
        val expression = tupleNode(listOf(literalInt(), literalBool()))
        val node = valStatement(target = target, expression = expression)
        val typeContext = typeContext()

        typeCheckFunctionStatement(node, typeContext)

        assertThat(typeContext.typeOf(elementTarget1), isIntType)
        assertThat(typeContext.typeOf(elementTarget2), isBoolType)
    }

    @Test
    fun whenTupleHasMoreElementsThanTargetThenErrorIsThrown() {
        val elementTarget1 = targetVariable("x")
        val target = targetTuple(elements = listOf(elementTarget1))
        val expression = tupleNode(listOf(literalInt(), literalBool()))
        val node = valStatement(target = target, expression = expression)
        val typeContext = typeContext()

        assertThat(
            { typeCheckFunctionStatement(node, typeContext) },
            throwsUnexpectedType(
                actual = isTupleType(elementTypes = isSequence(isAnyType)),
                expected = cast(isTupleType(elementTypes = isSequence(isIntType, isBoolType))),
                source = equalTo(target.source)
            )
        )
    }

    @Test
    fun whenTupleHasFewerElementsThanTargetThenErrorIsThrown() {
        val elementTarget1 = targetVariable("x")
        val elementTarget2 = targetVariable("y")
        val target = targetTuple(elements = listOf(
            elementTarget1,
            elementTarget2
        ))
        val expression = tupleNode(listOf(literalInt()))
        val node = valStatement(target = target, expression = expression)
        val typeContext = typeContext()

        assertThat(
            { typeCheckFunctionStatement(node, typeContext) },
            throwsUnexpectedType(
                actual = isTupleType(elementTypes = isSequence(isAnyType, isAnyType)),
                expected = cast(isTupleType(elementTypes = isSequence(isIntType))),
                source = equalTo(target.source)
            )
        )
    }

    @Test
    fun fieldTargetsTakeTypeOfField() {
        val elementTarget1 = targetVariable("targetX")
        val elementTarget2 = targetVariable("targetY")
        val target = targetFields(fields = listOf(
            fieldName("x") to elementTarget1,
            fieldName("y") to elementTarget2
        ))
        val expressionDeclaration = declaration("e")
        val expression = variableReference("e")
        val node = valStatement(target = target, expression = expression)
        val typeContext = typeContext(
            references = mapOf(
                expression to expressionDeclaration
            ),
            types = mapOf(
                expressionDeclaration to shapeType(
                    fields = listOf(
                        field("x", IntType),
                        field("y", BoolType)
                    )
                )
            )
        )

        typeCheckFunctionStatement(node, typeContext)

        assertThat(typeContext.typeOf(elementTarget1), isIntType)
        assertThat(typeContext.typeOf(elementTarget2), isBoolType)
    }

    @Test
    fun whenFieldIsMissingFromExpressionTypeThenErrorIsThrown() {
        val source = object: Source {
            override fun describe(): String {
                return "<source>"
            }
        }
        val elementTarget1 = targetVariable("targetX")
        val target = targetFields(fields = listOf(
            fieldName("x", source = source) to elementTarget1
        ))
        val expressionDeclaration = declaration("e")
        val expression = variableReference("e")
        val node = valStatement(target = target, expression = expression)
        val typeContext = typeContext(
            references = mapOf(
                expression to expressionDeclaration
            ),
            types = mapOf(
                expressionDeclaration to shapeType(
                    fields = listOf()
                )
            )
        )
        assertThat(
            { typeCheckFunctionStatement(node, typeContext) },
            throws(allOf(
                has(NoSuchFieldError::fieldName, isIdentifier("x")),
                has(NoSuchFieldError::source, cast(equalTo(source)))
            ))
        )
    }
}
