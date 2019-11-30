package org.zwobble.hod.compiler.backends.javascript.tests

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.zwobble.hod.compiler.EMPTY_TYPES
import org.zwobble.hod.compiler.Module
import org.zwobble.hod.compiler.ast.*
import org.zwobble.hod.compiler.backends.FieldInspector
import org.zwobble.hod.compiler.backends.FieldValue
import org.zwobble.hod.compiler.backends.SimpleCodeInspector
import org.zwobble.hod.compiler.backends.javascript.CodeGenerationContext
import org.zwobble.hod.compiler.backends.javascript.ast.*
import org.zwobble.hod.compiler.backends.javascript.generateCode
import org.zwobble.hod.compiler.tests.*
import org.zwobble.hod.compiler.typechecker.ResolvedReferencesMap
import org.zwobble.hod.compiler.types.*
import java.math.BigInteger

class CodeGeneratorTests {
    @Test
    fun emptyModuleGeneratesEmptyModule() {
        val hod = stubbedModule(
            node = module(body = listOf())
        )
        val node = generateCode(hod)

        assertThat(node, isJavascriptModule(equalTo(listOf())))
    }

    @Test
    fun moduleImportsGenerateJavascriptImports() {
        val hod = stubbedModule(node = module(imports = listOf(import(
            name = Identifier("a"),
            path = ImportPath.relative(listOf("x"))
        ))))

        val node = generateCode(hod)

        assertThat(node, isJavascriptModule(
            body = isSequence(
                isJavascriptConst(
                    target = isJavascriptVariableReference("a"),
                    expression = isJavascriptFunctionCall(
                        isJavascriptVariableReference("require"),
                        isSequence(isJavascriptStringLiteral("./x"))
                    )
                )
            )
        ))
    }

    @Test
    fun moduleIncludesBodyAndExports() {
        val hod = stubbedModule(node = module(
            exports = listOf(export("f")),
            body = listOf(function(name = "f"))
        ))

        val node = generateCode(hod)

        assertThat(node, isJavascriptModule(
            body = isSequence(
                isJavascriptFunction(name = equalTo("f")),
                isJavascriptAssignmentStatement(
                    isJavascriptPropertyAccess(
                        isJavascriptVariableReference("exports"),
                        equalTo("f")
                    ),
                    isJavascriptVariableReference("f")
                )
            )
        ))
    }

    private fun stubbedModule(node: ModuleNode): Module.Hod {
        return Module.Hod(
            name = listOf(Identifier("Module")),
            type = ModuleType(mapOf()),
            types = EMPTY_TYPES,
            references = ResolvedReferencesMap(mapOf()),
            node = node
        )
    }

    @Test
    fun typeAliasGeneratesNothing() {
        val hod = typeAliasDeclaration("Size", staticReference("Int"))

        val javascriptNodes = generateCodeForModuleStatement(hod)

        assertThat(javascriptNodes, isSequence())
    }

    @Test
    fun shapeGeneratesType() {
        val hod = shape(
            name = "X",
            fields = listOf(
                shapeField("a", staticReference("Int"), value = null),
                shapeField("b", staticReference("Int"), value = literalInt(0))
            )
        )

        val context = context(
            shapeFields = mapOf(
                hod to listOf(
                    fieldInspector(name = "a", value = null),
                    fieldInspector(name = "b", value = FieldValue.Expression(literalInt(0)))
                )
            )
        )
        val node = generateCode(hod, context).single()

        assertThat(node, isJavascriptConst(
            target = isJavascriptVariableReference("X"),
            expression = isJavascriptFunctionCall(
                isJavascriptVariableReference("\$hod.declareShape"),
                isSequence(
                    isJavascriptStringLiteral("X"),
                    isJavascriptArray(anything)
                )
            )
        ))
    }

