package org.zwobble.hod.compiler.tests.typechecker

import com.natpryce.hamkrest.assertion.assertThat
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.tests.staticReference
import org.zwobble.hod.compiler.typechecker.ResolvedReferencesMap

class VariableReferencesMapTests {
    @Test
    fun whenVariableIsUnresolvedThenCompilerErrorIsThrown() {
        val reference = staticReference("x")
        val references = ResolvedReferencesMap(mapOf())

        assertThat(
            { references[reference] },
            throwsCompilerError("reference x is unresolved")
        )
    }
}
