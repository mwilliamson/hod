package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.ast.Identifier
import org.zwobble.hod.compiler.tests.*
import org.zwobble.hod.compiler.typechecker.typeCheck
import org.zwobble.hod.compiler.types.IntType
import org.zwobble.hod.compiler.types.MetaType
import org.zwobble.hod.compiler.types.MetaTypeGroup

class TypeCheckTypesModuleTests {
    @Test
    fun bodyIsTypeChecked() {
        val reference = staticReference("x")
        val node = typesModule(body = listOf(
            valType(type = reference)
        ))

        val typeContext = typeContext(referenceTypes = mapOf(reference to IntType))

        assertThat(
            {
                typeCheck(node, typeContext)
            },
            throwsUnexpectedType(expected = MetaTypeGroup, actual = IntType)
        )
    }

    @Test
    fun typeOfModuleIsReturned() {
        val intReference = staticReference("Int")
        val node = typesModule(
            body = listOf(
                valType(name = "value", type = intReference)
            )
        )

        val result = typeCheck(node, typeContext(
            referenceTypes = mapOf(intReference to MetaType(IntType))
        ))
        assertThat(result.fields, isMap(
            Identifier("value") to isIntType
        ))
    }
}
