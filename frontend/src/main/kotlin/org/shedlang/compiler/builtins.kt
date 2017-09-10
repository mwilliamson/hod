package org.shedlang.compiler

import org.shedlang.compiler.ast.freshNodeId
import org.shedlang.compiler.parser.parse
import org.shedlang.compiler.typechecker.evalType
import org.shedlang.compiler.typechecker.newTypeContext
import org.shedlang.compiler.typechecker.resolve
import org.shedlang.compiler.types.*

internal data class Builtin(
    val name: String,
    val type: Type
) {
    val nodeId = freshNodeId()
}

private val coreBuiltins = listOf(
    Builtin("Any", MetaType(AnyType)),
    Builtin("Unit", MetaType(UnitType)),
    Builtin("Int", MetaType(IntType)),
    Builtin("String", MetaType(StringType)),
    Builtin("Bool", MetaType(BoolType)),
    Builtin("List", MetaType(ListType))
)

fun parseType(string: String): Type {
    val node = parse(
        filename = "<string>",
        input = string,
        rule = { tokens -> org.shedlang.compiler.parser.parseType(tokens) }
    )
    val resolvedReferences = resolve(
        node,
        coreBuiltins.associate({ builtin -> builtin.name to builtin.nodeId})
    )

    val typeContext = newTypeContext(
        nodeTypes = coreBuiltins.associate({ builtin -> builtin.nodeId to builtin.type}),
        resolvedReferences = resolvedReferences,
        getModule = { importPath -> throw UnsupportedOperationException() }
    )

    return evalType(node, typeContext)
}

private val mapType = parseType("[T, R, !E]((T) !E -> T, List[T]) !E -> List[R]")
private val forEachType = parseType("[T, !E]((T) !E -> Unit, List[T]) !E -> Unit")

internal val builtins = coreBuiltins + listOf(
    Builtin("!io", EffectType(IoEffect)),

    Builtin("print", FunctionType(
        staticParameters = listOf(),
        positionalArguments = listOf(StringType),
        namedArguments = mapOf(),
        effect = IoEffect,
        returns = UnitType
    )),
    Builtin("intToString", positionalFunctionType(listOf(IntType), StringType)),
    Builtin("list", ListConstructorType),
    Builtin("map", mapType),
    Builtin("forEach", forEachType)
)