    @Test
    fun unionGeneratesStubForUnionAndShapesForEachMember() {
        val member1Node = unionMember("Member1")
        val member2Node = unionMember("Member2")
        val hod = union("X", listOf(member1Node, member2Node))

        val context = context(
            shapeFields = mapOf(
                member1Node to listOf(),
                member2Node to listOf()
            )
        )
        val nodes = generateCode(hod, context)

        assertThat(nodes, isSequence(
            isJavascriptConst(
                target = isJavascriptVariableReference("X"),
                expression = isJavascriptNull()
            ),
            isJavascriptConst(
                target = isJavascriptVariableReference("Member1"),
                expression = isJavascriptFunctionCall(
                    isJavascriptVariableReference("\$hod.declareShape"),
                    isSequence(
                        isJavascriptStringLiteral("Member1"),
                        anything
                    )
                )
            ),
            isJavascriptConst(
                target = isJavascriptVariableReference("Member2"),
                expression = isJavascriptFunctionCall(
                    isJavascriptVariableReference("\$hod.declareShape"),
                    isSequence(
                        isJavascriptStringLiteral("Member2"),
                        anything
                    )
                )
            )
        ))
    }

    @Test
    fun varargsCallsVarargsFunction() {
        val consReference = variableReference("cons")
        val consDeclaration = declaration("cons")
        val nilReference = variableReference("nil")
        val nilDeclaration = declaration("nil")
        val hod = varargsDeclaration(
            name = "list",
            cons = consReference,
            nil = nilReference
        )

        val context = context()
        val nodes = generateCode(hod, context)

        assertThat(nodes, isSequence(
            isJavascriptConst(
                isJavascriptVariableReference("list"),
                isJavascriptFunctionCall(
                    function = isJavascriptVariableReference("\$hod.varargs"),
                    arguments = isSequence(isJavascriptVariableReference("cons"), isJavascriptVariableReference("nil"))
                )
            )
        ))
    }

    @Test
    fun functionDeclarationAsModuleStatementGeneratesFunctionDeclaration() {
        assertFunctionDeclarationGeneratesFunctionDeclaration { function ->
            generateCodeForModuleStatement(function).single()
        }
    }

    @Test
    fun functionDeclarationAsFunctionStatementGeneratesFunctionDeclaration() {
        assertFunctionDeclarationGeneratesFunctionDeclaration { function ->
            generateCodeForFunctionStatement(function)
        }
    }

    private fun assertFunctionDeclarationGeneratesFunctionDeclaration(
        generateCode: (node: FunctionDeclarationNode) -> JavascriptStatementNode
    ) {
        val hod = function(
            name = "f",
            parameters = listOf(parameter("x"), parameter("y")),
            namedParameters = listOf(parameter("z")),
            body = listOf(expressionStatement(literalInt(42)))
        )

        val node = generateCode(hod)

        assertThat(node, isJavascriptFunction(
            name = equalTo("f"),
            parameters = isSequence(equalTo("x"), equalTo("y"), equalTo("\$named")),
            body = isSequence(
                isJavascriptConst(
                    target = isJavascriptVariableReference("z"),
                    expression = isJavascriptPropertyAccess(
                        receiver = isJavascriptVariableReference("\$named"),
                        propertyName = equalTo("z")
                    )
                ),
                isJavascriptExpressionStatement(isJavascriptIntegerLiteral(42))
            )
        ))
    }

    @Test
    fun functionExpressionGeneratesFunctionExpression() {
        val hod = functionExpression(
            parameters = listOf(parameter("x"), parameter("y")),
            body = listOf(expressionStatement(literalInt(42)))
        )

        val node = generateCode(hod)

        assertThat(node, isJavascriptFunctionExpression(
            parameters = isSequence(equalTo("x"), equalTo("y")),
            body = isSequence(isJavascriptExpressionStatement(isJavascriptIntegerLiteral(42)))
        ))
    }

    @Test
    fun nonReturningExpressionStatementGeneratesExpressionStatement() {
        val hod = expressionStatementNoReturn(literalInt(42))

        val node = generateCodeForFunctionStatement(hod)

        assertThat(node, cast(has(
            JavascriptExpressionStatementNode::expression,
            isJavascriptIntegerLiteral(42)
        )))
    }

