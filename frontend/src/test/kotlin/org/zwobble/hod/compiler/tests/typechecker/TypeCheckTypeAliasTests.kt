package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.tests.*
import org.zwobble.hod.compiler.typechecker.typeCheckModuleStatement
import org.zwobble.hod.compiler.types.IntType
import org.zwobble.hod.compiler.types.MetaType

class TypeCheckTypeAliasTests {
    @Test
    fun typeAliasDeclaresType() {
        val intType = staticReference("Int")
        val node = typeAliasDeclaration("Size", expression = intType)

        val typeContext = typeContext(referenceTypes = mapOf(
            intType to MetaType(IntType)
        ))
        typeCheckModuleStatement(node, typeContext)
        assertThat(typeContext.typeOf(node), isMetaType(isTypeAlias(
            name = isIdentifier("Size"),
            aliasedType = isIntType
        )))
    }
}
