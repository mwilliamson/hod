package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.tests.isIdentifier
import org.zwobble.hod.compiler.tests.parameter
import org.zwobble.hod.compiler.typechecker.NodeTypesMap
import org.zwobble.hod.compiler.typechecker.UnknownTypeError


class NodeTypesMapTests {
    @Test
    fun whenTypeIsMissingThenExceptionIsThrown() {
        val types = NodeTypesMap(mapOf())
        assertThat(
            { types.typeOf(parameter("x")) },
            throws(has(UnknownTypeError::name, isIdentifier("x")))
        )
    }

}