    @Test
    fun returningExpressionStatementGeneratesReturnStatement() {
        val hod = expressionStatementReturn(literalInt(42))

        val node = generateCodeForFunctionStatement(hod)

        assertThat(node, cast(has(
            JavascriptReturnNode::expression,
            isJavascriptIntegerLiteral(42)
        )))
    }

    @Test
    fun ifExpressionGeneratesImmediatelyEvaluatedIfStatement() {
        val hod = ifExpression(
            literalInt(42),
            listOf(expressionStatement(literalInt(0))),
            listOf(expressionStatement(literalInt(1)))
        )

        val node = generateCode(hod)

        assertThat(node, isJavascriptImmediatelyInvokedFunction(
            body = isSequence(
                isJavascriptIfStatement(
                    conditionalBranches = isSequence(
                        isJavascriptConditionalBranch(
                            condition = isJavascriptIntegerLiteral(42),
                            body = isSequence(
                                isJavascriptExpressionStatement(isJavascriptIntegerLiteral(0))
                            )
                        )
                    ),
                    elseBranch = isSequence(
                        isJavascriptExpressionStatement(isJavascriptIntegerLiteral(1))
                    )
                )
            )
        ))
    }

    @Test
    fun whenExpressionGeneratesImmediatelyEvaluatedIfStatement() {
        val whenBranch = whenBranch(
            staticReference("T"),
            listOf(
                expressionStatementReturn(literalInt(42))
            )
        )
        val hod = whenExpression(
            variableReference("x"),
            branches = listOf(
                whenBranch
            ),
            elseBranch = listOf(
                expressionStatementReturn(literalInt(47))
            )
        )

        val context = context(
            discriminatorsForWhenBranches = mapOf(
                Pair(hod, whenBranch) to discriminator(symbolType(listOf("M"), "`A"), "tag")
            )
        )
        val node = generateCode(hod, context)

        assertThat(node, isJavascriptImmediatelyInvokedFunction(
            body = isSequence(
                isJavascriptConst(
                    target = isJavascriptVariableReference("\$hod_tmp"),
                    expression = isJavascriptVariableReference("x")
                ),
                isJavascriptIfStatement(
                    conditionalBranches = isSequence(
                        isJavascriptConditionalBranch(
                            condition = isJavascriptTypeCondition(
                                expression = isJavascriptVariableReference("\$hod_tmp"),
                                discriminator = discriminator(symbolType(listOf("M"), "`A"), "tag")
                            ),
                            body = isSequence(
                                isJavascriptReturn(isJavascriptIntegerLiteral(42))
                            )
                        )
                    ),
                    elseBranch = isSequence(
                        isJavascriptReturn(isJavascriptIntegerLiteral(47))
                    )
                )
            )
        ))
    }

    @Test
    fun valWithTargetVariableGeneratesConst() {
        val hod = valStatement(name = "x", expression = literalBool(true))

        val node = generateCodeForFunctionStatement(hod)

        assertThat(node, isJavascriptConst(
            target = isJavascriptVariableReference("x"),
            expression = isJavascriptBooleanLiteral(true)
        ))
    }

    @Test
    fun valWithTargetTupleGeneratesConstTargetingArray() {
        val hod = valStatement(
            target = targetTuple(elements = listOf(targetVariable("x"), targetVariable("y"))),
            expression = literalBool(true)
        )

        val node = generateCodeForFunctionStatement(hod)

        assertThat(node, isJavascriptConst(
            target = isJavascriptArrayDestructuring(
                elements = isSequence(
                    isJavascriptVariableReference("x"),
                    isJavascriptVariableReference("y")
                )
            ),
            expression = isJavascriptBooleanLiteral(true)
        ))
    }

