package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.ast.Identifier
import org.zwobble.hod.compiler.tests.isType
import org.zwobble.hod.compiler.tests.varargsDeclaration
import org.zwobble.hod.compiler.tests.variableReference
import org.zwobble.hod.compiler.typechecker.typeCheckModuleStatement
import org.zwobble.hod.compiler.types.UnitType
import org.zwobble.hod.compiler.types.VarargsType
import org.zwobble.hod.compiler.types.functionType

class TypeCheckVarargsDeclarationTests {
    @Test
    fun varargsDeclarationDeclaresVarargsFunction() {
        val consReference = variableReference("cons")
        val nilReference = variableReference("nil")
        val node = varargsDeclaration(
            name = "list",
            cons = consReference,
            nil = nilReference
        )
        val consType = functionType()
        val nilType = UnitType

        val typeContext = typeContext(
            referenceTypes = mapOf(
                consReference to consType,
                nilReference to nilType
            )
        )
        typeCheckModuleStatement(node, typeContext)

        assertThat(typeContext.typeOf(node), isType(VarargsType(
            name = Identifier("list"),
            cons = consType,
            nil = nilType
        )))
    }
}
