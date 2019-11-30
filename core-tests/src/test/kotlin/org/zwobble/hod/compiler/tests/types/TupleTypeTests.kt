package org.zwobble.hod.compiler.tests.types

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.zwobble.hod.compiler.types.BoolType
import org.zwobble.hod.compiler.types.IntType
import org.zwobble.hod.compiler.types.TupleType

class TupleTypeTests {
    @Test
    fun shortDescriptionOfEmptyTupleHasNoElements() {
        val type = TupleType(listOf())

        assertThat(type.shortDescription, equalTo("#()"))
    }

    @Test
    fun shortDescriptionOfTupleWithElements() {
        val type = TupleType(listOf(IntType, BoolType))

        assertThat(type.shortDescription, equalTo("#(Int, Bool)"))
    }
}
