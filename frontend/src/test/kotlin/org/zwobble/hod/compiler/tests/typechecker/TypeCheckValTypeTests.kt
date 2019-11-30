package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.tests.*
import org.zwobble.hod.compiler.typechecker.ResolvedReferencesMap
import org.zwobble.hod.compiler.typechecker.newTypeContext
import org.zwobble.hod.compiler.typechecker.typeCheck
import org.zwobble.hod.compiler.types.IntType
import org.zwobble.hod.compiler.types.MetaType

class TypeCheckValTypeTests {
    @Test
    fun typeExpressionIsTypeChecked() {
        val intReference = staticReference("Int")
        val node = valType(type = intReference)
        val typeContext = typeContext(referenceTypes = mapOf(intReference to IntType))
        assertThat(
            {
                typeCheck(node, typeContext)
            },
            throwsUnexpectedType(
                expected = isMetaTypeGroup,
                actual = isIntType
            )
        )
    }

    @Test
    fun valIsTypedUsingTypeExpression() {
        val intDeclaration = declaration("Int")
        val intReference = staticReference("Int")
        val node = valType(name = "value", type = intReference)
        val typeContext = newTypeContext(
            moduleName = null,
            nodeTypes = mapOf(
                intDeclaration.nodeId to MetaType(IntType)
            ),
            resolvedReferences = ResolvedReferencesMap(mapOf(
                intReference.nodeId to intDeclaration
            )),
            getModule = { moduleName -> throw UnsupportedOperationException() }
        )
        typeCheck(node, typeContext)
        assertThat(typeContext.typeOf(node), isIntType)
    }
}
