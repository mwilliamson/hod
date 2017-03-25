package org.shedlang.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.cast
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.shedlang.compiler.tests.isOperation
import org.shedlang.compiler.tests.literalBool
import org.shedlang.compiler.tests.typeReference
import org.shedlang.compiler.typechecker.BoolType
import org.shedlang.compiler.typechecker.MetaType
import org.shedlang.compiler.typechecker.inferType

class TypeCheckIsOperationTests {
    @Test
    fun isOperationHasBooleanType() {
        val booleanType = typeReference("Boolean")
        val node = isOperation(
            expression = literalBool(),
            type = booleanType
        )

        val typeContext = typeContext(referenceTypes = mapOf(booleanType to MetaType(BoolType)))
        val type = inferType(node, typeContext)
        assertThat(type, cast(equalTo(BoolType)))
    }
}