    @Test
    fun valWithTargetFieldsGeneratesDestructuringObjectAssignment() {
        val hod = valStatement(
            target = targetFields(fields = listOf(
                fieldName("x") to targetVariable("targetX"),
                fieldName("y") to targetVariable("targetY")
            )),
            expression = literalBool(true)
        )

        val node = generateCodeForFunctionStatement(hod)

        assertThat(node, isJavascriptConst(
            target = isJavascriptObjectDestructuring(
                properties = isSequence(
                    isPair(equalTo("x"), isJavascriptVariableReference("targetX")),
                    isPair(equalTo("y"), isJavascriptVariableReference("targetY"))
                )
            ),
            expression = isJavascriptBooleanLiteral(true)
        ))
    }

    @Test
    fun unitLiteralGeneratesNull() {
        val hod = literalUnit()
        val node = generateCode(hod)
        assertThat(node, isJavascriptNull())
    }

    @Test
    fun booleanLiteralGeneratesBooleanLiteral() {
        val hod = literalBool(true)

        val node = generateCode(hod)

        assertThat(node, isJavascriptBooleanLiteral(true))
    }

    @Test
    fun integerLiteralGeneratesIntegerLiteral() {
        val hod = literalInt(42)

        val node = generateCode(hod)

        assertThat(node, isJavascriptIntegerLiteral(42))
    }

    @Test
    fun stringLiteralGeneratesStringLiteral() {
        val hod = literalString("<string>")
        val node = generateCode(hod)
        assertThat(node, isJavascriptStringLiteral("<string>"))
    }

    @Test
    fun codePointLiteralGeneratesStringLiteral() {
        val hod = literalCodePoint('!')
        val node = generateCode(hod)
        assertThat(node, isJavascriptStringLiteral("!"))
    }

    @Test
    fun symbolNameGeneratesString() {
        val hod = symbolName("`blah")
        val node = generateCode(hod, context(moduleName = listOf("A", "B")))
        assertThat(node, isJavascriptStringLiteral("A.B.`blah"))
    }

    @Test
    fun tupleGeneratesTuple() {
        val hod = tupleNode(listOf(literalInt(42), literalBool(true)))
        val node = generateCode(hod, context())
        assertThat(node, isJavascriptArray(elements = isSequence(
            isJavascriptIntegerLiteral(42),
            isJavascriptBooleanLiteral(true)
        )))
    }

    @Test
    fun variableReferenceGenerateVariableReference() {
        val hod = variableReference("x")

        val node = generateCode(hod)

        assertThat(node, isJavascriptVariableReference("x"))
    }

    @Test
    fun notOperationGeneratesNotOperation() {
        val hod = unaryOperation(
            operator = UnaryOperator.NOT,
            operand= literalBool(true)
        )

        val node = generateCode(hod)

        assertThat(node, isJavascriptUnaryOperation(
            operator = equalTo(JavascriptUnaryOperator.NOT),
            operand = isJavascriptBooleanLiteral(true)
        ))
    }

    @Test
    fun unaryMinusOperationGeneratesUnaryMinusOperation() {
        val hod = unaryOperation(
            operator = UnaryOperator.MINUS,
            operand= literalBool(true)
        )

        val node = generateCode(hod)

        assertThat(node, isJavascriptUnaryOperation(
            operator = equalTo(JavascriptUnaryOperator.MINUS),
            operand = isJavascriptBooleanLiteral(true)
        ))
    }

