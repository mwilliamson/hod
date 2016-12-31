package org.shedlang.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test
import org.shedlang.compiler.ast.BooleanLiteralNode
import org.shedlang.compiler.ast.IntegerLiteralNode
import org.shedlang.compiler.ast.SourceLocation
import org.shedlang.compiler.ast.VariableReferenceNode
import org.shedlang.compiler.typechecker.*

class TypeCheckExpressionTests {
    @Test
    fun booleanLiteralIsTypedAsInteger() {
        val node = BooleanLiteralNode(true, anySourceLocation())
        val type = inferType(node, emptyTypeContext())
        assertThat(type, cast(equalTo(BoolType)))
    }

    @Test
    fun integerLiteralIsTypedAsInteger() {
        val node = IntegerLiteralNode(42, anySourceLocation())
        val type = inferType(node, emptyTypeContext())
        assertThat(type, cast(equalTo(IntType)))
    }

    @Test
    fun variableReferenceTypeIsRetrievedFromContext() {
        val node = VariableReferenceNode("x", anySourceLocation())
        val type = inferType(node, TypeContext(mutableMapOf(Pair("x", IntType))))
        assertThat(type, cast(equalTo(IntType)))
    }

    @Test
    fun exceptionWhenVariableNotInScope() {
        val node = VariableReferenceNode("x", anySourceLocation())
        assertThat(
            { inferType(node, emptyTypeContext()) },
            throws(has(UnboundLocalError::name, equalTo("x")))
        )
    }

    fun emptyTypeContext(): TypeContext {
        return TypeContext(mutableMapOf())
    }

    fun anySourceLocation(): SourceLocation {
        return SourceLocation("<string>", 0)
    }
}
