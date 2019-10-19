package org.shedlang.compiler.stackinterpreter.tests

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.junit.jupiter.api.Test
import org.shedlang.compiler.*
import org.shedlang.compiler.ast.*
import org.shedlang.compiler.backends.CodeInspector
import org.shedlang.compiler.backends.FieldValue
import org.shedlang.compiler.backends.SimpleCodeInspector
import org.shedlang.compiler.stackinterpreter.*
import org.shedlang.compiler.tests.*
import org.shedlang.compiler.typechecker.ResolvedReferencesMap
import org.shedlang.compiler.types.*

class InterpreterTests {
    @Test
    fun booleanLiteralIsEvaluatedToBoolean() {
        val node = literalBool(true)

        val value = evaluateExpression(node)

        assertThat(value, isBool(true))
    }

    @Test
    fun integerLiteralIsEvaluatedToInteger() {
        val node = literalInt(42)

        val value = evaluateExpression(node)

        assertThat(value, isInt(42))
    }

    @Test
    fun stringLiteralIsEvaluatedToString() {
        val node = literalString("hello")

        val value = evaluateExpression(node)

        assertThat(value, isString("hello"))
    }

    @Test
    fun whenOperandsAreEqualThenBooleanEqualityEvaluatesToTrue() {
        val left = literalBool(true)
        val node = binaryOperation(BinaryOperator.EQUALS, left, literalBool(true))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to BoolType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isBool(true))
    }

    @Test
    fun whenOperandsAreNotEqualThenBooleanEqualityEvaluatesToFalse() {
        val left = literalBool(true)
        val node = binaryOperation(BinaryOperator.EQUALS, left, literalBool(false))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to BoolType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isBool(false))
    }

    @Test
    fun integerAdditionAddsOperandsTogether() {
        val left = literalInt(1)
        val node = binaryOperation(BinaryOperator.ADD, left, literalInt(2))
        val types = createTypes(expressionTypes = mapOf(left.nodeId to IntType))

        val value = evaluateExpression(node, types = types)

        assertThat(value, isInt(3))
    }

    @Test
    fun subtractSubtractsOperandsFromEachOther() {
        val node = binaryOperation(BinaryOperator.SUBTRACT, literalInt(1), literalInt(2))

        val value = evaluateExpression(node)

        assertThat(value, isInt(-1))
    }

    @Test
    fun multiplyMultipliesOperands() {
        val node = binaryOperation(BinaryOperator.MULTIPLY, literalInt(3), literalInt(4))

        val value = evaluateExpression(node)

        assertThat(value, isInt(12))
    }

    @Test
    fun whenOperandsAreEqualThenIntegerEqualityEvaluatesToTrue() {
        val left = literalInt(1)
        val node = binaryOperation(BinaryOperator.EQUALS, left, literalInt(1))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to IntType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isBool(true))
    }

    @Test
    fun whenOperandsAreNotEqualThenIntegerEqualityEvaluatesToFalse() {
        val left = literalInt(1)
        val node = binaryOperation(BinaryOperator.EQUALS, left, literalInt(2))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to IntType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isBool(false))
    }

    @Test
    fun whenOperandsAreEqualThenIntegerInequalityEvaluatesToFalse() {
        val left = literalInt(1)
        val node = binaryOperation(BinaryOperator.NOT_EQUAL, left, literalInt(1))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to IntType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isBool(false))
    }

    @Test
    fun whenOperandsAreNotEqualThenIntegerInequalityEvaluatesToTrue() {
        val left = literalInt(1)
        val node = binaryOperation(BinaryOperator.NOT_EQUAL, left, literalInt(2))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to IntType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isBool(true))
    }

    @Test
    fun stringAdditionConcatenatesStrings() {
        val left = literalString("hello ")
        val node = binaryOperation(BinaryOperator.ADD, left, literalString("world"))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to StringType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isString("hello world"))
    }

    @Test
    fun whenOperandsAreEqualThenStringEqualityReturnsTrue() {
        val left = literalString("hello")
        val node = binaryOperation(BinaryOperator.EQUALS, left, literalString("hello"))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to StringType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isBool(true))
    }

    @Test
    fun whenOperandsAreEqualThenStringEqualityReturnsFalse() {
        val left = literalString("hello")
        val node = binaryOperation(BinaryOperator.EQUALS, left, literalString("world"))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to StringType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isBool(false))
    }

    @Test
    fun whenOperandsAreEqualThenStringInequalityReturnsFalse() {
        val left = literalString("hello")
        val node = binaryOperation(BinaryOperator.NOT_EQUAL, left, literalString("hello"))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to StringType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isBool(false))
    }

    @Test
    fun whenOperandsAreEqualThenStringInequalityReturnsTrue() {
        val left = literalString("hello")
        val node = binaryOperation(BinaryOperator.NOT_EQUAL, left, literalString("world"))
        val types = createTypes(
            expressionTypes = mapOf(left.nodeId to StringType)
        )

        val value = evaluateExpression(node, types = types)

        assertThat(value, isBool(true))
    }

    @Test
    fun whenDiscriminatorMatchesThenIsOperationIsTrue() {
        val shapeDeclaration = shape("X")
        val shapeReference = variableReference("X")

        val receiverTarget = targetVariable("receiver")
        val receiverDeclaration = valStatement(
            target = receiverTarget,
            expression = call(shapeReference)
        )
        val receiverReference = variableReference("receiver")
        val node = isOperation(receiverReference, shapeReference)

        val symbol = Symbol(listOf(Identifier("A")), "B")
        val inspector = SimpleCodeInspector(
            discriminatorsForIsExpressions = mapOf(
                node to discriminator(fieldName = "tag", symbolType = SymbolType(symbol))
            ),
            shapeFields = mapOf(
                shapeDeclaration to listOf(
                    fieldInspector(name = "tag", value = FieldValue.Symbol(symbol))
                )
            )
        )
        val references = ResolvedReferencesMap(mapOf(
            shapeReference.nodeId to shapeDeclaration,
            receiverReference.nodeId to receiverTarget
        ))

        val loader = loader(inspector = inspector, references = references)
        val instructions = loader.loadModuleStatement(shapeDeclaration)
            .addAll(loader.loadFunctionStatement(receiverDeclaration))
            .addAll(loader.loadExpression(node))
        val value = executeInstructions(instructions)

        assertThat(value, isBool(true))
    }

    @Test
    fun whenDiscriminatorMatchesThenIsOperationIsFalse() {
        val shapeDeclaration = shape("X")
        val shapeReference = variableReference("X")

        val receiverTarget = targetVariable("receiver")
        val receiverDeclaration = valStatement(
            target = receiverTarget,
            expression = call(shapeReference)
        )
        val receiverReference = variableReference("receiver")
        val node = isOperation(receiverReference, shapeReference)

        val shapeSymbol = Symbol(listOf(Identifier("A")), "B")
        val isSymbol = Symbol(listOf(Identifier("A")), "C")
        val inspector = SimpleCodeInspector(
            discriminatorsForIsExpressions = mapOf(
                node to discriminator(fieldName = "tag", symbolType = SymbolType(isSymbol))
            ),
            shapeFields = mapOf(
                shapeDeclaration to listOf(
                    fieldInspector(name = "tag", value = FieldValue.Symbol(shapeSymbol))
                )
            )
        )
        val references = ResolvedReferencesMap(mapOf(
            shapeReference.nodeId to shapeDeclaration,
            receiverReference.nodeId to receiverTarget
        ))

        val loader = loader(inspector = inspector, references = references)
        val instructions = loader.loadModuleStatement(shapeDeclaration)
            .addAll(loader.loadFunctionStatement(receiverDeclaration))
            .addAll(loader.loadExpression(node))
        val value = executeInstructions(instructions)

        assertThat(value, isBool(false))
    }

    @Test
    fun canAccessFieldsOnShapeValue() {
        val shapeDeclaration = shape("Pair")
        val shapeReference = variableReference("Pair")

        val receiverTarget = targetVariable("receiver")
        val receiverDeclaration = valStatement(
            target = receiverTarget,
            expression = call(
                shapeReference,
                namedArguments = listOf(
                    callNamedArgument("first", literalInt(1)),
                    callNamedArgument("second", literalInt(2))
                )
            )
        )
        val receiverReference = variableReference("receiver")
        val fieldAccess = fieldAccess(receiverReference, "second")

        val inspector = SimpleCodeInspector(
            shapeFields = mapOf(
                shapeDeclaration to listOf(
                    fieldInspector(name = "first"),
                    fieldInspector(name = "second")
                )
            )
        )
        val references = ResolvedReferencesMap(mapOf(
            shapeReference.nodeId to shapeDeclaration,
            receiverReference.nodeId to receiverTarget
        ))

        val loader = loader(inspector = inspector, references = references)
        val instructions = loader.loadModuleStatement(shapeDeclaration)
            .addAll(loader.loadFunctionStatement(receiverDeclaration))
            .addAll(loader.loadExpression(fieldAccess))
        val value = executeInstructions(instructions)

        assertThat(value, isInt(2))
    }

    @Test
    fun constantSymbolFieldsOnShapesHaveValueSet() {
        val shapeDeclaration = shape("Pair")
        val shapeReference = variableReference("Pair")

        val receiverTarget = targetVariable("receiver")
        val receiverDeclaration = valStatement(
            target = receiverTarget,
            expression = call(
                shapeReference,
                namedArguments = listOf()
            )
        )
        val receiverReference = variableReference("receiver")
        val fieldAccess = fieldAccess(receiverReference, "constantField")

        val symbol = Symbol(listOf(Identifier("A")), "B")
        val inspector = SimpleCodeInspector(
            shapeFields = mapOf(
                shapeDeclaration to listOf(
                    fieldInspector(name = "constantField", value = FieldValue.Symbol(symbol))
                )
            )
        )
        val references = ResolvedReferencesMap(mapOf(
            shapeReference.nodeId to shapeDeclaration,
            receiverReference.nodeId to receiverTarget
        ))

        val loader = loader(inspector = inspector, references = references)
        val instructions = loader.loadModuleStatement(shapeDeclaration)
            .addAll(loader.loadFunctionStatement(receiverDeclaration))
            .addAll(loader.loadExpression(fieldAccess))
        val value = executeInstructions(instructions)

        assertThat(value, isSymbol(symbol))
    }

    @Test
    fun canDestructureFieldsInVal() {
        val shapeDeclaration = shape("Pair")
        val shapeReference = variableReference("Pair")

        val firstTarget = targetVariable("target1")
        val secondTarget = targetVariable("target2")
        val receiverDeclaration = valStatement(
            target = targetFields(listOf(
                fieldName("first") to firstTarget,
                fieldName("second") to secondTarget
            )),
            expression = call(
                shapeReference,
                namedArguments = listOf(
                    callNamedArgument("first", literalInt(1)),
                    callNamedArgument("second", literalInt(2))
                )
            )
        )

        val firstReference = variableReference("first")
        val secondReference = variableReference("second")
        val addition = binaryOperation(BinaryOperator.ADD, firstReference, secondReference)

        val inspector = SimpleCodeInspector(
            shapeFields = mapOf(
                shapeDeclaration to listOf(
                    fieldInspector(name = "first"),
                    fieldInspector(name = "second")
                )
            )
        )
        val references = ResolvedReferencesMap(mapOf(
            shapeReference.nodeId to shapeDeclaration,
            firstReference.nodeId to firstTarget,
            secondReference.nodeId to secondTarget
        ))
        val types = createTypes(expressionTypes = mapOf(firstReference.nodeId to IntType))

        val loader = loader(inspector = inspector, references = references, types = types)
        val instructions = loader.loadModuleStatement(shapeDeclaration)
            .addAll(loader.loadFunctionStatement(receiverDeclaration))
            .addAll(loader.loadExpression(addition))
        val value = executeInstructions(instructions)

        assertThat(value, isInt(3))
    }

    @Test
    fun canCreateAndDestructureTuple() {
        val firstTarget = targetVariable("target1")
        val secondTarget = targetVariable("target2")
        val valStatement = valStatement(
            target = targetTuple(listOf(firstTarget, secondTarget)),
            expression = tupleNode(listOf(literalInt(1), literalInt(2)))
        )
        val firstReference = variableReference("first")
        val secondReference = variableReference("second")
        val addition = binaryOperation(BinaryOperator.ADD, firstReference, secondReference)
        val references = ResolvedReferencesMap(mapOf(
            firstReference.nodeId to firstTarget,
            secondReference.nodeId to secondTarget
        ))
        val types = createTypes(expressionTypes = mapOf(firstReference.nodeId to IntType))

        val loader = loader(references = references, types = types)
        val instructions = loader.loadFunctionStatement(valStatement)
            .addAll(loader.loadExpression(addition))
        val value = executeInstructions(instructions)

        assertThat(value, isInt(3))
    }

    @Test
    fun whenConditionOfIfIsTrueThenFinalValueIsResultOfTrueBranch() {
        val node = ifExpression(
            literalBool(true),
            listOf(expressionStatementReturn(literalInt(1))),
            listOf(expressionStatementReturn(literalInt(2)))
        )

        val value = evaluateExpression(node)

        assertThat(value, isInt(1))
    }

    @Test
    fun firstTrueBranchIsEvaluated() {
        val node = ifExpression(
            listOf(
                conditionalBranch(
                    literalBool(false),
                    listOf(expressionStatementReturn(literalInt(1)))
                ),
                conditionalBranch(
                    literalBool(true),
                    listOf(expressionStatementReturn(literalInt(2)))
                ),
                conditionalBranch(
                    literalBool(true),
                    listOf(expressionStatementReturn(literalInt(3)))
                ),
                conditionalBranch(
                    literalBool(false),
                    listOf(expressionStatementReturn(literalInt(4)))
                )
            ),
            listOf(expressionStatementReturn(literalInt(5)))
        )

        val value = evaluateExpression(node)

        assertThat(value, isInt(2))
    }

    @Test
    fun whenConditionOfIfIsFalseThenFinalValueIsResultOfFalseBranch() {
        val node = ifExpression(
            literalBool(false),
            listOf(expressionStatementReturn(literalInt(1))),
            listOf(expressionStatementReturn(literalInt(2)))
        )

        val value = evaluateExpression(node)

        assertThat(value, isInt(2))
    }

    @Test
    fun variableIntroducedByValCanBeRead() {
        val target = targetVariable("x")
        val reference = variableReference("x")
        val block = block(
            listOf(
                valStatement(target = target, expression = literalInt(42)),
                expressionStatementReturn(reference)
            )
        )

        val references = ResolvedReferencesMap(mapOf(
            reference.nodeId to target
        ))

        val value = evaluateBlock(block, references = references)

        assertThat(value, isInt(42))
    }

    @Test
    fun canCallFunctionDefinedInModuleWithZeroArguments() {
        val function = function(
            name = "main",
            body = listOf(
                expressionStatementReturn(
                    literalInt(42)
                )
            )
        )
        val moduleName = listOf(Identifier("Example"))
        val functionReference = export("main")
        val references = ResolvedReferencesMap(mapOf(
            functionReference.nodeId to function
        ))
        val module = stubbedModule(
            name = moduleName,
            node = module(
                exports = listOf(functionReference),
                body = listOf(function)
            ),
            references = references
        )

        val image = loadModuleSet(ModuleSet(listOf(module)))
        val value = executeInstructions(
            persistentListOf(
                InitModule(moduleName),
                LoadModule(moduleName),
                FieldAccess(Identifier("main")),
                Call(positionalArgumentCount = 0, namedArgumentNames = listOf())
            ),
            image = image
        )

        assertThat(value, isInt(42))
    }

    @Test
    fun canCallFunctionDefinedInModuleWithPositionalArguments() {
        val firstParameter = parameter("first")
        val secondParameter = parameter("second")
        val firstReference = variableReference("first")
        val secondReference = variableReference("second")
        val function = function(
            name = "subtract",
            parameters = listOf(
                firstParameter,
                secondParameter
            ),
            body = listOf(
                expressionStatementReturn(
                    binaryOperation(
                        BinaryOperator.SUBTRACT,
                        firstReference,
                        secondReference
                    )
                )
            )
        )
        val moduleName = listOf(Identifier("Example"))
        val functionReference = export("main")
        val references = ResolvedReferencesMap(mapOf(
            functionReference.nodeId to function,
            firstReference.nodeId to firstParameter,
            secondReference.nodeId to secondParameter
        ))
        val module = stubbedModule(
            name = moduleName,
            node = module(
                exports = listOf(functionReference),
                body = listOf(function)
            ),
            references = references
        )

        val image = loadModuleSet(ModuleSet(listOf(module)))
        val value = executeInstructions(
            persistentListOf(
                InitModule(moduleName),
                LoadModule(moduleName),
                FieldAccess(Identifier("main")),
                PushValue(InterpreterInt(1.toBigInteger())),
                PushValue(InterpreterInt(2.toBigInteger())),
                Call(positionalArgumentCount = 2, namedArgumentNames = listOf())
            ),
            image = image
        )

        assertThat(value, isInt(-1))
    }

    @Test
    fun functionCanReferenceVariablesFromOuterScope() {
        val valueTarget = targetVariable("value")
        val valueDeclaration = valStatement(valueTarget, literalInt(42))
        val valueReference = variableReference("value")
        val function = function(
            name = "main",
            body = listOf(
                expressionStatementReturn(valueReference)
            )
        )
        val moduleName = listOf(Identifier("Example"))
        val functionReference = export("main")
        val references = ResolvedReferencesMap(mapOf(
            functionReference.nodeId to function,
            valueReference.nodeId to valueTarget
        ))
        val module = stubbedModule(
            name = moduleName,
            node = module(
                exports = listOf(functionReference),
                body = listOf(valueDeclaration, function)
            ),
            references = references
        )

        val image = loadModuleSet(ModuleSet(listOf(module)))
        val value = executeInstructions(
            persistentListOf(
                InitModule(moduleName),
                LoadModule(moduleName),
                FieldAccess(Identifier("main")),
                Call(positionalArgumentCount = 0, namedArgumentNames = listOf())
            ),
            image = image
        )

        assertThat(value, isInt(42))
    }

    @Test
    fun functionCanReferenceVariablesFromLaterInOuterScope() {
        val valueTarget = targetVariable("value")
        val valueDeclaration = valStatement(valueTarget, literalInt(42))
        val valueReference = variableReference("value")
        val function = function(
            name = "main",
            body = listOf(
                expressionStatementReturn(valueReference)
            )
        )
        val moduleName = listOf(Identifier("Example"))
        val functionReference = export("main")
        val references = ResolvedReferencesMap(mapOf(
            functionReference.nodeId to function,
            valueReference.nodeId to valueTarget
        ))
        val module = stubbedModule(
            name = moduleName,
            node = module(
                exports = listOf(functionReference),
                body = listOf(function, valueDeclaration)
            ),
            references = references
        )

        val image = loadModuleSet(ModuleSet(listOf(module)))
        val value = executeInstructions(
            persistentListOf(
                InitModule(moduleName),
                LoadModule(moduleName),
                FieldAccess(Identifier("main")),
                Call(positionalArgumentCount = 0, namedArgumentNames = listOf())
            ),
            image = image
        )

        assertThat(value, isInt(42))
    }

    @Test
    fun functionDeclarationCreatesFunctionValueThatCanBeCalledWithZeroArguments() {
        val function = function(
            name = "main",
            body = listOf(
                expressionStatementReturn(
                    literalInt(42)
                )
            )
        )
        val functionReference = variableReference("main")
        val call = call(
            receiver = functionReference
        )
        val references = ResolvedReferencesMap(mapOf(
            functionReference.nodeId to function
        ))

        val loader = loader(references = references)
        val instructions = loader.loadModuleStatement(function).addAll(loader.loadExpression(call))
        val value = executeInstructions(instructions)

        assertThat(value, isInt(42))
    }

    @Test
    fun emptyFunctionReturnsUnit() {
        val function = function(
            name = "main",
            body = listOf()
        )
        val functionReference = variableReference("main")
        val call = call(receiver = functionReference)
        val references = ResolvedReferencesMap(mapOf(
            functionReference.nodeId to function
        ))

        val loader = loader(references = references)
        val instructions = loader.loadModuleStatement(function).addAll(loader.loadExpression(call))
        val value = executeInstructions(instructions)

        assertThat(value, isUnit)
    }

    @Test
    fun functionDeclarationCreatesFunctionValueThatCanBeCalledWithPositionalArguments() {
        val firstParameter = parameter("first")
        val secondParameter = parameter("second")
        val firstReference = variableReference("first")
        val secondReference = variableReference("second")
        val function = function(
            name = "subtract",
            parameters = listOf(
                firstParameter,
                secondParameter
            ),
            body = listOf(
                expressionStatementReturn(
                    binaryOperation(
                        BinaryOperator.SUBTRACT,
                        firstReference,
                        secondReference
                    )
                )
            )
        )
        val functionReference = variableReference("main")
        val call = call(
            receiver = functionReference,
            positionalArguments = listOf(
                literalInt(1),
                literalInt(2)
            )
        )
        val references = ResolvedReferencesMap(mapOf(
            functionReference.nodeId to function,
            firstReference.nodeId to firstParameter,
            secondReference.nodeId to secondParameter
        ))

        val loader = loader(references = references)
        val instructions = loader.loadModuleStatement(function).addAll(loader.loadExpression(call))
        val value = executeInstructions(instructions)

        assertThat(value, isInt(-1))
    }

    @Test
    fun intToStringBuiltinConvertsIntToString() {
        val functionReference = variableReference("intToString")
        val call = call(
            receiver = functionReference,
            positionalArguments = listOf(
                literalInt(42)
            )
        )
        val references = ResolvedReferencesMap(mapOf(
            functionReference.nodeId to Builtins.intToString
        ))

        val loader = loader(references = references)
        val instructions = loader.loadExpression(call)
        val value = executeInstructions(instructions, variables = builtinVariables)

        assertThat(value, isString("42"))
    }

    @Test
    fun printBuiltinWritesToStdout() {
        val functionReference = variableReference("print")
        val call = call(
            receiver = functionReference,
            positionalArguments = listOf(
                literalString("hello")
            )
        )
        val references = ResolvedReferencesMap(mapOf(
            functionReference.nodeId to Builtins.print
        ))

        val loader = loader(references = references)
        val instructions = loader.loadExpression(call)
        val world = InMemoryWorld()
        val finalState = executeInstructions(
            instructions,
            image = Image.EMPTY,
            defaultVariables = builtinVariables,
            world = world
        )

        assertThat(world.stdout, equalTo("hello"))
    }

    private fun evaluateBlock(block: Block, references: ResolvedReferences): InterpreterValue {
        val instructions = loader(references = references).loadBlock(block)
        return executeInstructions(instructions)
    }

    private fun evaluateExpression(node: ExpressionNode, types: Types = EMPTY_TYPES): InterpreterValue {
        val instructions = loader(types = types).loadExpression(node)
        return executeInstructions(instructions)
    }

    private fun executeInstructions(
        instructions: PersistentList<Instruction>,
        image: Image = Image.EMPTY,
        variables: Map<Int, InterpreterValue> = mapOf()
    ): InterpreterValue {
        val finalState = org.shedlang.compiler.stackinterpreter.executeInstructions(
            instructions,
            image = image,
            defaultVariables = variables,
            world = NullWorld
        )
        return finalState.popTemporary().second
    }

    private fun loader(
        inspector: CodeInspector = SimpleCodeInspector(),
        references: ResolvedReferences = ResolvedReferencesMap.EMPTY,
        types: Types = EMPTY_TYPES
    ): Loader {
        return Loader(inspector = inspector, references = references, types = types)
    }

    private fun isBool(value: Boolean): Matcher<InterpreterValue> {
        return cast(has(InterpreterBool::value, equalTo(value)))
    }

    private fun isInt(value: Int): Matcher<InterpreterValue> {
        return cast(has(InterpreterInt::value, equalTo(value.toBigInteger())))
    }

    private fun isString(value: String): Matcher<InterpreterValue> {
        return cast(has(InterpreterString::value, equalTo(value)))
    }

    private fun isSymbol(value: Symbol): Matcher<InterpreterValue> {
        return cast(has(InterpreterSymbol::value, equalTo(value)))
    }

    private val isUnit = cast(equalTo(InterpreterUnit))

    private fun stubbedModule(
        name: List<Identifier>,
        node: ModuleNode,
        references: ResolvedReferences = ResolvedReferencesMap.EMPTY
    ): Module.Shed {
        return Module.Shed(
            name = name,
            type = ModuleType(mapOf()),
            types = EMPTY_TYPES,
            references = references,
            node = node
        )
    }

    private fun createTypes(expressionTypes: Map<Int, Type>): Types {
        return TypesMap(
            discriminators = mapOf(),
            expressionTypes = expressionTypes,
            variableTypes = mapOf()
        )
    }
}