    @TestFactory
    fun binaryOperationGeneratesBinaryOperation(): List<DynamicTest> {
        return listOf(
            BinaryOperator.ADD to JavascriptBinaryOperator.ADD,
            BinaryOperator.SUBTRACT to JavascriptBinaryOperator.SUBTRACT,
            BinaryOperator.MULTIPLY to JavascriptBinaryOperator.MULTIPLY,
            BinaryOperator.EQUALS to JavascriptBinaryOperator.EQUALS,
            BinaryOperator.NOT_EQUAL to JavascriptBinaryOperator.NOT_EQUAL,
            BinaryOperator.LESS_THAN to JavascriptBinaryOperator.LESS_THAN,
            BinaryOperator.LESS_THAN_OR_EQUAL to JavascriptBinaryOperator.LESS_THAN_OR_EQUAL,
            BinaryOperator.GREATER_THAN to JavascriptBinaryOperator.GREATER_THAN,
            BinaryOperator.GREATER_THAN_OR_EQUAL to JavascriptBinaryOperator.GREATER_THAN_OR_EQUAL,
            BinaryOperator.AND to JavascriptBinaryOperator.AND,
            BinaryOperator.OR to JavascriptBinaryOperator.OR
        ).map({ operator ->  DynamicTest.dynamicTest(
            operator.first.toString(), {
                val hod = binaryOperation(
                    operator = operator.first,
                    left = literalInt(0),
                    right = literalInt(1)
                )

                val node = generateCode(hod)

                assertThat(node, isJavascriptBinaryOperation(
                    operator = equalTo(operator.second),
                    left = isJavascriptIntegerLiteral(0),
                    right = isJavascriptIntegerLiteral(1)
                ))
            })
        })
    }

    @Test
    fun isOperationGeneratesTypeCheck() {
        val hod = isOperation(
            expression = variableReference("x"),
            type = staticReference("X")
        )

        val context = context(
            discriminatorsForIsExpressions = mapOf(
                hod to discriminator(symbolType(listOf("M"), "`A"), "tag")
            )
        )
        val node = generateCode(hod, context)

        assertThat(node, isJavascriptTypeCondition(
            isJavascriptVariableReference("x"),
            discriminator(symbolType(listOf("M"), "`A"), "tag")
        ))
    }

    @Test
    fun functionCallGeneratesFunctionCall() {
        val hod = call(variableReference("f"), listOf(literalInt(42)))

        val node = generateCode(hod)

        assertThat(node, isJavascriptFunctionCall(
            isJavascriptVariableReference("f"),
            isSequence(isJavascriptIntegerLiteral(42))
        ))
    }

    @Test
    fun namedArgumentsArePassedAsObject() {
        val hod = call(
            variableReference("f"),
            namedArguments = listOf(callNamedArgument("a", literalBool(true)))
        )

        val node = generateCode(hod)

        assertThat(node, isJavascriptFunctionCall(
            isJavascriptVariableReference("f"),
            isSequence(isJavascriptObject(isMap("a" to isJavascriptBooleanLiteral(true))))
        ))
    }

    @Test
    fun whenThereAreBothPositionalAndNamedArgumentsThenNamedArgumentsObjectIsLastArgument() {
        val hod = call(
            variableReference("f"),
            positionalArguments = listOf(literalInt(1)),
            namedArguments = listOf(callNamedArgument("a", literalBool(true)))
        )

        val node = generateCode(hod)

        assertThat(node, isJavascriptFunctionCall(
            isJavascriptVariableReference("f"),
            isSequence(
                isJavascriptIntegerLiteral(1),
                isJavascriptObject(isMap("a" to isJavascriptBooleanLiteral(true)))
            )
        ))
    }

    @Test
    fun partialFunctionCallWithPositionalArgumentsGeneratesNewFunction() {
        val reference = variableReference("f")
        val hod = partialCall(reference, listOf(literalInt(42), literalBool(false)))

        val node = generateCode(hod, context(
            expressionTypes = mapOf(
                reference to functionType(positionalParameters = listOf(IntType, BoolType, IntType, IntType))
            )
        ))

        assertThat(node, isJavascriptFunctionCall(
            function = isJavascriptFunctionExpression(
                parameters = isSequence(equalTo("\$func"), equalTo("\$arg0"), equalTo("\$arg1")),
                body = isSequence(
                    isJavascriptReturn(
                        isJavascriptFunctionExpression(
                            parameters = isSequence(equalTo("\$arg2"), equalTo("\$arg3")),
                            body = isSequence(
                                isJavascriptReturn(
                                    isJavascriptFunctionCall(
                                        function = isJavascriptVariableReference("\$func"),
                                        arguments = isSequence(
                                            isJavascriptVariableReference("\$arg0"),
                                            isJavascriptVariableReference("\$arg1"),
                                            isJavascriptVariableReference("\$arg2"),
                                            isJavascriptVariableReference("\$arg3")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            arguments = isSequence(
                isJavascriptVariableReference("f"),
                isJavascriptIntegerLiteral(42),
                isJavascriptBooleanLiteral(false)
            )
        ))
    }

    @Test
    fun whenAllPartialArgumentsAreSuppliedThenGeneratedFunctionHasNoNamedArgument() {
        val reference = variableReference("f")
        val hod = partialCall(reference, namedArguments = listOf(callNamedArgument("x", literalInt(42))))

        val node = generateCode(hod, context(
            expressionTypes = mapOf(
                reference to functionType(namedParameters = mapOf(Identifier("x") to IntType))
            )
        ))

        assertThat(node, isJavascriptFunctionCall(
            function = isJavascriptFunctionExpression(
                parameters = isSequence(equalTo("\$func"), equalTo("x")),
                body = isSequence(
                    isJavascriptReturn(
                        isJavascriptFunctionExpression(
                            parameters = isSequence(),
                            body = isSequence(
                                isJavascriptReturn(
                                    isJavascriptFunctionCall(
                                        function = isJavascriptVariableReference("\$func"),
                                        arguments = isSequence(
                                            isJavascriptObject(isMap(
                                                "x" to isJavascriptVariableReference("x")
                                            ))
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            arguments = isSequence(
                isJavascriptVariableReference("f"),
                isJavascriptIntegerLiteral(42)
            )
        ))
    }

    @Test
    fun whenNotAllPartialArgumentsAreSuppliedThenGeneratedFunctionHasNamedArgument() {
        val reference = variableReference("f")
        val hod = partialCall(reference, namedArguments = listOf(callNamedArgument("x", literalInt(42))))

        val node = generateCode(hod, context(
            expressionTypes = mapOf(
                reference to functionType(namedParameters = mapOf(Identifier("x") to IntType, Identifier("y") to IntType))
            )
        ))

        assertThat(node, isJavascriptFunctionCall(
            function = isJavascriptFunctionExpression(
                parameters = isSequence(equalTo("\$func"), equalTo("x")),
                body = isSequence(
                    isJavascriptReturn(
                        isJavascriptFunctionExpression(
                            parameters = isSequence(equalTo("\$named")),
                            body = isSequence(
                                isJavascriptReturn(
                                    isJavascriptFunctionCall(
                                        function = isJavascriptVariableReference("\$func"),
                                        arguments = isSequence(
                                            isJavascriptObject(isMap(
                                                "x" to isJavascriptVariableReference("x"),
                                                "y" to isJavascriptPropertyAccess(
                                                    receiver = isJavascriptVariableReference("\$named"),
                                                    propertyName = equalTo("y")
                                                )
                                            ))
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            arguments = isSequence(
                isJavascriptVariableReference("f"),
                isJavascriptIntegerLiteral(42)
            )
        ))
    }

    @Test
    fun fieldAccessGeneratesPropertyAccess() {
        val hod = fieldAccess(variableReference("x"), "y")

        val node = generateCode(hod)

        assertThat(node, isJavascriptPropertyAccess(
            isJavascriptVariableReference("x"),
            equalTo("y")
        ))
    }

    private fun generateCodeForModuleStatement(node: ModuleStatementNode) = generateCode(node, context())
    private fun generateCodeForFunctionStatement(node: FunctionStatementNode) = generateCode(node, context())
    private fun generateCode(node: ExpressionNode) = generateCode(node, context())

    private fun context(
        moduleName: List<String> = listOf(),
        discriminatorsForIsExpressions: Map<IsNode, Discriminator> = mapOf(),
        discriminatorsForWhenBranches: Map<Pair<WhenNode, WhenBranchNode>, Discriminator> = mapOf(),
        expressionTypes: Map<ExpressionNode, Type> = mapOf(),
        shapeFields: Map<ShapeBaseNode, List<FieldInspector>> = mapOf()
    ): CodeGenerationContext {
        return CodeGenerationContext(
            moduleName = moduleName.map(::Identifier),
            inspector = SimpleCodeInspector(
                discriminatorsForIsExpressions = discriminatorsForIsExpressions,
                discriminatorsForWhenBranches = discriminatorsForWhenBranches,
                expressionTypes = expressionTypes,
                shapeFields = shapeFields
            )
        )
    }

    private fun isJavascriptModule(body: Matcher<List<JavascriptStatementNode>>)
        = cast(has(JavascriptModuleNode::body, body))

    private fun isJavascriptFunction(
        name: Matcher<String>,
        parameters: Matcher<List<String>> = anything,
        body: Matcher<List<JavascriptStatementNode>> = anything
    ) : Matcher<JavascriptStatementNode>
        = cast(allOf(
            has(JavascriptFunctionDeclarationNode::name, name),
            has(JavascriptFunctionDeclarationNode::parameters, parameters),
            has(JavascriptFunctionDeclarationNode::body, body)
        ))

    private fun isJavascriptFunctionExpression(
        parameters: Matcher<List<String>> = anything,
        body: Matcher<List<JavascriptStatementNode>> = anything
    ) : Matcher<JavascriptExpressionNode>
        = cast(allOf(
        has(JavascriptFunctionExpressionNode::parameters, parameters),
        has(JavascriptFunctionExpressionNode::body, body)
    ))

    private fun isJavascriptConst(
        target: Matcher<JavascriptTargetNode>,
        expression: Matcher<JavascriptExpressionNode>
    ): Matcher<JavascriptStatementNode>  = cast(allOf(
        has(JavascriptConstNode::target, target),
        has(JavascriptConstNode::expression, expression)
    ))

    private fun isJavascriptIfStatement(
        conditionalBranches: Matcher<List<JavascriptConditionalBranchNode>>,
        elseBranch: Matcher<List<JavascriptStatementNode>>
    ): Matcher<JavascriptStatementNode> {
        return cast(allOf(
            has(JavascriptIfStatementNode::conditionalBranches, conditionalBranches),
            has(JavascriptIfStatementNode::elseBranch, elseBranch)
        ))
    }

    private fun isJavascriptConditionalBranch(
        condition: Matcher<JavascriptExpressionNode>,
        body: Matcher<List<JavascriptStatementNode>>
    ): Matcher<JavascriptConditionalBranchNode> {
        return allOf(
            has(JavascriptConditionalBranchNode::condition, condition),
            has(JavascriptConditionalBranchNode::body, body)
        )
    }

    private fun isJavascriptReturn(expression: Matcher<JavascriptExpressionNode>)
        : Matcher<JavascriptStatementNode>
        = cast(has(JavascriptReturnNode::expression, expression))

    private fun isJavascriptExpressionStatement(expression: Matcher<JavascriptExpressionNode>)
        = cast(has(JavascriptExpressionStatementNode::expression, expression))

    private fun isJavascriptBooleanLiteral(value: Boolean)
        : Matcher<JavascriptExpressionNode>
        = cast(has(JavascriptBooleanLiteralNode::value, equalTo(value)))

    private fun isJavascriptNull()
        : Matcher<JavascriptExpressionNode>
        = isA<JavascriptNullLiteralNode>()

    private fun isJavascriptIntegerLiteral(value: Int)
        : Matcher<JavascriptExpressionNode>
        = cast(has(JavascriptIntegerLiteralNode::value, has(BigInteger::intValueExact, equalTo(value))))

    private fun isJavascriptStringLiteral(value: String)
        : Matcher<JavascriptExpressionNode>
        = cast(has(JavascriptStringLiteralNode::value, equalTo(value)))

    private fun isJavascriptVariableReference(name: String)
        : Matcher<JavascriptNode>
        = cast(has(JavascriptVariableReferenceNode::name, equalTo(name)))

    private fun isJavascriptUnaryOperation(
        operator: Matcher<JavascriptUnaryOperator>,
        operand: Matcher<JavascriptExpressionNode>
    ): Matcher<JavascriptExpressionNode> = cast(allOf(
        has(JavascriptUnaryOperationNode::operator, operator),
        has(JavascriptUnaryOperationNode::operand, operand)
    ))

    private fun isJavascriptBinaryOperation(
        operator: Matcher<JavascriptBinaryOperator>,
        left: Matcher<JavascriptExpressionNode>,
        right: Matcher<JavascriptExpressionNode>
    ) : Matcher<JavascriptExpressionNode>
    = cast(allOf(
        has(JavascriptBinaryOperationNode::operator, operator),
        has(JavascriptBinaryOperationNode::left, left),
        has(JavascriptBinaryOperationNode::right, right)
    ))

    private fun isJavascriptFunctionCall(
        function: Matcher<JavascriptExpressionNode>,
        arguments: Matcher<List<JavascriptExpressionNode>>
    ) : Matcher<JavascriptExpressionNode>
    = cast(allOf(
        has(JavascriptFunctionCallNode::function, function),
        has(JavascriptFunctionCallNode::arguments, arguments)
    ))

    private fun isJavascriptPropertyAccess(
        receiver: Matcher<JavascriptExpressionNode>,
        propertyName: Matcher<String>
    ) : Matcher<JavascriptExpressionNode> = cast(allOf(
        has(JavascriptPropertyAccessNode::receiver, receiver),
        has(JavascriptPropertyAccessNode::propertyName, propertyName)
    ))

    private fun isJavascriptArray(
        elements: Matcher<List<JavascriptExpressionNode>>
    ): Matcher<JavascriptExpressionNode> = cast(
        has(JavascriptArrayLiteralNode::elements, elements)
    )

    private fun isJavascriptObject(
        properties: Matcher<Map<String, JavascriptExpressionNode>>
    ): Matcher<JavascriptExpressionNode> = cast(
        has(JavascriptObjectLiteralNode::properties, properties)
    )

    private fun isJavascriptAssignment(
        target: Matcher<JavascriptExpressionNode>,
        expression: Matcher<JavascriptExpressionNode>
    ): Matcher<JavascriptExpressionNode> = cast(allOf(
        has(JavascriptAssignmentNode::target, target),
        has(JavascriptAssignmentNode::expression, expression)
    ))

    private fun isJavascriptArrayDestructuring(
        elements: Matcher<List<JavascriptTargetNode>>
    ): Matcher<JavascriptTargetNode> = cast(
        has(JavascriptArrayDestructuringNode::elements, elements)
    )

    private fun isJavascriptObjectDestructuring(
        properties: Matcher<List<Pair<String, JavascriptTargetNode>>>
    ): Matcher<JavascriptTargetNode> = cast(
        has(JavascriptObjectDestructuringNode::properties, properties)
    )

    private fun isJavascriptAssignmentStatement(
        target: Matcher<JavascriptExpressionNode>,
        expression: Matcher<JavascriptExpressionNode>
    ) = isJavascriptExpressionStatement(
        isJavascriptAssignment(target = target, expression = expression)
    )

    private fun isJavascriptTypeCondition(
        expression: Matcher<JavascriptExpressionNode>,
        discriminator: Discriminator
    ): Matcher<JavascriptExpressionNode> {
        return isJavascriptBinaryOperation(
            operator = equalTo(JavascriptBinaryOperator.EQUALS),
            left = isJavascriptPropertyAccess(
                receiver = expression,
                propertyName = equalTo(discriminator.fieldName.value)
            ),
            right = isJavascriptStringLiteral(discriminator.symbolType.symbol.fullName)
        )
    }

    private fun isJavascriptImmediatelyInvokedFunction(
        body: Matcher<List<JavascriptStatementNode>>
    ): Matcher<JavascriptExpressionNode> {
        return isJavascriptFunctionCall(
            function = isJavascriptFunctionExpression(
                parameters = isSequence(),
                body = body
            ),
            arguments = isSequence()
        )
    }
}
